import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Scanner;
import java.util.Base64;

public class Initiator {
    private static final String HOST = "127.0.0.1"; // Cambiar a la IP del Respondedor
    private static final int PORT = 65432;
    private static final String ID_I = "Iniciador_01";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("INICIADOR");
        System.out.println("========================================");
        
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("[Estado] Conectado al Respondedor exitosamente.\n");
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            
            // Variables de estado (persisten durante el menú)
            KeyPair pairI = null;
            PublicKey pkR = null;
            byte[] kMac = null;
            
            boolean continuar = true;
            while (continuar) {
                System.out.println("\n--- MENÚ PRINCIPAL ---");
                System.out.println("[1] Generar llaves RSA e Intercambiar (Setup)");
                System.out.println("[2] Ejecutar fase SHARE (Intercambio k_I y k_R)");
                System.out.println("[3] Ejecutar fase EXCH (Pendiente)");
                System.out.println("[4] Ejecutar fase AUTH (Pendiente)");
                System.out.println("[5] Salir");
                System.out.print("Seleccione un paso y presione Enter al mismo tiempo que su compañero: ");
                
                int opcion = scanner.nextInt();
                System.out.println();
                
                switch (opcion) {
                    case 1:
                        System.out.println("=== PASO 0: SETUP RSA ===");
                        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                        keyGen.initialize(512);
                        pairI = keyGen.generateKeyPair();
                        
                        byte[] pkIBytes = pairI.getPublic().getEncoded();
                        System.out.println("[Operación] Mi llave pública generada (Base64): " + Base64.getEncoder().encodeToString(pkIBytes).substring(0, 50) + "...");
                        
                        out.writeInt(pkIBytes.length);
                        out.write(pkIBytes);
                        System.out.println("[Operación] Llave pública enviada. Esperando la del Respondedor...");
                        
                        int pkRLength = in.readInt();
                        byte[] pkRBytes = new byte[pkRLength];
                        in.readFully(pkRBytes);
                        
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        pkR = keyFactory.generatePublic(new X509EncodedKeySpec(pkRBytes));
                        System.out.println("[Operación] Llave del Respondedor recibida y almacenada en memoria.");
                        break;
                        
                    case 2:
                        if (pkR == null) {
                            System.out.println("[Alerta] Debes ejecutar el Paso 1 primero.");
                            break;
                        }
                        System.out.println("=== FASE SHARE ===");
                        byte[] kI = new byte[16];
                        new SecureRandom().nextBytes(kI);
                        System.out.println("[Operación] Valor k_I generado: " + bytesToHex(kI));
                        
                        byte[] idBytes = ID_I.getBytes();
                        byte[] mensaje = new byte[idBytes.length + kI.length];
                        System.arraycopy(idBytes, 0, mensaje, 0, idBytes.length);
                        System.arraycopy(kI, 0, mensaje, idBytes.length, kI.length);
                        
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, pkR);
                        byte[] cI = cipher.doFinal(mensaje);
                        
                        out.writeInt(cI.length);
                        out.write(cI);
                        System.out.println("[Operación] c_I enviado (" + cI.length + " bytes). Esperando c_R...");
                        
                        int cRLength = in.readInt();
                        byte[] cR = new byte[cRLength];
                        in.readFully(cR);
                        System.out.println("[Operación] Recibido c_R del Respondedor.");
                        
                        cipher.init(Cipher.DECRYPT_MODE, pairI.getPrivate());
                        byte[] kR = cipher.doFinal(cR);
                        
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(kI);
                        md.update(kR);
                        kMac = md.digest();
                        System.out.println("[Resultado] Clave compartida (k_mac) derivada: " + bytesToHex(kMac));
                        break;
                        
                    case 3:
                        System.out.println("Fase EXCH: En construcción...");
                        break;
                    case 4:
                        System.out.println("Fase AUTH: En construcción...");
                        break;
                    case 5:
                        System.out.println("Cerrando conexión...");
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el Iniciador: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) { sb.append(String.format("%02x", b)); }
        return sb.toString();
    }
}