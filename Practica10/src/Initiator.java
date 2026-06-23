import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import java.util.Base64;

public class Initiator {
    private static final String HOST = "192.168.100.12"; 
    private static final int PORT = 65432;
    private static final String ID_I = "Iniciador_01";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SIMULADOR SKEME - INICIADOR");
        System.out.println("========================================");
        
        try (Socket socket = new Socket(HOST, PORT)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            
            KeyPair pairI = null;
            PublicKey pkR = null;
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
                        pairI = SHARE.generarLlavesRSA(512);
                        byte[] pkIBytes = pairI.getPublic().getEncoded();
                        
                        System.out.println("[Operación] Mi llave pública generada (Base64): " + Base64.getEncoder().encodeToString(pkIBytes).substring(0, 50) + "...");
                        out.writeInt(pkIBytes.length);
                        out.write(pkIBytes);
                        
                        int pkRLength = in.readInt();
                        byte[] pkRBytes = new byte[pkRLength];
                        in.readFully(pkRBytes);
                        pkR = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pkRBytes));
                        System.out.println("[Operación] Llave del Respondedor recibida y almacenada.");
                        break;
                        
                    case 2:
                        if (pkR == null) { System.out.println("[Alerta] Ejecuta el Paso 1 primero."); break; }
                        System.out.println("=== FASE SHARE ===");
                        
                        byte[] kI = SHARE.generarAleatorio(16);
                        System.out.println("[Operación] Valor k_I generado: " + SHARE.bytesToHex(kI));
                        
                        // Concatenar ID_I y k_I
                        byte[] idBytes = ID_I.getBytes();
                        byte[] mensaje = new byte[idBytes.length + kI.length];
                        System.arraycopy(idBytes, 0, mensaje, 0, idBytes.length);
                        System.arraycopy(kI, 0, mensaje, idBytes.length, kI.length);
                        
                        byte[] cI = SHARE.cifrarRSA(mensaje, pkR);
                        out.writeInt(cI.length);
                        out.write(cI);
                        System.out.println("[Operación] c_I enviado. Esperando c_R...");
                        
                        int cRLength = in.readInt();
                        byte[] cR = new byte[cRLength];
                        in.readFully(cR);
                        
                        byte[] kR = SHARE.descifrarRSA(cR, pairI.getPrivate());
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