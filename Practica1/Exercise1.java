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
        List<int[]> curvasValidas = buscarCurvasNoSingulares(p.intValue());

        System.out.println("Se encontraron " + curvasValidas.size() + " curvas no singulares.");
        System.out.println("Primeros 5 resultados (a, b):");
        for (int i = 0; i < Math.min(5, curvasValidas.size()); i++) {
            int[] datos = curvasValidas.get(i);
            System.out.println("a = " + datos[0] + " ; b = " + datos[1] + " ; Delta = " + datos[2]);
        }
    }

    public static List<int[]> buscarCurvasNoSingulares(int p) {
        List<int[]> pares = new ArrayList<>();

        for (int a = 0; a < p; a++) {
            for (int b = 0; b < p; b++) {
                // Condicion: (4a^3 + 27b^2) mod p != 0
                int discriminante = (4 * a * a * a + 27 * b * b) % p;
                
                if (discriminante != 0) {
                    pares.add(new int[]{a, b, discriminante});
                }
            }
        }
        return pares;
    }
}