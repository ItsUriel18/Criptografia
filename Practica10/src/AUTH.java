import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AUTH {

    // Genera el código HMAC para un conjunto de datos usando la clave k_mac
    public static byte[] calcularHMAC(byte[] datos, byte[] kMac) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(kMac, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        return mac.doFinal(datos);
    }

    // Prepara el bloque de bytes a firmar concatenando los elementos en el orden especificado
    public static byte[] concatenarDatos(Punto p1, Punto p2, String id1, String id2) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Escribimos las coordenadas en bytes
        baos.write(p1.x.toByteArray());
        baos.write(p1.y.toByteArray());
        baos.write(p2.x.toByteArray());
        baos.write(p2.y.toByteArray());
        // Escribimos los identificadores
        baos.write(id1.getBytes());
        baos.write(id2.getBytes());
        
        return baos.toByteArray();
    }

    // Deriva k_sess calculando el hash SHA-256 de la coordenada X del secreto compartido
    public static byte[] derivarKSess(BigInteger coordenadaX) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(coordenadaX.toByteArray());
    }
}