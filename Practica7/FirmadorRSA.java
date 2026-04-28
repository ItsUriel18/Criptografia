import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FirmadorRSA {

    public static void main(String[] args) {
        try {
            // Mensaje sugerido para el archivo txt
            String mensaje = "La criptografía es la base de la confianza en el mundo digital. ESCOM 2026.";
            Files.write(Paths.get("mensaje.txt"), mensaje.getBytes());

            // CORRECCIÓN: Leer el contenido del archivo antes de pasarlo
            String contenidoPrivada = new String(Files.readAllBytes(Paths.get("private_key.txt")));
            
            System.out.println("--- Firmando archivo ---");
            String firma = firmarArchivo("mensaje.txt", contenidoPrivada);

            System.out.println("\n--- Verificando firma ---");
            String contenidoPublica = new String(Files.readAllBytes(Paths.get("public_key.txt")));
            boolean esValida = verificarFirma("mensaje.txt", contenidoPublica, firma);
            
            System.out.println("¿La firma es válida?: " + esValida);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // PUNTO 2: Función para firmar con CRT 
    public static String firmarArchivo(String fileName, String privateKeyBase64) throws Exception {
        byte[] messageBytes = Files.readAllBytes(Paths.get(fileName));
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger h = new BigInteger(1, md.digest(messageBytes));

        String decodedKey = new String(Base64.getDecoder().decode(privateKeyBase64));
        String[] parts = decodedKey.split(":");
        
        // p, q, dP, dQ, qInv extraídos para el cálculo CRT
        BigInteger p = new BigInteger(parts[3]);
        BigInteger q = new BigInteger(parts[4]);
        BigInteger dP = new BigInteger(parts[5]);
        BigInteger dQ = new BigInteger(parts[6]);
        BigInteger qInv = new BigInteger(parts[7]);

        // Fórmulas de Garner (CRT)
        BigInteger s1 = h.modPow(dP, p);
        BigInteger s2 = h.modPow(dQ, q);
        BigInteger h_crt = qInv.multiply(s1.subtract(s2)).mod(p);
        BigInteger s = s2.add(h_crt.multiply(q));

        String signatureBase64 = Base64.getEncoder().encodeToString(s.toByteArray());
        System.out.println("Firma s (Base64): " + signatureBase64); 
        return signatureBase64;
    }

    public static boolean verificarFirma(String fileName, String publicKeyBase64, String sBase64) throws Exception {
        // 1. Obtener hash h' del mensaje actual 
        byte[] messageBytes = Files.readAllBytes(Paths.get(fileName));
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hPrime = new BigInteger(1, md.digest(messageBytes));

        // 2. Recuperar n y e de la llave pública
        String decodedPub = new String(Base64.getDecoder().decode(publicKeyBase64));
        String[] parts = decodedPub.split(":");
        BigInteger n = new BigInteger(parts[0]);
        BigInteger e = new BigInteger(parts[1]);

        // 3. Recuperar h haciendo h = s^e mod n 
        BigInteger s = new BigInteger(Base64.getDecoder().decode(sBase64));
        BigInteger hRecovered = s.modPow(e, n);

        // 4. Comparar y retornar 
        return hPrime.equals(hRecovered);
    }
}
