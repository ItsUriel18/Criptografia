import java.math.BigInteger;
import java.security.SecureRandom;

public class EXCH {

    // Genera un escalar secreto aleatorio 'k' tal que: 1 <= k < q
    public static BigInteger generarEscalarSecreto() {
        SecureRandom sr = new SecureRandom();
        BigInteger secreto;
        do {
            // Generamos un número aleatorio con la misma cantidad de bits que 'q'
            secreto = new BigInteger(CurvaEliptica.q.bitLength(), sr);
        } while (secreto.compareTo(BigInteger.ONE) < 0 || secreto.compareTo(CurvaEliptica.q) >= 0);
        
        return secreto;
    }

    // Calcula el punto público (Escalar * Punto Generador G)
    public static Punto calcularPuntoPublico(BigInteger escalarSecreto) {
        return CurvaEliptica.RTL(CurvaEliptica.G, escalarSecreto);
    }

    // Calcula el secreto compartido (Escalar Secreto Local * Punto Público Recibido)
    public static Punto calcularSecretoCompartido(Punto puntoPublicoRecibido, BigInteger escalarSecretoLocal) {
        return CurvaEliptica.RTL(puntoPublicoRecibido, escalarSecretoLocal);
    }
}