package Practica5;

import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Scanner;

public class ECDSA {
    private static final SecureRandom random = new SecureRandom();

    public static class Punto {
        public BigInteger x, y, z;
        public boolean isInfinito;

        // El punto al infinito se define con z = 0 en coordenadas proyectivas
        public static final Punto INFINITO = new Punto(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO, true);

        // Constructor para puntos normales (z = 1)
        public Punto(BigInteger x, BigInteger y, BigInteger z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.isInfinito = false;
        }

        private Punto(BigInteger x, BigInteger y, BigInteger z, boolean inf) {
            this.x = x; this.y = y; this.z = z; this.isInfinito = inf;
        }
    }

    public static void main(String[] args) {
        // Datos de ejemplo: Fuente 1 [cite: 2, 3]
        BigInteger p = new BigInteger("1009");
        BigInteger a = new BigInteger("1006");
        BigInteger b = new BigInteger("4");
        BigInteger q = new BigInteger("967");
        Punto G = new Punto(new BigInteger("256"), new BigInteger("155"), BigInteger.ONE);

        // Generar clave privada d: 0 < d < q

        BigInteger d;
        do {
            d = new BigInteger(q.bitLength(), random);
        } while (d.compareTo(BigInteger.ZERO) <= 0 || d.compareTo(q) >= 0);

        // Calcular B = d * G usando RTL
        Punto B = RTL(G, d, p, a);

        System.out.println("=== CLAVE PÚBLICA GENERADA ===");
        System.out.println( p + ", " + a + ", " + b + ", " + q + " ,  (" + G.x + ", " + G.y + ", " + G.z + ") , (" + B.x + ", " + B.y + ", " + B.z + ")");
        System.out.println("Clave Privada d: " + d);

        // 1. Elegir un entero m tal que 0 < m < q
        // Para la práctica, puedes elegir un número fijo o aleatorio

        System.out.println("\nIngrese un mensaje m (0 < m < q): ");
        Scanner sc = new Scanner(System.in);
        BigInteger m = new BigInteger(sc.next());

        // 2. Generar la firma (r, s)
        BigInteger[] firma = generarFirma(m, q, G, d, p, a);
        BigInteger r = firma[0];
        BigInteger s = firma[1];

        // 3. Imprimir resultados para el Excel
        System.out.println("\n=== GENERACIÓN DE FIRMA ===");
        System.out.println("Mensaje (m): " + m);
        System.out.println("Firma r: " + r);
        System.out.println("Firma s: " + s);
        System.out.println("Formato Excel (m, r, s): " + m + ", " + r + ", " + s);
        sc.close();

        // --- PASO 3: VERIFICACIÓN ---
        System.out.println("\n=== INICIANDO VERIFICACIÓN ===");
        boolean esValida = verificarFirma(m, r, s, q, G, B, p, a);

        if (esValida) {
            System.out.println("Resultado: ¡La firma es VÁLIDA!");
        } else {
            System.out.println("Resultado: La firma es INVÁLIDA.");
        }
    }

    public static Punto RTL(Punto P_ingresado, BigInteger k, BigInteger p, BigInteger a) {
        Punto Q = Punto.INFINITO;
        Punto P_aux = P_ingresado;
        int t = k.bitLength();

        for (int i = 0; i < t; i++) {
            if (k.testBit(i)) { 
                Q = sumarPuntos(Q, P_aux, p, a);
            }
            P_aux = doblarPunto(P_aux, p, a);
        }
        return Q;
    }

    public static Punto sumarPuntos(Punto P1, Punto P2, BigInteger p, BigInteger a) {
        if (P1.isInfinito) return P2;
        if (P2.isInfinito) return P1;
        
        if (P1.x.equals(P2.x)) {
            if (!P1.y.equals(P2.y)) return Punto.INFINITO;
            else return doblarPunto(P1, p, a);
        }

        // m = (y2 - y1) * inv(x2 - x1) mod p
        BigInteger num = P2.y.subtract(P1.y).mod(p);
        BigInteger den = P2.x.subtract(P1.x).mod(p);
        BigInteger m = num.multiply(den.modInverse(p)).mod(p);

        BigInteger x3 = m.pow(2).subtract(P1.x).subtract(P2.x).mod(p);
        BigInteger y3 = m.multiply(P1.x.subtract(x3)).subtract(P1.y).mod(p);

        return new Punto(x3, y3, BigInteger.ONE); // z siempre vuelve a 1
    }

    public static Punto doblarPunto(Punto P, BigInteger p, BigInteger a) {
        if (P.isInfinito || P.y.equals(BigInteger.ZERO)) return Punto.INFINITO;

        // m = (3x^2 + a) * inv(2y) mod p
        BigInteger num = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a).mod(p);
        BigInteger den = P.y.multiply(BigInteger.valueOf(2)).mod(p);
        BigInteger m = num.multiply(den.modInverse(p)).mod(p);

        BigInteger x3 = m.pow(2).subtract(P.x.multiply(BigInteger.valueOf(2))).mod(p);
        BigInteger y3 = m.multiply(P.x.subtract(x3)).subtract(P.y).mod(p);

        return new Punto(x3, y3, BigInteger.ONE); // z siempre vuelve a 1

    }

    // Genera la firma (r, s)
    public static BigInteger[] generarFirma(BigInteger m, BigInteger q, Punto G, BigInteger d, BigInteger p, BigInteger a) {
        BigInteger kE, r, s;

        do {
            // Inciso a)
            do {
                kE = new BigInteger(q.bitLength(), random);
            } while (kE.compareTo(BigInteger.ZERO) <= 0 || kE.compareTo(q) >= 0);

            // Inciso b)
            Punto T = RTL(G, kE, p, a);

            // Inciso c)
            r = T.x.mod(q);

        } while (r.equals(BigInteger.ZERO)); // r no puede ser 0 en ECDSA

        // Inciso d)
        BigInteger kE_inv = kE.modInverse(q);
        s = m.add(d.multiply(r)).multiply(kE_inv).mod(q);

        // Inciso e)
        return new BigInteger[]{r, s};
    }

    //Validación de la firma
    
    public static boolean verificarFirma(BigInteger m, BigInteger r, BigInteger s, BigInteger q, Punto G, Punto B, BigInteger p, BigInteger a) {
        // Validaciones básicas de rango: 0 < r < q y 0 < s < q
        if (r.compareTo(BigInteger.ZERO) <= 0 || r.compareTo(q) >= 0) return false;
        if (s.compareTo(BigInteger.ZERO) <= 0 || s.compareTo(q) >= 0) return false;

        // Inciso a) 
        BigInteger w = s.modInverse(q);

        // Inciso b) 
        BigInteger u1 = w.multiply(m).mod(q);

        // Inciso c) 
        BigInteger u2 = w.multiply(r).mod(q);

        // Inciso d) Compute P = u1*G + u2*B
        Punto u1G = RTL(G, u1, p, a);
        Punto u2B = RTL(B, u2, p, a);
        Punto P = sumarPuntos(u1G, u2B, p, a);

        if (P.isInfinito) return false;

        // Inciso e) If xP == r mod q the signature is valid
        // Obtenemos la coordenada x del punto resultante y la comparamos con r
        BigInteger xP = P.x.mod(q);
    
        return xP.equals(r.mod(q));
    }
}
