import java.math.BigInteger;
import java.security.SecureRandom; // IMPORTANTE: Añadir esta línea
import java.util.Scanner;

public class CurvaEliptica {
    static class Punto {
        BigInteger x, y, z;
        public static final Punto INFINITO = new Punto(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO);

        Punto(BigInteger x, BigInteger y, BigInteger z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            if (this.z.equals(BigInteger.ZERO)) return "Infinito (O)";
            return "(" + x + ", " + y + ")";
        }
    }

    // Parámetros de la curva: y^2 = x^3 + ax + b (mod p)
    private static BigInteger p, a, b;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SecureRandom random = new SecureRandom(); // Generador criptográfico

        System.out.println("--- Parámetros Globales (ECDH) ---");
        System.out.print("Ingrese p: "); p = new BigInteger(sc.next());
        System.out.print("Ingrese a: "); a = new BigInteger(sc.next());
        System.out.print("Ingrese b: "); b = new BigInteger(sc.next());

        System.out.print("Ingrese Gx: "); BigInteger gx = new BigInteger(sc.next());
        System.out.print("Ingrese Gy: "); BigInteger gy = new BigInteger(sc.next());
        Punto G = new Punto(gx, gy, BigInteger.ONE);

        // Pasos 3 y 4: Elección de exponentes aleatorios y cálculo de llaves públicas
        BigInteger kA = new BigInteger(p.bitLength(), random).mod(p); // kA aleatorio
        BigInteger kB = new BigInteger(p.bitLength(), random).mod(p); // kB aleatorio

        System.out.println("\n[SIMULACIÓN]");
        System.out.println("Alice elige kA (secreta): " + kA);
        System.out.println("Bob elige kB (secreta): " + kB);

        Punto A = RTL(G, kA); // Alice calcula A = kA * G
        Punto B = RTL(G, kB); // Bob calcula B = kB * G

        System.out.println("Alice envía A a Bob: " + A);
        System.out.println("Bob envía B a Alice: " + B);

        // Pasos 5 y 6: Cálculo del secreto compartido
        Punto secretoAlice = RTL(B, kA); // kA * B
        Punto secretoBob = RTL(A, kB);   // kB * A

        System.out.println("\n--- Resultados del Protocolo ---");
        System.out.println("Secreto calculado por Alice: " + secretoAlice);
        System.out.println("Secreto calculado por Bob:   " + secretoBob);

        // Paso 7: Verificación de igualdad
        if (secretoAlice.x.equals(secretoBob.x) && secretoAlice.y.equals(secretoBob.y)) {
            System.out.println("¡ÉXITO! Los secretos coinciden.");
        } else {
            System.out.println("ERROR: Los secretos no coinciden.");
        }
    }

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

    // --- ALGORITMO 2: Left-to-Right ---
    public static Punto LTR(Punto P_ingresado, BigInteger k) {

        Punto Q = Punto.INFINITO;
        int t = k.bitLength();

        for (int i = t - 1; i >= 0; i--) {
            Q = doblarPunto(Q);
            if (k.testBit(i)) {
                Q = sumarPuntos(Q, P_ingresado);
            }
        }
        return Q;
    }

    // --- Lógica Matemática (Traducida de tus ejercicios) ---

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

    public static Punto doblarPunto(Punto P) {
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