import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import java.util.Base64;
import java.util.Arrays;

public class Responder {
    private static final int PORT = 65432;

    public static void main(String[] args) {
        System.out.println("\n ==== RESPONDER ==== \n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket socket = serverSocket.accept();
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            
            KeyPair pairR = null;
            PublicKey pkI = null;
            byte[] kMac = null;
            Punto secretoCompartido = null;
            
            boolean continuar = true;
            while (continuar) {
                System.out.println("[1] Generar llaves RSA");
                System.out.println("[2] Fase SHARE");
                System.out.println("[3] Fase EXCH");
                System.out.println("[4] Fase AUTH");
                System.out.println("\n");
                
                int opcion = scanner.nextInt();
                System.out.println();
                
                switch (opcion) {
                    case 1:
                        System.out.println("===  RSA  ===");
                        pairR = SHARE.generarLlavesRSA(512);
                        byte[] pkRBytes = pairR.getPublic().getEncoded();
                        
                        System.out.println("[Operación] Mi llave pública generada (Base64): " + Base64.getEncoder().encodeToString(pkRBytes).substring(0, 50) + "...");
                        out.writeInt(pkRBytes.length);
                        out.write(pkRBytes);
                        
                        int pkILength = in.readInt();
                        byte[] pkIBytes = new byte[pkILength];
                        in.readFully(pkIBytes);
                        pkI = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pkIBytes));
                        System.out.println("[Operación] Llave del Iniciador recibida y almacenada.\n");
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
                        System.out.println("\n");
                        break;

                    case 3:
                        System.out.println("=== FASE EXCH (ECDH) ===");
                        
                        // 1. Recibir las coordenadas del punto X del Iniciador
                        System.out.println("[Operación] Esperando el punto X del Iniciador...");
                        int lenXx = in.readInt();
                        byte[] xxBytes = new byte[lenXx];
                        in.readFully(xxBytes);
                        
                        int lenXy = in.readInt();
                        byte[] xyBytes = new byte[lenXy];
                        in.readFully(xyBytes);
                        
                        Punto X = new Punto(new BigInteger(xxBytes), new BigInteger(xyBytes), BigInteger.ONE);
                        System.out.println("[Operación] Punto público X recibido con éxito.");
                        
                        // 2. Generar el secreto local 'y'
                        BigInteger ySecreto = EXCH.generarEscalarSecreto();
                        System.out.println("[Operación] Secreto local 'y' generado de forma segura.");
                        
                        // 3. Calcular el punto público Y = y * G
                        Punto Y = EXCH.calcularPuntoPublico(ySecreto);
                        System.out.println("[Operación] Mi punto público Y calculado: " + Y.toString());
                        
                        // 4. Enviar las coordenadas de Y al Iniciador por el socket
                        byte[] xCoordY = Y.x.toByteArray();
                        byte[] yCoordY = Y.y.toByteArray();
                        
                        out.writeInt(xCoordY.length);
                        out.write(xCoordY);
                        out.writeInt(yCoordY.length);
                        out.write(yCoordY);
                        System.out.println("[Operación] Punto Y enviado al Iniciador.");
                        
                        // 5. Calcular el secreto compartido final: y * X
                        secretoCompartido = EXCH.calcularSecretoCompartido(X, ySecreto);
                        System.out.println("[Resultado] Secreto compartido ECDH derivado: " + secretoCompartido.toString());
                        break;
                    
                        
                    default:
                        continuar = false;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
