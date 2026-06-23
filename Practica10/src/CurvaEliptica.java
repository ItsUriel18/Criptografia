import java.math.BigInteger;

public class CurvaEliptica {

    // PARÁMETROS DE LA CURVA: y^2 = x^3 + 1006x + 4 mod 1009

    public static BigInteger p = new BigInteger("1009");
    public static BigInteger a = new BigInteger("1006");
    public static BigInteger b = new BigInteger("4");
    public static BigInteger q = new BigInteger("967");
    
    // Punto Generador

    public static Punto G = new Punto(
        new BigInteger("256"), 
        new BigInteger("155"), 
        BigInteger.ONE
    );

    // --- ALGORITMO 1: Right-to-Left ---
    public static Punto RTL(Punto P_ingresado, BigInteger k) {
        Punto Q = Punto.INFINITO;
        Punto P_aux = P_ingresado;
        int t = k.bitLength();

        for (int i = 0; i < t; i++) {
            if (k.testBit(i)) { // Si el bit i-ésimo es 1
                Q = sumarPuntos(Q, P_aux);
            }
            P_aux = doblarPunto(P_aux);
        }
        return Q;
    }

    // --- SUMA DE PUNTOS ---
    public static Punto sumarPuntos(Punto P1, Punto P2) {
        if (P1.z.equals(BigInteger.ZERO)) return P2;
        if (P2.z.equals(BigInteger.ZERO)) return P1;

        if (P1.x.equals(P2.x) && !P1.y.equals(P2.y)) return Punto.INFINITO;
        if (P1.x.equals(P2.x) && P1.y.equals(P2.y)) return doblarPunto(P1);

        // m = (y2 - y1) * inv(x2 - x1) mod p
        BigInteger num = P2.y.subtract(P1.y).mod(p);
        BigInteger den = P2.x.subtract(P1.x).mod(p);
        BigInteger m = num.multiply(den.modInverse(p)).mod(p);

        // x3 = m^2 - x1 - x2 mod p
        BigInteger x3 = m.pow(2).subtract(P1.x).subtract(P2.x).mod(p);
        // y3 = m(x1 - x3) - y1 mod p
        BigInteger y3 = m.multiply(P1.x.subtract(x3)).subtract(P1.y).mod(p);

        return new Punto(x3, y3, BigInteger.ONE);
    }

    // --- DOBLADO DE PUNTO ---
    public static Punto doblarPunto(Punto P) {
        // Doblar el Infinito o un punto con y=0 da Infinito
        if (P.z.equals(BigInteger.ZERO) || P.y.equals(BigInteger.ZERO)) return Punto.INFINITO;

        // m = (3x^2 + a) * inv(2y) mod p
        BigInteger num = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a).mod(p);
        BigInteger den = P.y.multiply(BigInteger.valueOf(2)).mod(p);
        BigInteger m = num.multiply(den.modInverse(p)).mod(p);

        // x3 = m^2 - 2x mod p
        BigInteger x3 = m.pow(2).subtract(P.x.multiply(BigInteger.valueOf(2))).mod(p);
        // y3 = m(x1 - x3) - y1 mod p
        BigInteger y3 = m.multiply(P.x.subtract(x3)).subtract(P.y).mod(p);

        return new Punto(x3, y3, BigInteger.ONE);
    }
}
