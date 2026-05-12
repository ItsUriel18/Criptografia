import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class FirmadorRSA {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.println("\n--- SISTEMA DE FIRMA RSASSA-PSS ---");
            System.out.println("1. Generar firma");
            System.out.println("2. Verificar firma");
            System.out.print("Selecciona una opción: ");
            int opcion = Integer.parseInt(sc.nextLine());

            if (opcion == 1) {
                System.out.print("\nArchivo a firmar:");
                String archivo = sc.nextLine();
                System.out.print("\nArchivo de llave privada: ");
                String keyFile = sc.nextLine();

                String firma = firmarPSS(archivo, keyFile);
                System.out.println("\nFirma generada (Base64):\n" + firma);

            } else if (opcion == 2) {
                System.out.print("\nArchivo original: ");
                String archivo = sc.nextLine();
                System.out.print("\nArchivo de llave pública: ");
                String keyFile = sc.nextLine();
                System.out.print("\nPegue la firma (Base64): ");
                String firmaStr = sc.nextLine();

                boolean esValida = verificarPSS(archivo, keyFile, firmaStr);
                System.out.println("\nResultado: " + (esValida ? "FIRMA VÁLIDA\n" : "FIRMA INVÁLIDA\n"));
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    public static String firmarPSS(String filePath, String keyPath) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        byte[] keyBytes = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(keyPath)));

        // Reconstruir llave privada
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(spec);

        // Configurar Firma PSS
        Signature sig = Signature.getInstance("RSASSA-PSS");
        // Parametrización según estándar (SHA-256, MGF1, 32 bytes salt)
        PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1", 
                                   MGF1ParameterSpec.SHA256, 32, 1);
        sig.setParameter(pssSpec);
        
        sig.initSign(privKey);
        sig.update(data);
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public static boolean verificarPSS(String filePath, String keyPath, String sigBase64) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        byte[] sigBytes = Base64.getDecoder().decode(sigBase64);
        byte[] keyBytes = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(keyPath)));

        // Reconstruir llave pública
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pubKey = kf.generatePublic(spec);

        // Configurar Verificación PSS
        Signature sig = Signature.getInstance("RSASSA-PSS");
        PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1", 
                                   MGF1ParameterSpec.SHA256, 32, 1);
        sig.setParameter(pssSpec);

        sig.initVerify(pubKey);
        sig.update(data);
        return sig.verify(sigBytes);
    }
}
