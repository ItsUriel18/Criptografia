import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class Initiator {
    // Cambia "127.0.0.1" por la IP de la computadora Respondedor
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 65432;
    private static final String ID_I = "Iniciador_01";

    public static void main(String[] args) throws Exception {
        System.out.println("--- INITIATOR ---");
        
        System.out.println("Generando par de claves RSA...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair pairI = keyGen.generateKeyPair();
        
        System.out.println("Conectando al Responder en " + HOST + ":" + PORT + "...");
        try (Socket socket = new Socket(HOST, PORT)) {
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            // 0. Intercambio de Claves Públicas
            // Recibir pk_R
            int pkRLength = in.readInt();
            byte[] pkRBytes = new byte[pkRLength];
            in.readFully(pkRBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pkR = keyFactory.generatePublic(new X509EncodedKeySpec(pkRBytes));
            
            // Enviar pk_I
            byte[] pkIBytes = pairI.getPublic().getEncoded();
            out.writeInt(pkIBytes.length);
            out.write(pkIBytes);
            System.out.println("Claves públicas intercambiadas exitosamente.\n");
            
            System.out.println("=== FASE SHARE ===");
            
            // 1. Generar k_I
            System.out.println("Generando valor aleatorio k_I.");
            byte[] kI = new byte[16];
            new SecureRandom().nextBytes(kI);
            
            // 2. Concatenar (ID_I || k_I) y cifrar con pk_R
            byte[] idBytes = ID_I.getBytes();
            byte[] mensaje = new byte[idBytes.length + kI.length];
            System.arraycopy(idBytes, 0, mensaje, 0, idBytes.length);
            System.arraycopy(kI, 0, mensaje, idBytes.length, kI.length);
            
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pkR);
            byte[] cI = cipher.doFinal(mensaje);
            
            // 3. Enviar c_I
            out.writeInt(cI.length);
            out.write(cI);
            System.out.println("Enviando c_I al Respondedor.");
            
            // 4. Recibir c_R
            int cRLength = in.readInt();
            byte[] cR = new byte[cRLength];
            in.readFully(cR);
            System.out.println("Recibido c_R del Respondedor.");
            
            // 5. Descifrar c_R
            cipher.init(Cipher.DECRYPT_MODE, pairI.getPrivate());
            byte[] kR = cipher.doFinal(cR);
            System.out.println("c_R descifrado, k_R obtenido.");
            
            // 6. Calcular k_mac = h(k_I | k_R)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(kI);
            md.update(kR);
            byte[] kMac = md.digest();
            System.out.println("Clave MAC generada (k_mac): " + bytesToHex(kMac));

            System.out.println("\n=== FASE EXCH ===");
            
            // 1. Generar valor secreto x (debe ser 1 <= x < q)
            // NOTA: Asegúrate de tener la variable q definida en tu clase CurvaEliptica, e.g., public static BigInteger q = new BigInteger("967");
            BigInteger xSecreto;
            do {
                xSecreto = new BigInteger(CurvaEliptica.q.bitLength(), new SecureRandom());
            } while (xSecreto.compareTo(BigInteger.ONE) < 0 || xSecreto.compareTo(CurvaEliptica.q) >= 0);
            System.out.println("[Operación] Generado secreto 'x' (módulo q).");
            
            // 2. Calcular el punto público X = x * G
            Punto X = CurvaEliptica.RTL(CurvaEliptica.G, xSecreto);
            System.out.println("[Operación] Punto público X calculado: " + X.toString());
            
            // 3. Enviar las coordenadas de X al Respondedor
            byte[] xCoordX = X.x.toByteArray();
            byte[] yCoordX = X.y.toByteArray();
            
            out.writeInt(xCoordX.length);
            out.write(xCoordX);
            out.writeInt(yCoordX.length);
            out.write(yCoordX);
            System.out.println("[Operación] Punto X enviado al Respondedor.");
            
            // 4. Recibir el punto público Y del Respondedor
            int lenXY = in.readInt();
            byte[] xCoordYBytes = new byte[lenXY];
            in.readFully(xCoordYBytes);
            
            int lenYY = in.readInt();
            byte[] yCoordYBytes = new byte[lenYY];
            in.readFully(yCoordYBytes);
            
            Punto Y = new Punto(new BigInteger(xCoordYBytes), new BigInteger(yCoordYBytes), BigInteger.ONE);
            System.out.println("[Operación] Punto Y recibido: " + Y.toString());
            
            // 5. Calcular el secreto compartido (xyG)
            Punto secretoCompartidoI = CurvaEliptica.RTL(Y, xSecreto);
            System.out.println("[Resultado] Secreto compartido Diffie-Hellman calculado: " + secretoCompartidoI.toString());

            System.out.println("\n=== FASE AUTH ===");
            
            // NOTA: Asegúrate de declarar el ID del respondedor arriba en tu clase:
            // private static final String ID_R = "Responder_01";

            // 1. Inicializar HMAC con la llave kMac (obtenida en la fase SHARE)
            javax.crypto.spec.SecretKeySpec macKey = new javax.crypto.spec.SecretKeySpec(kMac, "HmacSHA256");
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            hmac.init(macKey);
            
            // 2. Concatenar (Y | X | ID_I | ID_R)
            ByteArrayOutputStream baosI = new ByteArrayOutputStream();
            baosI.write(Y.x.toByteArray()); baosI.write(Y.y.toByteArray());
            baosI.write(X.x.toByteArray()); baosI.write(X.y.toByteArray());
            baosI.write(ID_I.getBytes());
            baosI.write("Responder_01".getBytes()); // Sustituir por la constante ID_R
            
            // 3. Generar mac_I y enviarlo
            byte[] macI = hmac.doFinal(baosI.toByteArray());
            out.writeInt(macI.length);
            out.write(macI);
            System.out.println("[Operación] mac_I generado y enviado al Respondedor.");
            
            // 4. Recibir mac_R
            int lenMacR = in.readInt();
            byte[] macR = new byte[lenMacR];
            in.readFully(macR);
            System.out.println("[Operación] mac_R recibido del Respondedor.");
            
            // 5. Calcular clave de sesión final k_sess = h(coordenada x del secreto compartido)
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] kSess = sha256.digest(secretoCompartidoI.x.toByteArray());
            System.out.println("[Resultado FINAL] Clave de sesión (k_sess): " + bytesToHex(kSess));
            System.out.println("--- PROTOCOLO SKEME COMPLETADO CON ÉXITO ---");
        }
    }
    
    // Método auxiliar para imprimir los bytes en Hexadecimal
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}