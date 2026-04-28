import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GeneradorRSA {

    public static void main(String[] args) {
        generarLlaves();
    }

    public static void generarLlaves() {
        try {
            SecureRandom random = new SecureRandom();

            int bitlen = 512; // El módulo n debe ser de 1024 bits, por lo tanto p y q son de 512 
            
            BigInteger p = BigInteger.probablePrime(bitlen, random); 
            BigInteger q = BigInteger.probablePrime(bitlen, random); 

            // Cálculo de n y phi

            BigInteger n = p.multiply(q);
            BigInteger pMinus1 = p.subtract(BigInteger.ONE);
            BigInteger qMinus1 = q.subtract(BigInteger.ONE);
            BigInteger phi = pMinus1.multiply(qMinus1);
            
            // Exponente público (e) y cálculo del inverso (d)

            BigInteger e = BigInteger.valueOf(65537);
            BigInteger d = e.modInverse(phi); 

            // --- Componentes para CRT ---
            // Requeridos para el inciso 1.2
            BigInteger dP = d.mod(pMinus1);
            BigInteger dQ = d.mod(qMinus1);
            BigInteger qInv = q.modInverse(p); 

            // 4. Almacenamiento en archivos de texto en Base64
            
            String privateKeyData = n + ":" + e + ":" + d + ":" + p + ":" + q + ":" + dP + ":" + dQ + ":" + qInv;
            String publicKeyData = n + ":" + e;

            String privateBase64 = Base64.getEncoder().encodeToString(privateKeyData.getBytes());
            String publicBase64 = Base64.getEncoder().encodeToString(publicKeyData.getBytes());

            // Guardar archivos
            Files.write(Paths.get("private_key.txt"), privateBase64.getBytes()); 
            Files.write(Paths.get("public_key.txt"), publicBase64.getBytes()); 

            System.out.println("Llaves generadas y guardadas exitosamente.");
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}