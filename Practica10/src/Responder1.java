import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Arrays;

public class Responder1 {
    private static final int PORT = 65432;

    public static void main(String[] args) throws Exception {
        System.out.println("--- RESPONDER ---");
        
        // Generar par de claves RSA (512 bits por rapidez en simulación)
        System.out.println("Generando par de claves RSA...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair pairR = keyGen.generateKeyPair();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Esperando conexión del Iniciador en el puerto " + PORT + "...");
            Socket socket = serverSocket.accept();
            System.out.println("Conectado con " + socket.getInetAddress());
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            // 0. Intercambio de Claves Públicas
            // Enviar pk_R
            byte[] pkRBytes = pairR.getPublic().getEncoded();
            out.writeInt(pkRBytes.length);
            out.write(pkRBytes);
            
            // Recibir pk_I
            int pkILength = in.readInt();
            byte[] pkIBytes = new byte[pkILength];
            in.readFully(pkIBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pkI = keyFactory.generatePublic(new X509EncodedKeySpec(pkIBytes));
            System.out.println("Claves públicas intercambiadas exitosamente.\n");
            
            System.out.println("=== FASE SHARE ===");
            
            // 1. Recibir c_I
            int cILength = in.readInt();
            byte[] cI = new byte[cILength];
            in.readFully(cI);
            System.out.println("Recibido c_I del Iniciador.");
            
            // 2. Descifrar c_I
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pairR.getPrivate());
            byte[] mensajeDescifrado = cipher.doFinal(cI);
            
            // Separar ID_I (todo menos los últimos 16 bytes) y k_I (los últimos 16 bytes)
            int idLength = mensajeDescifrado.length - 16;
            byte[] idIBytes = Arrays.copyOfRange(mensajeDescifrado, 0, idLength);
            byte[] kI = Arrays.copyOfRange(mensajeDescifrado, idLength, mensajeDescifrado.length);
            System.out.println("[Resultado] c_I descifrado. ID_I: " + new String(idIBytes) + ", k_I obtenido.");
            
            // 3. Generar k_R y cifrar c_R
            System.out.println("Generando valor aleatorio k_R.");
            byte[] kR = new byte[16];
            new SecureRandom().nextBytes(kR);
            
            cipher.init(Cipher.ENCRYPT_MODE, pkI);
            byte[] cR = cipher.doFinal(kR);
            
            // 4. Enviar c_R
            out.writeInt(cR.length);
            out.write(cR);
            System.out.println("Enviando c_R al Iniciador.");
            
            // 5. Calcular k_mac = h(k_I | k_R)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(kI);
            md.update(kR);
            byte[] kMac = md.digest();
            System.out.println("Clave MAC generada (k_mac): " + bytesToHex(kMac));

            System.out.println("\n=== FASE EXCH ===");
            
            // 1. Recibir el punto público X del Iniciador
            int lenXX = in.readInt();
            byte[] xCoordXBytes = new byte[lenXX];
            in.readFully(xCoordXBytes);
            
            int lenYX = in.readInt();
            byte[] yCoordXBytes = new byte[lenYX];
            in.readFully(yCoordXBytes);
            
            Punto X = new Punto(new BigInteger(xCoordXBytes), new BigInteger(yCoordXBytes), BigInteger.ONE);
            System.out.println("[Operación] Punto X recibido: " + X.toString());
            
            // 2. Generar valor secreto y (debe ser 1 <= y < q)
            BigInteger ySecreto;
            do {
                ySecreto = new BigInteger(CurvaEliptica.q.bitLength(), new SecureRandom());
            } while (ySecreto.compareTo(BigInteger.ONE) < 0 || ySecreto.compareTo(CurvaEliptica.q) >= 0);
            System.out.println("[Operación] Generado secreto 'y' (módulo q).");
            
            // 3. Calcular el punto público Y = y * G
            Punto Y = CurvaEliptica.RTL(CurvaEliptica.G, ySecreto);
            System.out.println("[Operación] Punto público Y calculado: " + Y.toString());
            
            // 4. Enviar las coordenadas de Y al Iniciador
            byte[] xCoordY = Y.x.toByteArray();
            byte[] yCoordY = Y.y.toByteArray();
            
            out.writeInt(xCoordY.length);
            out.write(xCoordY);
            out.writeInt(yCoordY.length);
            out.write(yCoordY);
            System.out.println("[Operación] Punto Y enviado al Iniciador.");
            
            // 5. Calcular el secreto compartido (xyG)
            Punto secretoCompartidoR = CurvaEliptica.RTL(X, ySecreto);
            System.out.println("[Resultado] Secreto compartido Diffie-Hellman calculado: " + secretoCompartidoR.toString());

            System.out.println("\n=== FASE AUTH ===");
            
            // NOTA: Asegúrate de declarar los IDs arriba en tu clase:
            // private static final String ID_R = "Responder_01";
            // private static final String ID_I = "Iniciador_01";

            // 1. Inicializar HMAC con la llave kMac (obtenida en la fase SHARE)
            javax.crypto.spec.SecretKeySpec macKey = new javax.crypto.spec.SecretKeySpec(kMac, "HmacSHA256");
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            hmac.init(macKey);
            
            // 2. Recibir mac_I
            int lenMacI = in.readInt();
            byte[] macI = new byte[lenMacI];
            in.readFully(macI);
            System.out.println("[Operación] mac_I recibido del Iniciador.");
            
            // 3. Concatenar (X | Y | ID_R | ID_I)
            ByteArrayOutputStream baosR = new ByteArrayOutputStream();
            baosR.write(X.x.toByteArray()); baosR.write(X.y.toByteArray());
            baosR.write(Y.x.toByteArray()); baosR.write(Y.y.toByteArray());
            baosR.write("Responder_01".getBytes()); // Sustituir por la constante ID_R
            baosR.write("Iniciador_01".getBytes()); // Sustituir por la constante ID_I
            
            // 4. Generar mac_R y enviarlo
            byte[] macR = hmac.doFinal(baosR.toByteArray());
            out.writeInt(macR.length);
            out.write(macR);
            System.out.println("[Operación] mac_R generado y enviado al Iniciador.");
            
            // 5. Calcular clave de sesión final k_sess = h(coordenada x del secreto compartido)
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] kSess = sha256.digest(secretoCompartidoR.x.toByteArray());
            System.out.println("[Resultado FINAL] Clave de sesión (k_sess): " + bytesToHex(kSess));
            System.out.println("--- PROTOCOLO SKEME COMPLETADO CON ÉXITO ---");

            socket.close();
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
