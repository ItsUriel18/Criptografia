import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Exercise1 {

    public static void main(String[] args) {
        // 1. Generar un número primo p > 3 usando la librería estándar
        SecureRandom random = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(8, random); // 8 bits para que no tarde milenios
        
        while (p.compareTo(BigInteger.valueOf(3)) <= 0) {
            p = BigInteger.probablePrime(8, random);
        }

        System.out.println("Primo p generado: " + p);
        
        // 2. Encontrar todos los pares (a, b) que forman curvas no singulares
        List<int[]> validCurves = findNonSingularCurves(p.intValue());

        System.out.println("Se encontraron " + validCurves.size() + " curvas no singulares.");
        System.out.println("Primeros 10 resultados (a, b):");
        for (int i = 0; i < Math.min(10, validCurves.size()); i++) {
            System.out.println("a: " + validCurves.get(i)[0] + ", b: " + validCurves.get(i)[1]);
        }
    }

    public static List<int[]> findNonSingularCurves(int p) {
        List<int[]> pairs = new ArrayList<>();

        for (int a = 0; a < p; a++) {
            for (int b = 0; b < p; b++) {
                // Condición: (4a^3 + 27b^2) mod p != 0
                int discriminant = (4 * a * a * a + 27 * b * b) % p;
                
                if (discriminant != 0) {
                    pairs.add(new int[]{a, b});
                }
            }
        }
        return pairs;
    }
}