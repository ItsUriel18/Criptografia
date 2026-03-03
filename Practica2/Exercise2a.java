import java.math.BigInteger;
import java.util.Scanner;

public class Exercise2a {

    static class Punto {
        BigInteger x, y, z;

        public Punto(BigInteger x, BigInteger y, BigInteger z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        // Este método soluciona el error de "Exercise2a$Punto@..."
        @Override
        public String toString() {
            if (x.equals(BigInteger.ZERO) && y.equals(BigInteger.ONE) && z.equals(BigInteger.ZERO)) {
                return "Punto al infinito (0, 1, 0)";
            }
            return "(" + x + ", " + y + ", " + z + ")";
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Ingrese el valor de p (primo): ");
        BigInteger p = new BigInteger(sc.next());
        
        System.out.print("Ingrese los valores de a y b separados por espacio: ");
        BigInteger a = new BigInteger(sc.next());
        BigInteger b = new BigInteger(sc.next());

        System.out.print("Ingrese las coordenadas x y del punto P separadas por espacio: ");
        BigInteger px = new BigInteger(sc.next());
        BigInteger py = new BigInteger(sc.next());
        BigInteger pz = BigInteger.ONE;

        Punto puntoP = new Punto(px, py, pz);

        System.out.print("\nIngrese el valor del escalar k (k > 2): ");
        BigInteger k = new BigInteger(sc.next());
        if (k.compareTo(BigInteger.valueOf(2)) <= 0) {
            System.out.println("El valor de k debe ser mayor que 2.");
            sc.close();
            return;
        }

        // Mostrar resumen
        System.out.println("\n-------------------------------------------");
        System.out.println("Curva: y^2 = x^3 + " + a + "x + " + b + " (mod " + p + ")");
        System.out.println("Punto P: " + puntoP);
        System.out.println("Escalar k: " + k);
        Punto resultado = multiplicarPunto(k, puntoP, a, b, p);
        System.out.println(k +"P = " + resultado);
        System.out.println("-------------------------------------------");

        sc.close();
    }

    public static Punto multiplicarPunto(BigInteger k, Punto P, BigInteger a, BigInteger b, BigInteger p) {
        if (calcularDiscriminante(a, b, p).esCero()) {
            System.out.println("La curva no es elíptica (discriminante es cero).");
            return new Punto(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO); // Retorna el punto al infinito como placeholder
        }
        // Implementación de la multiplicación de puntos en la curva elíptica
        // Aquí se debería implementar el algoritmo de doble y suma para calcular k * P
        // Por simplicidad, este método no está implementado en esta versión
        return new Punto(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO); // Retorna el punto al infinito como placeholder
    }   
    public static Discriminante calcularDiscriminante(BigInteger a, BigInteger b, BigInteger p) {
        BigInteger cuatroA3 = a.pow(3).multiply(BigInteger.valueOf(4)).mod(p);
        BigInteger veintisieteB2 = b.pow(2).multiply(BigInteger.valueOf(27)).mod(p);
        BigInteger discriminante = cuatroA3.add(veintisieteB2).mod(p).negate().mod(p);
        return new Discriminante(discriminante);
    }
}
