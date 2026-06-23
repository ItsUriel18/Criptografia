import java.security.*; //Generadores de llaves, generación de números aleatorios y funciones hash.
import javax.crypto.Cipher; //Procesos de cifrado y descifrado

public class SHARE {

    // Generar llaves RSA
    public static KeyPair generarLlavesRSA(int size) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(size);
        return keyGen.generateKeyPair();
    }

    // Generar k_I y k_R
    public static byte[] generarAleatorio(int numBytes) {
        byte[] aleatorio = new byte[numBytes];
        new SecureRandom().nextBytes(aleatorio);
        return aleatorio;
    }

    // Cifra un mensaje con la clave pública RSA (Para c_I y c_R)
    public static byte[] cifrarRSA(byte[] mensaje, PublicKey pk) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        return cipher.doFinal(mensaje);
    }

    // Descifra un criptograma con la clave privada RSA
    public static byte[] descifrarRSA(byte[] criptograma, PrivateKey sk) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, sk);
        return cipher.doFinal(criptograma);
    }

    // Calcula SHA-256 de la concatenación de dos arreglos de bytes (k_mac)
    public static byte[] calcularHashSHA256(byte[] k1, byte[] k2) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(k1);
        md.update(k2);
        return md.digest();
    }

    // Convierte bytes a formato Hexadecimal para imprimir en consola
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) { sb.append(String.format("%02x", b)); }
        return sb.toString();
    }
}
