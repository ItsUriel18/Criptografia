import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Scanner;

public class DSA {

    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {
        // 1. Generar p y q
        BigInteger[] pq = generatePrimes();
        BigInteger p = pq[0];
        BigInteger q = pq[1];

        System.out.println("--- Parámetros Globales ---");
        System.out.println("q: " + q);
        System.out.println("p (kq + 1): " + p);

        // 2. Encontrar el generador g
        BigInteger g = findGenerator(p, q);
        System.out.println("g (generador): " + g);

        // 3. Generar el par de llaves
        DSAKeyPair keys = generateKeyPair(p, q, g);
        
        System.out.println("\n--- Llaves Generadas ---");
        System.out.println("Llave Privada (d): " + keys.privateKey);
        System.out.println("Llave Pública (q: " + q + ", p: " + p + ", g: " + g + ", beta: " + keys.publicKeyBeta + ")");

    // 2. Pedir m al usuario para asegurar que sea menor que q
        Scanner sc = new Scanner(System.in);
        System.out.print("\nIntroduce un valor para m (debe ser menor a " + q + "): ");
        BigInteger m = sc.nextBigInteger();
        sc.close();

        if (m.compareTo(q) >= 0 || m.compareTo(BigInteger.ONE) < 0) {
            System.out.println("¡Error! m debe estar entre 1 y " + (q.subtract(BigInteger.ONE)) + ".");
            return;
        }

        // Generar la firma (r, s)
        BigInteger[] signature = generateSignature(m, p, q, g, keys.privateKey);
        BigInteger r = signature[0];
        BigInteger s = signature[1];

        // (d) Imprimir la firma (r, s)
        System.out.println("Firma Digital:");
        System.out.println("  r: " + r);
        System.out.println("  s: " + s);
    }

    /**
     * Paso 1: Generar números primos p y q.
     * q es un primo entre 11 y 1024.
     * p = kq + 1 es un primo.
     */
    public static BigInteger[] generatePrimes() {
        BigInteger q, p;
        // Encontrar un q primo entre 11 y 1024
        do {
            // Generamos un número de aprox 10 bits para estar en el rango
            q = new BigInteger(10, 100, random); 
        } while (q.compareTo(BigInteger.valueOf(11)) < 0 || q.compareTo(BigInteger.valueOf(1024)) > 0);

        // Intentar diferentes valores de k para encontrar p = kq + 1
        long k = 2;
        while (true) {
            p = q.multiply(BigInteger.valueOf(k)).add(BigInteger.ONE);
            // El segundo parámetro de isProbablePrime es la certeza (100 es muy seguro)
            if (p.isProbablePrime(100)) {
                return new BigInteger[]{p, q};
            }
            k++;
        }
    }

    /**
     * Paso 2: Encontrar un generador g de orden q.
     */
    public static BigInteger findGenerator(BigInteger p, BigInteger q) {
        BigInteger e = p.subtract(BigInteger.ONE).divide(q);
        BigInteger g;

        while (true) {
            // (b) Generar h aleatorio 1 <= h <= p-1
            BigInteger h = new BigInteger(p.bitLength(), random);
            if (h.compareTo(BigInteger.ONE) <= 0 || h.compareTo(p.subtract(BigInteger.ONE)) >= 0) continue;

            // (c) g = h^e mod p
            g = h.modPow(e, p);

            // (d) Verificar 2 <= g <= p-1
            if (g.compareTo(BigInteger.valueOf(2)) < 0) continue;

            // (e) Verificar g^q mod p = 1
            if (g.modPow(q, p).equals(BigInteger.ONE)) {
                return g;
            }
        }
    }

    /**
     * Paso 3: Generar el par de llaves.
     */
    public static DSAKeyPair generateKeyPair(BigInteger p, BigInteger q, BigInteger g) {
        // Elegir d aleatorio 0 < d < q
        BigInteger d;
        do {
            d = new BigInteger(q.bitLength(), random);
        } while (d.compareTo(BigInteger.ZERO) <= 0 || d.compareTo(q) >= 0);

        // Calcular beta = g^d mod p
        BigInteger beta = g.modPow(d, p);

        return new DSAKeyPair(d, beta);
    }

    // Clase auxiliar para guardar las llaves
    static class DSAKeyPair {
        BigInteger privateKey; // d
        BigInteger publicKeyBeta; // beta

        DSAKeyPair(BigInteger d, BigInteger beta) {
            this.privateKey = d;
            this.publicKeyBeta = beta;
        }
    }

    /**
 * Genera la firma digital (r, s) para un mensaje m.
 * @param m El mensaje (entero entre 1 y q-1)
 * @param p Parámetro público
 * @param q Parámetro público
 * @param g Generador
 * @param d Llave privada
 * @return Un arreglo con [r, s]
 */
    public static BigInteger[] generateSignature(BigInteger m, BigInteger p, BigInteger q, BigInteger g, BigInteger d) {
        BigInteger r = BigInteger.ZERO; 
        BigInteger s = BigInteger.ZERO;
        BigInteger kE;
        
        do {
            // (a) Elegir una llave secreta aleatoria 0 < Ke < q
            do {
                kE = new BigInteger(q.bitLength(), random);
            } while (kE.compareTo(BigInteger.ZERO) <= 0 || kE.compareTo(q) >= 0);

            // (b) Calcular r = (g^Ke mod p) mod q
            r = g.modPow(kE, p).mod(q);

            // Si r es 0, no es una firma válida, hay que repetir con otra Ke
            if (r.equals(BigInteger.ZERO)) continue;

            // (c) Calcular s = (m + dr) * Ke^-1 mod q
            // Primero calculamos el inverso modular de Ke respecto a q
            BigInteger kEInv = kE.modInverse(q);
            
            // s = (m + (d * r)) * kEInv mod q
            s = m.add(d.multiply(r)).multiply(kEInv).mod(q);

        } while (s.equals(BigInteger.ZERO)); // Si s es 0, se repite el proceso

        return new BigInteger[]{r, s};
    }
}