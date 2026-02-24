import java.math.BigInteger;

public class Excercise4 {

    public static void main(String[] args) {
        // Parámetros de la curva 
        BigInteger p = BigInteger.valueOf(13);
        BigInteger a = BigInteger.valueOf(0);
        
        // Puntos
        BigInteger[] P = {BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(1)}; 
        BigInteger[] Q = {BigInteger.valueOf(0), BigInteger.valueOf(12), BigInteger.valueOf(1)};

        BigInteger[] R = sumarPuntos(P, Q, a, p);

        System.out.println("P + Q = (" + R[0] + ", " + R[1] + ", " + R[2] + ")");
    }

    public static BigInteger[] sumarPuntos(BigInteger[] P, BigInteger[] Q, BigInteger a, BigInteger p) {
        // Manejo del punto al infinito (Elemento neutro)
        if (P[2].equals(BigInteger.ZERO)) return Q;
        if (Q[2].equals(BigInteger.ZERO)) return P;

        // Si son inversos (x1 == x2 y y1 == -y2), el resultado es el infinito
        if (P[0].equals(Q[0]) && !P[1].equals(Q[1])) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO};
        }

        // 1. Calcular la pendiente m = (y2 - y1) * inv(x2 - x1) mod p
        BigInteger num = Q[1].subtract(P[1]).mod(p);
        BigInteger den = Q[0].subtract(P[0]).mod(p);
        
        // Inverso multiplicativo modular
        BigInteger pendiente = num.multiply(den.modInverse(p)).mod(p);

        // 2. Calcular x3 = m^2 - x1 - x2 mod p
        BigInteger x3 = pendiente.pow(2).subtract(P[0]).subtract(Q[0]).mod(p);
        if (x3.signum() < 0) x3 = x3.add(p); // Ajuste positivo

        // 3. Calcular y3 = m(x1 - x3) - y1 mod p
        BigInteger y3 = pendiente.multiply(P[0].subtract(x3)).subtract(P[1]).mod(p);
        if (y3.signum() < 0) y3 = y3.add(p); // Ajuste positivo

        return new BigInteger[]{x3, y3, BigInteger.ONE};
    }
}