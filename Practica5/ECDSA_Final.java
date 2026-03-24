package Practica5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.SecureRandom;

public class ECDSA_Final {
    private static final SecureRandom random = new SecureRandom();

    // --- 1. CLASE PARA PUNTOS DE LA CURVA ---
    public static class Punto {
        public BigInteger x, y;
        public boolean isInfinito;
        public static final Punto INFINITO = new Punto(null, null, true);

        public Punto(BigInteger x, BigInteger y) {
            this.x = x; this.y = y; this.isInfinito = false;
        }
        private Punto(BigInteger x, BigInteger y, boolean inf) {
            this.x = x; this.y = y; this.isInfinito = inf;
        }
    }

    public static void main(String[] args) {
        // --- PARTE A: TU PROPIA GENERACIÓN Y FIRMA ---
        ejecutarProcesoPersonal();

        // --- PARTE B: VALIDACIÓN DEL CSV DE LA CLASE ---
        validarArchivoCSV("ECDSA data (Phone list) (1).csv");
    }

    // --- 2. LÓGICA DE GENERACIÓN Y FIRMA PERSONAL ---
    public static void ejecutarProcesoPersonal() {
        // Datos de la Fuente 1
        BigInteger p = new BigInteger("1009");
        BigInteger a = new BigInteger("1006");
        BigInteger q = new BigInteger("967");
        Punto G = new Punto(new BigInteger("256"), new BigInteger("155"));

        // Generar Clave Privada d (0 < d < q)
        BigInteger d;
        do { d = new BigInteger(q.bitLength(), random); } while (d.compareTo(BigInteger.ZERO) <= 0 || d.compareTo(q) >= 0);

        // Clave Pública B = d * G
        Punto B = RTL(G, d, p, a);

        // Firmar un mensaje m
        BigInteger m = new BigInteger("500");
        BigInteger[] firma = generarFirma(m, q, G, d, p, a);

        System.out.println("=== MI REPORTE PARA EXCEL ===");
        System.out.println("Public Key B: (" + B.x + ", " + B.y + ")");
        System.out.println("Firma (r, s): (" + firma[0] + ", " + firma[1] + ")");
        System.out.println("------------------------------------------\n");
    }

    // --- 3. MÉTODOS CORE DE ECDSA ---

    public static BigInteger[] generarFirma(BigInteger m, BigInteger q, Punto G, BigInteger d, BigInteger p, BigInteger a) {
        BigInteger kE, r, s;
        do {
            do { kE = new BigInteger(q.bitLength(), random); } while (kE.compareTo(BigInteger.ZERO) <= 0 || kE.compareTo(q) >= 0);
            Punto T = RTL(G, kE, p, a);
            r = T.x.mod(q); // r = xT mod q
        } while (r.equals(BigInteger.ZERO));

        s = m.add(d.multiply(r)).multiply(kE.modInverse(q)).mod(q); // s = (m + dr)kE^-1 mod q
        return new BigInteger[]{r, s};
    }

    public static boolean verificarFirma(BigInteger m, BigInteger r, BigInteger s, BigInteger q, Punto G, Punto B, BigInteger p, BigInteger a) {
        if (r.compareTo(BigInteger.ZERO) <= 0 || r.compareTo(q) >= 0 || s.compareTo(BigInteger.ZERO) <= 0 || s.compareTo(q) >= 0) return false;
        
        BigInteger w = s.modInverse(q); // w = s^-1 mod q
        BigInteger u1 = w.multiply(m).mod(q); // u1 = wm mod q
        BigInteger u2 = w.multiply(r).mod(q); // u2 = wr mod q

        Punto P = sumarPuntos(RTL(G, u1, p, a), RTL(B, u2, p, a), p, a); // P = u1G + u2B
        return !P.isInfinito && P.x.mod(q).equals(r.mod(q)); // xP == r mod q
    }

    // --- 4. ARITMÉTICA DE LA CURVA (Módulo p) ---

    public static Punto RTL(Punto P_ingresado, BigInteger k, BigInteger p, BigInteger a) {
        Punto Q = Punto.INFINITO;
        Punto P_aux = P_ingresado;
        for (int i = 0; i < k.bitLength(); i++) {
            if (k.testBit(i)) Q = sumarPuntos(Q, P_aux, p, a);
            P_aux = doblarPunto(P_aux, p, a);
        }
        return Q;
    }

    public static Punto sumarPuntos(Punto P1, Punto P2, BigInteger p, BigInteger a) {
        if (P1.isInfinito) return P2;
        if (P2.isInfinito) return P1;
        if (P1.x.equals(P2.x)) return P1.y.equals(P2.y) ? doblarPunto(P1, p, a) : Punto.INFINITO;

        BigInteger m = P2.y.subtract(P1.y).multiply(P2.x.subtract(P1.x).modInverse(p)).mod(p);
        BigInteger x3 = m.pow(2).subtract(P1.x).subtract(P2.x).mod(p);
        BigInteger y3 = m.multiply(P1.x.subtract(x3)).subtract(P1.y).mod(p);
        return new Punto(x3, y3);
    }

    public static Punto doblarPunto(Punto P, BigInteger p, BigInteger a) {
        if (P.isInfinito || P.y.equals(BigInteger.ZERO)) return Punto.INFINITO;
        BigInteger num = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a);
        BigInteger den = P.y.multiply(BigInteger.valueOf(2));
        BigInteger m = num.multiply(den.modInverse(p)).mod(p);
        BigInteger x3 = m.pow(2).subtract(P.x.multiply(BigInteger.valueOf(2))).mod(p);
        BigInteger y3 = m.multiply(P.x.subtract(x3)).subtract(P.y).mod(p);
        return new Punto(x3, y3);
    }

    // --- 5. PROCESADOR DE CSV ---

    public static void validarArchivoCSV(String ruta) {
        System.out.println("=== VALIDANDO ARCHIVO CSV DE LA CLASE ===");
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int n = 0;
            while ((linea = br.readLine()) != null) {
                n++;
                if (n <= 2) continue; // Encabezados
                
                String[] col = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (col.length < 11 || col[2].isEmpty()) continue;

                try {
                    BigInteger p = new BigInteger(col[2].trim());
                    BigInteger a = new BigInteger(col[3].trim());
                    BigInteger q = new BigInteger(col[5].trim());
                    Punto G = limpiarPunto(col[6]);
                    Punto B = limpiarPunto(col[7]);
                    BigInteger m = new BigInteger(col[8].trim());
                    BigInteger r = new BigInteger(col[9].trim());
                    BigInteger s = new BigInteger(col[10].trim());

                    boolean ok = verificarFirma(m, r, s, q, G, B, p, a);
                    System.out.println("Fila " + n + " [" + col[1] + "]: " + (ok ? "VÁLIDA" : "INVÁLIDA"));
                } catch (Exception e) {
                    System.out.println("Fila " + n + ": ERROR DE DATOS (" + e.getMessage() + ")");
                }
            }
        } catch (Exception e) { System.out.println("Error al leer el archivo: " + e.getMessage()); }
    }

    private static Punto limpiarPunto(String raw) {
        String clean = raw.replaceAll("[()\\s\"\\n\\r]", ""); // Elimina paréntesis, saltos de línea y basura
        String[] c = clean.split(",");
        return new Punto(new BigInteger(c[0].trim()), new BigInteger(c[1].trim()));
    }
}