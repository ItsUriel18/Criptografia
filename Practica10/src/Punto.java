import java.math.BigInteger;

public class Punto {
    public BigInteger x, y, z;
    public static final Punto INFINITO = new Punto(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO);

    // Constructor para puntos regulares en la curva
    public Punto(BigInteger x, BigInteger y, BigInteger z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Método útil para imprimir el punto durante las pruebas (debug)
    @Override
    public String toString() {
        if (this.z.equals(BigInteger.ZERO)) return "Punto al infinito (O)";
        return "Punto(" + x + ", " + y + ", " + z + ")";
    }
}
