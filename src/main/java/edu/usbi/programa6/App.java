package edu.usbi.programa6;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;

public final class App {
    private App() { }

    public static void main(String[] args) {
        try {
            String pText;
            String dofText;
            if (args.length == 0) {
                Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
                System.out.print("Valor de p (0 <= p < 0.5): ");
                if (!scanner.hasNextLine()) throw new IllegalArgumentException("Falta el valor de p.");
                pText = scanner.nextLine();
                System.out.print("DOF (grados de libertad): ");
                if (!scanner.hasNextLine()) throw new IllegalArgumentException("Faltan los grados de libertad.");
                dofText = scanner.nextLine();
            } else if (args.length == 2) {
                pText = args[0];
                dofText = args[1];
            } else {
                throw new IllegalArgumentException("Uso: java -jar programa-6a-1.0-SNAPSHOT.jar [p dof]");
            }
            double p = Double.parseDouble(pText.trim().replace(',', '.'));
            int dof = Integer.parseInt(dofText.trim());
            double x = new StudentT().findX(p, dof);
            String result = String.format(Locale.US, "%.5f%n", x);
            Path output = Path.of("output", "output6.txt");
            Files.createDirectories(output.getParent());
            Files.writeString(output, result, StandardCharsets.UTF_8);
            System.out.print("x = " + result);
            System.out.println("Resultado guardado en " + output);
        } catch (IllegalArgumentException | ArithmeticException | IOException e) {
            System.err.println("No se pudo calcular x: " + e.getMessage());
            System.exit(1);
        }
    }
}
