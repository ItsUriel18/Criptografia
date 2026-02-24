import java.math.BigInteger;
import java.security.SecureRandom;

public class Exercise2 {

    public static void main(String[] args) {
        int bits = 16; 
        System.out.println("Generando curva para " + bits + " bits...");
        
        // Ejecutamos la función del ejercicio 2
        BigInteger[] resultado = generarCurvaPorBits(bits);
        
        System.out.println("--- Resultado ---");
        System.out.println("Primo p (" + bits + " bits): " + resultado[0]);
        System.out.println("Valor a: " + resultado[1]);
        System.out.println("Valor b: " + resultado[2]);
    }

    /**
     * Solución al Ejercicio 2:
     * Recibe el tamaño en bits y retorna [p, a, b] de una curva no-singular.
     */
    public static BigInteger[] generarCurvaPorBits(int bits) {
        SecureRandom random = new SecureRandom();
        
        // 1. Generar el primo p de N bits
        BigInteger p = BigInteger.probablePrime(bits, random);
        
        // Asegurar que p > 3
        while (p.compareTo(BigInteger.valueOf(3)) <= 0) {
            p = BigInteger.probablePrime(bits, random);
        }

        BigInteger a, b;
        boolean esSingular = true;
        
        // 2. Buscar aleatoriamente a y b hasta que la curva sea no-singular
        do {
            // a y b deben estar en Zp, así que generamos números menores a p
            a = new BigInteger(bits, random).mod(p);
            b = new BigInteger(bits, random).mod(p);
            
            // Verificamos la condición: 4a^3 + 27b^2 != 0 (mod p)
            if (esNoSingular(a, b, p)) {
                esSingular = false;
            }
        } while (esSingular);

        return new BigInteger[]{p, a, b};
    }

    // Función auxiliar para verificar la condición matemática con BigInteger
    public static boolean esNoSingular(BigInteger a, BigInteger b, BigInteger p) {
        BigInteger cuatro = BigInteger.valueOf(4);
        BigInteger veintisiete = BigInteger.valueOf(27);
        
        // Delta = (4 * a^3 + 27 * b^2) mod p
        BigInteger term1 = cuatro.multiply(a.pow(3));
        BigInteger term2 = veintisiete.multiply(b.pow(2));
        BigInteger delta = term1.add(term2).mod(p);
        
        return !delta.equals(BigInteger.ZERO);
    }
}