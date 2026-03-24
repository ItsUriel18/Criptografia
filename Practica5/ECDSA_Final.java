package Practica5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;

public class ECDSA_Final {

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
        // Procesamos todo el archivo de la clase
        validarArchivoCSV("ECDSA data (Phone list) (1).csv");
    }

    public static void validarArchivoCSV(String ruta) {
        System.out.println("=== INICIANDO PROCESO MASIVO: VERIFICACIÓN Y CRACKING ===");
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int n = 0;
            while ((linea = br.readLine()) != null) {
                n++;
                // Saltamos los encabezados (el nuevo CSV tiene 4 líneas de cabecera)
                if (n <= 4) continue; 
                
                String[] col = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (col.length < 11 || col[2].isEmpty() || col[2].equals("p")) continue;

                try {
                    // 1. Extraer parámetros
                    BigInteger p = new BigInteger(col[2].trim());
                    BigInteger a = new BigInteger(col[3].trim());
                    BigInteger q = new BigInteger(col[5].trim());
                    Punto G = limpiarPunto(col[6]);
                    Punto B = limpiarPunto(col[7]);
                    BigInteger m = new BigInteger(col[8].trim());
                    BigInteger r = new BigInteger(col[9].trim());
                    BigInteger s = new BigInteger(col[10].trim());

                    // 2. PRIMERA IMPRESIÓN: Verificación de Firma
                    boolean ok = verificarFirma(m, r, s, q, G, B, p, a);
                    System.out.println("--------------------------------------------------");
                    System.out.println("Fila " + n + " [" + col[1] + "]: Firma -> " + (ok ? "VÁLIDA" : "INVÁLIDA"));

                    // 3. SEGUNDA IMPRESIÓN: Cálculo de Clave Privada (Cracking)
                    // Ponemos un límite de seguridad para q para no trabar el programa
                    if (q.compareTo(new BigInteger("10000000")) < 0) {
                        BigInteger dCracked = crackPrivateKey(G, B, q, p, a);
                        System.out.println("   -> Clave Privada calculada (d): " + dCracked);
                    } else {
                        System.out.println("   -> Clave demasiado grande para fuerza bruta (q > 10^7)");
                    }

                } catch (Exception e) {
                    // Ignorar filas con errores de formato menores
                }
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // --- MÉTODOS DE CÁLCULO (RTL, Sumar, Doblar) ---

    public static BigInteger crackPrivateKey(Punto G, Punto B, BigInteger q, BigInteger p, BigInteger a) {
        for (BigInteger i = BigInteger.ONE; i.compareTo(q) < 0; i = i.add(BigInteger.ONE)) {
            Punto T = RTL(G, i, p, a);
            if (!T.isInfinito && T.x.equals(B.x) && T.y.equals(B.y)) return i;
        }
        return null;
    }

    public static boolean verificarFirma(BigInteger m, BigInteger r, BigInteger s, BigInteger q, Punto G, Punto B, BigInteger p, BigInteger a) {
        try {
            BigInteger w = s.modInverse(q);
            BigInteger u1 = w.multiply(m).mod(q);
            BigInteger u2 = w.multiply(r).mod(q);
            Punto P = sumarPuntos(RTL(G, u1, p, a), RTL(B, u2, p, a), p, a);
            return !P.isInfinito && P.x.mod(q).equals(r.mod(q));
        } catch (Exception e) { return false; }
    }

    public static Punto RTL(Punto P, BigInteger k, BigInteger p, BigInteger a) {
        Punto Q = Punto.INFINITO;
        Punto aux = P;
        for (int i = 0; i < k.bitLength(); i++) {
            if (k.testBit(i)) Q = sumarPuntos(Q, aux, p, a);
            aux = doblarPunto(aux, p, a);
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
        BigInteger m = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a).multiply(P.y.multiply(BigInteger.valueOf(2)).modInverse(p)).mod(p);
        BigInteger x3 = m.pow(2).subtract(P.x.multiply(BigInteger.valueOf(2))).mod(p);
        BigInteger y3 = m.multiply(P.x.subtract(x3)).subtract(P.y).mod(p);
        return new Punto(x3, y3);
    }

    private static Punto limpiarPunto(String raw) {
        String clean = raw.replaceAll("[()\\s\"\\n\\r]", "");
        String[] c = clean.split(",");
        return new Punto(new BigInteger(c[0].trim()), new BigInteger(c[1].trim()));
    }
}