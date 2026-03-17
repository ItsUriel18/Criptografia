import java.math.BigInteger;
import java.io.File;
import java.util.Scanner;

public class LectorDSA {
    public static void main(String[] args) {
        // Nombre exacto del archivo según tu imagen
        String nombreArchivo = "DSA data(Phone list).csv";
        
        try {
            File archivo = new File(nombreArchivo);
            // Usamos ISO_8859_1 para que los nombres con acentos () se lean mejor
            Scanner lector = new Scanner(archivo, "ISO-8859-1");
            
            // Saltamos exactamente 5 líneas de títulos
            for (int i = 0; i < 5 && lector.hasNextLine(); i++) {
                lector.nextLine();
            }

            System.out.println("=== PROCESANDO LISTA DE ESCOM ===");
            System.out.printf("%-30s | %-10s%n", "Nombre del Alumno", "Resultado");
            System.out.println("------------------------------------------------------------");

            while (lector.hasNextLine()) {
                String linea = lector.nextLine().trim();
                if (linea.isEmpty()) continue; // Saltar líneas vacías

                String[] datos = linea.split(",");

                if (datos.length >= 9) {
                    String nombre = datos[1].trim();
                    String valorP = datos[2].trim();

                    if (valorP.isEmpty()) {
                        continue; // Alumnos sin datos
                    }

                    try {
                        BigInteger p = new BigInteger(valorP).abs();
                        BigInteger q = new BigInteger(datos[3].trim()).abs();
                        BigInteger g = new BigInteger(datos[4].trim()).abs();
                        BigInteger beta = new BigInteger(datos[5].trim()).abs();
                        BigInteger m = new BigInteger(datos[6].trim()).abs();
                        BigInteger r = new BigInteger(datos[7].trim()).abs();
                        BigInteger s = new BigInteger(datos[8].trim()).abs();

                        boolean esValida = DSA.verificarFirma(m, r, s, p, q, g, beta);
                        System.out.printf("%-30s | %-10s%n", nombre, (esValida ? "VÁLIDA" : "INVÁLIDA"));

                    } catch (Exception e) {
                        // Si hay un error en los números de esa fila, nos avisa
                        System.out.printf("%-30s | ERROR EN DATOS%n", nombre);
                    }
                }
            }
            lector.close();
            System.out.println("------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("No se pudo procesar el archivo: " + e.getMessage());
        }
    }
}