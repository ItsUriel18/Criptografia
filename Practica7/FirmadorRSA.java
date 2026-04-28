import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner; // Nueva importación

public class FirmadorRSA {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.println("\n--- FIRMA RSA-CRT ---");
            System.out.println("1. Firmar un archivo ");
            System.out.println("2. Verificar firma de un compañero ");
            System.out.print("Selecciona una opción: ");
            int opcion = Integer.parseInt(sc.nextLine());

            if (opcion == 1) {
                //  PROCESO DE FIRMA 
                System.out.print("\nNombre del archivo a firmar: ");
                String archivo = sc.nextLine();
                System.out.print("Archivo de la LLAVE PRIVADA: ");
                String llavePrivFile = sc.nextLine();

                String contenidoPrivada = new String(Files.readAllBytes(Paths.get(llavePrivFile)));
                String firma = firmarArchivo(archivo, contenidoPrivada);
                System.out.println("Firma: " + firma);
                System.out.println("\n");

            } else if (opcion == 2) {
                // PROCESO DE VERIFICACIÓN 
                System.out.print("Nombre del archivo a validar: ");
                String archivo = sc.nextLine();
                System.out.print("Archivo de la LLAVE PÚBLICA: ");
                String llavePubFile = sc.nextLine();
                System.out.print("Firma (Base64): ");
                String firmaSocio = sc.nextLine();

                String contenidoPublica = new String(Files.readAllBytes(Paths.get(llavePubFile)));
                boolean esValida = verificarFirma(archivo, contenidoPublica, firmaSocio);

                System.out.println("\n-------------------------------------");
                if (esValida) {
                    System.out.println("Firma VALIDA");
                } else {
                    System.out.println("La firma es INVÁLIDA");
                }
                System.out.println("-------------------------------------\n");
            } else {
                System.out.println("Opción no válida.");
            }

        } catch (Exception e) {
            System.err.println("Error en el proceso: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    // Las funciones firmarArchivo y verificarFirma se mantienen igual que las tenías
    public static String firmarArchivo(String fileName, String privateKeyBase64) throws Exception {
        byte[] messageBytes = Files.readAllBytes(Paths.get(fileName));
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger h = new BigInteger(1, md.digest(messageBytes));

        String decodedKey = new String(Base64.getDecoder().decode(privateKeyBase64));
        String[] parts = decodedKey.split(":");
        
        BigInteger p = new BigInteger(parts[3]);
        BigInteger q = new BigInteger(parts[4]);
        BigInteger dP = new BigInteger(parts[5]);
        BigInteger dQ = new BigInteger(parts[6]);
        BigInteger qInv = new BigInteger(parts[7]);

        BigInteger s1 = h.modPow(dP, p);
        BigInteger s2 = h.modPow(dQ, q);
        BigInteger h_crt = qInv.multiply(s1.subtract(s2)).mod(p);
        BigInteger s = s2.add(h_crt.multiply(q));

        return Base64.getEncoder().encodeToString(s.toByteArray());
    }

    public static boolean verificarFirma(String fileName, String publicKeyBase64, String sBase64) throws Exception {
        byte[] messageBytes = Files.readAllBytes(Paths.get(fileName));
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hPrime = new BigInteger(1, md.digest(messageBytes));

        String decodedPub = new String(Base64.getDecoder().decode(publicKeyBase64));
        String[] parts = decodedPub.split(":");
        BigInteger n = new BigInteger(parts[0]);
        BigInteger e = new BigInteger(parts[1]);

        BigInteger s = new BigInteger(1, Base64.getDecoder().decode(sBase64)); // El '1' asegura signo positivo
        BigInteger hRecovered = s.modPow(e, n);

        return hPrime.equals(hRecovered);
    }
}
