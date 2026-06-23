import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Scanner;
import java.util.Base64;
import java.util.Arrays;

public class Responder {
    private static final int PORT = 65432;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SIMULADOR SKEME - RESPONDEDOR");
        System.out.println("========================================");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Estado] Esperando al Iniciador en el puerto " + PORT + "...");
            Socket socket = serverSocket.accept();
            System.out.println("[Estado] Conectado con el Iniciador (" + socket.getInetAddress() + ")\n");
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            
            // Variables de estado
            KeyPair pairR = null;
            PublicKey pkI = null;
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
                        pairR = keyGen.generateKeyPair();
                        
                        byte[] pkRBytes = pairR.getPublic().getEncoded();
                        System.out.println("[Operación] Mi llave pública generada (Base64): " + Base64.getEncoder().encodeToString(pkRBytes).substring(0, 50) + "...");
                        
                        out.writeInt(pkRBytes.length);
                        out.write(pkRBytes);
                        System.out.println("[Operación] Llave pública enviada. Esperando la del Iniciador...");
                        
                        int pkILength = in.readInt();
                        byte[] pkIBytes = new byte[pkILength];
                        in.readFully(pkIBytes);
                        
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        pkI = keyFactory.generatePublic(new X509EncodedKeySpec(pkIBytes));
                        System.out.println("[Operación] Llave del Iniciador recibida y almacenada en memoria.");
                        break;
                        
                    case 2:
                        if (pkI == null) {
                            System.out.println("[Alerta] Debes ejecutar el Paso 1 primero.");
                            break;
                        }
                        System.out.println("=== FASE SHARE ===");
                        System.out.println("[Operación] Esperando c_I del Iniciador...");
                        int cILength = in.readInt();
                        byte[] cI = new byte[cILength];
                        in.readFully(cI);
                        
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.DECRYPT_MODE, pairR.getPrivate());
                        byte[] mensajeDescifrado = cipher.doFinal(cI);
                        
                        int idLength = mensajeDescifrado.length - 16;
                        byte[] idIBytes = Arrays.copyOfRange(mensajeDescifrado, 0, idLength);
                        byte[] kI = Arrays.copyOfRange(mensajeDescifrado, idLength, mensajeDescifrado.length);
                        System.out.println("[Operación] c_I recibido y descifrado (ID_I: " + new String(idIBytes) + ")");
                        
                        byte[] kR = new byte[16];
                        new SecureRandom().nextBytes(kR);
                        System.out.println("[Operación] Valor k_R generado: " + bytesToHex(kR));
                        
                        cipher.init(Cipher.ENCRYPT_MODE, pkI);
                        byte[] cR = cipher.doFinal(kR);
                        
                        out.writeInt(cR.length);
                        out.write(cR);
                        System.out.println("[Operación] c_R enviado (" + cR.length + " bytes).");
                        
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
            System.err.println("Error en el Respondedor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) { sb.append(String.format("%02x", b)); }
        return sb.toString();
    }
}
