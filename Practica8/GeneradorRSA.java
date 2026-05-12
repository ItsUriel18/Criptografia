import java.security.KeyPair;
import java.security.KeyPairGenerator; 
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class GeneradorRSA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.println("--- Generador de Llaves RSA (NIST SP800-186) ---");
            
            System.out.print("Nombre para el archivo de la llave PRIVADA: ");
            String privFileName = sc.nextLine();
            System.out.print("Nombre para el archivo de la llave PÚBLICA: ");
            String pubFileName = sc.nextLine();

            // Generación de llaves de 2048 bits
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); 
            KeyPair pair = keyGen.generateKeyPair();
            
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();

            // Almacenamiento en Base64
            String privBase64 = Base64.getEncoder().encodeToString(priv.getEncoded());
            String pubBase64 = Base64.getEncoder().encodeToString(pub.getEncoded());

            Files.write(Paths.get(privFileName), privBase64.getBytes());
            Files.write(Paths.get(pubFileName), pubBase64.getBytes());

            System.out.println("\nLlaves generadas exitosamente en " + privFileName + " y " + pubFileName);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}