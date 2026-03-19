import java.math.BigInteger;
import java.security.SecureRandom;

public class Exercise1 {

    public static void main(String[] args) {
        // 1. Generar un número primo p > 3 usando la librería estándar
        SecureRandom random = new SecureRandom();
        BigInteger p = new BigInteger(8, 100, random); // 8 bits para que no tarde milenios
        
        while (p.compareTo(BigInteger.valueOf(3)) <= 0) {
            p = BigInteger.probablePrime(8, random);
        }

        System.out.println("Primo p generado: " + p);
        
        BigInteger resultado = curvasNoSingulares(p.intValue());
        System.out.println("Número total de curvas no singulares: " + resultado);

        BigInteger[] curva = generarCoeficientes(p);
        System.out.println("Curva generada aleatoriamente: a=" + curva[0] + ", b=" + curva[1]);

        // Verificación manual de una curva conocida (ej. a=1, b=1)
        boolean check = esCurvaValida(BigInteger.ONE, BigInteger.ONE, p);
        System.out.println("¿Es y^2 = x^3 + x + 1 válida?: " + check);

        
    }

    public static BigInteger curvasNoSingulares(int p){
        BigInteger primo = BigInteger.valueOf(p);
        BigInteger resultado = primo.multiply(primo).subtract(primo); // p^2 - p
        return resultado;
    }

    /**
     * 2. MÉTODO DE VERIFICACIÓN (Validador)
     * Comprueba si un par (a, b) genera una curva no singular: (4a^3 + 27b^2) mod p != 0.
     */
    public static boolean esCurvaValida(BigInteger a, BigInteger b, BigInteger p) {
        BigInteger cuatro = BigInteger.valueOf(4);
        BigInteger veintisiete = BigInteger.valueOf(27);

        // Delta = (4 * a^3 + 27 * b^2) % p
        BigInteger aCubo = a.multiply(a).multiply(a);
        BigInteger bCuadrado = b.multiply(b);
        
        BigInteger discriminante = cuatro.multiply(aCubo)
                                        .add(veintisiete.multiply(bCuadrado))
                                        .mod(p);

        // Es válida si el discriminante NO es cero
        return !discriminante.equals(BigInteger.ZERO);
    }

    /**
     * 3. MÉTODO DE GENERACIÓN (Aleatorio)
     * Crea valores a y b al azar y usa el verificador para asegurar validez.
     */

    public static BigInteger[] generarCoeficientes(BigInteger p) {
        SecureRandom random = new SecureRandom();
        BigInteger a, b;

        do {
            // Generamos números aleatorios menores a p
            a = new BigInteger(p.bitLength(), random).mod(p);
            b = new BigInteger(p.bitLength(), random).mod(p);

            // Usamos nuestro método de verificación
        } while (!esCurvaValida(a, b, p));

        return new BigInteger[]{a, b};
    }


}