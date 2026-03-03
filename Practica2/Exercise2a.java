import java.math.BigInteger;
import java.util.Scanner;

public class Exercise2a {

    static class Punto {
        BigInteger x, y, z;

        Punto(BigInteger x, BigInteger y, BigInteger z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        // Verifica si es el punto al infinito (0, 1, 0)
        public boolean esInfinito() {
            return x.equals(BigInteger.ZERO) && 
                   y.equals(BigInteger.ONE) && 
                   z.equals(BigInteger.ZERO);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ", " + z + ")";
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Entrada de parámetros de la curva
        System.out.println("Digite el valor de p (módulo primo):");
        BigInteger p = sc.nextBigInteger();
        System.out.println("Digite el valor de a:");
        BigInteger a = sc.nextBigInteger();
        System.out.println("Digite el valor de b:");
        BigInteger b = sc.nextBigInteger();

        // Entrada del Punto P (x, y, z)
        System.out.println("Digite la coordenada x de P:");
        BigInteger px = sc.nextBigInteger();
        System.out.println("Digite la coordenada y de P:");
        BigInteger py = sc.nextBigInteger();
        System.out.println("Digite la coordenada z de P (1 para afín, 0 para infinito):");
        BigInteger pz = sc.nextBigInteger();
        
        Punto P = new Punto(px, py, pz);

        // Entrada del escalar k
        System.out.println("Digite el valor del escalar k:");
        BigInteger k = sc.nextBigInteger();

        // Cálculo de kP
        Punto resultado = multiplicacionEscalar(k, P, a, p);

        System.out.println("El valor de kP es: " + resultado);
        sc.close();
    }

    public static Punto multiplicacionEscalar(BigInteger k, Punto P, BigInteger a, BigInteger p) {
        // Inicializamos el resultado como el punto al infinito (0, 1, 0)
        Punto resultado = new Punto(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO);
        Punto auxiliar = P;

        // Algoritmo Double-and-Add
        String binario = k.toString(2);
        for (int i = binario.length() - 1; i >= 0; i--) {
            if (binario.charAt(i) == '1') {
                resultado = sumarPuntos(resultado, auxiliar, p);
            }
            auxiliar = duplicarPunto(auxiliar, a, p);
        }
        return resultado;
    }

    // --- AQUÍ DEBES INTEGRAR TUS MÉTODOS DEL LAB ANTERIOR ---
    
    public static Punto sumarPuntos(Punto P, Punto Q, BigInteger p) {
        // 1. Manejo de puntos al infinito
        if (P.esInfinito()) return Q;
        if (Q.esInfinito()) return P;
        
        // 2. Lógica de suma (usa BigInteger.modInverse para divisiones)
        // ... (Tu implementación previa de sumarPuntos)
        return new Punto(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO); // Dummy
    }

    public static Punto duplicarPunto(Punto P, BigInteger a, BigInteger p) {
        if (P.esInfinito()) return P;
        
        // 3. Lógica de duplicación (lambda = (3x^2 + a) / 2y)
        // ... (Tu implementación previa de duplicarPunto)
        return new Punto(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO); // Dummy
    }
}
