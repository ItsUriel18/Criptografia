import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
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
            Socket socket = serverSocket.accept();
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            
            KeyPair pairR = null;
            PublicKey pkI = null;
            byte[] kMac = null;
            
            boolean continuar = true;
            while (continuar) {
                System.out.println("[1] Generar llaves RSA");
                System.out.println("[2] Fase SHARE");
                
                int opcion = scanner.nextInt();
                System.out.println();
                
                switch (opcion) {
                    case 1:
                        System.out.println("=== PASO 0: SETUP RSA ===");
                        pairR = SHARE.generarLlavesRSA(512);
                        byte[] pkRBytes = pairR.getPublic().getEncoded();
                        
                        System.out.println("[Operación] Mi llave pública generada (Base64): " + Base64.getEncoder().encodeToString(pkRBytes).substring(0, 50) + "...");
                        out.writeInt(pkRBytes.length);
                        out.write(pkRBytes);
                        
                        int pkILength = in.readInt();
                        byte[] pkIBytes = new byte[pkILength];
                        in.readFully(pkIBytes);
                        pkI = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pkIBytes));
                        System.out.println("[Operación] Llave del Iniciador recibida y almacenada.");
                        break;
                        
                    case 2:
                        if (pkI == null) { System.out.println("[Alerta] Ejecuta el Paso 1 primero."); break; }
                        System.out.println("=== FASE SHARE ===");
                        
                        int cILength = in.readInt();
                        byte[] cI = new byte[cILength];
                        in.readFully(cI);
                        
                        byte[] mensajeDescifrado = SHARE.descifrarRSA(cI, pairR.getPrivate());
                        int idLength = mensajeDescifrado.length - 16;
                        byte[] idIBytes = Arrays.copyOfRange(mensajeDescifrado, 0, idLength);
                        byte[] kI = Arrays.copyOfRange(mensajeDescifrado, idLength, mensajeDescifrado.length);
                        System.out.println("[Operación] c_I recibido (ID_I: " + new String(idIBytes) + ")");
                        
                        byte[] kR = SHARE.generarAleatorio(16);
                        System.out.println("[Operación] Valor k_R generado: " + SHARE.bytesToHex(kR));
                        
                        byte[] cR = SHARE.cifrarRSA(kR, pkI);
                        out.writeInt(cR.length);
                        out.write(cR);
                        
                        kMac = SHARE.calcularHashSHA256(kI, kR);
                        System.out.println("[Resultado] Clave compartida (k_mac) derivada: " + SHARE.bytesToHex(kMac));
                        break;
                        
                    default:
                        continuar = false;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
