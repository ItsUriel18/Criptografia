import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Exercise3 {

    public static void main(String[] args) {
        // Ejemplo con valores pequeños para visualizar
        BigInteger p = BigInteger.valueOf(251); 
        BigInteger a = BigInteger.valueOf(0);
        BigInteger b = BigInteger.valueOf(1);

        System.out.println("Buscando puntos para: y^2 = x^3 + " + a + "x + " + b + " (mod " + p + ")");
        
        List<BigInteger[]> puntos = calcularPuntos(a, b, p);

        System.out.println("Total de puntos encontrados (incluyendo infinito): " + puntos.size());
        System.out.println("Primeros 10 puntos (x, y, z):");
        for (int i = 0; i < Math.min(10, puntos.size()); i++) {
            BigInteger[] pt = puntos.get(i);
            System.out.println("(" + pt[0] + ", " + pt[1] + ", " + pt[2] + ")");
        }
    }

    public static List<BigInteger[]> calcularPuntos(BigInteger a, BigInteger b, BigInteger p) {
        List<BigInteger[]> listaPuntos = new ArrayList<>();

        // 1. Agregar el punto al infinito (0, 1, 0) según pide el ejercicio
        listaPuntos.add(new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO});

        // 2. Probar cada x en el rango [0, p-1]
        for (BigInteger x = BigInteger.ZERO; x.compareTo(p) < 0; x = x.add(BigInteger.ONE)) {
            
            // Lado derecho: RHS = (x^3 + ax + b) mod p
            BigInteger rhs = x.pow(3).add(a.multiply(x)).add(b).mod(p);

            // 3. Buscar y tal que y^2 mod p == rhs
            for (BigInteger y = BigInteger.ZERO; y.compareTo(p) < 0; y = y.add(BigInteger.ONE)) {
                if (y.pow(2).mod(p).equals(rhs)) {
                    // Representación (x, y, 1) para puntos diferentes al infinito
                    listaPuntos.add(new BigInteger[]{x, y, BigInteger.ONE});
                }
            }
        }
        return listaPuntos;
    }
}

