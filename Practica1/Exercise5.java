import java.math.BigInteger;

public class Exercise5 {

    public static void main(String[] args) {
        BigInteger p = BigInteger.valueOf(11);
        BigInteger a = BigInteger.valueOf(1);
        BigInteger[] P = {BigInteger.valueOf(10), BigInteger.valueOf(0), BigInteger.valueOf(1)};

        BigInteger[] R = duplicarPunto(P, a, p);

        System.out.println("2P = (" + R[0] + ", " + R[1] + ", " + R[2] + ")");
    }

    public static BigInteger[] duplicarPunto(BigInteger[] P, BigInteger a, BigInteger p) {
        // El doble del infinito es el infinito
        // O si y1 es 0, la tangente es vertical y el resultado es el infinito
        if (P[2].equals(BigInteger.ZERO) || P[1].equals(BigInteger.ZERO)) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO};
        }

        // 2. Calcular la pendiente m = (3*x1^2 + a) * inv(2*y1) mod p
        BigInteger tres = BigInteger.valueOf(3);
        BigInteger dos = BigInteger.valueOf(2);

        // Numerador: (3 * x1^2 + a) mod p
        BigInteger num = tres.multiply(P[0].pow(2)).add(a).mod(p);
        
        // Denominador: (2 * y1) mod p
        BigInteger den = dos.multiply(P[1]).mod(p);
        
        // Inverso modular para la "división"
        BigInteger pendiente = num.multiply(den.modInverse(p)).mod(p);

        // 3. Calcular x3 = m^2 - 2*x1 mod p
        BigInteger x3 = pendiente.pow(2).subtract(dos.multiply(P[0])).mod(p);
        if (x3.signum() < 0) x3 = x3.add(p);

        // 4. Calcular y3 = m(x1 - x3) - y1 mod p
        BigInteger y3 = pendiente.multiply(P[0].subtract(x3)).subtract(P[1]).mod(p);
        if (y3.signum() < 0) y3 = y3.add(p);

        return new BigInteger[]{x3, y3, BigInteger.ONE};
    }
}