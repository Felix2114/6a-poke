package edu.usbi.programa6;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudentTTest {
    private final StudentT distribution = new StudentT();

    @Test
    void reproduceLosTresCasosDelPdf() {
        verifyCase(0.20, 6, 0.55338);
        verifyCase(0.45, 15, 1.75305);
        verifyCase(0.495, 4, 4.60409);
    }

    private void verifyCase(double p, int dof, double expected) {
        double x = distribution.findX(p, dof);
        assertEquals(expected, x, 0.00001);
        assertEquals(p, distribution.integrate(x, dof), 0.00001);
    }

    @Test
    void calculaElLimiteCeroYLaDistribucionConUnGradoDeLibertad() {
        assertEquals(0.0, distribution.integrate(0.0, 1));
        assertEquals(0.0, distribution.findX(0.0, 1));
        // Para DOF=1, la integral es atan(x)/pi.
        assertEquals(1.0, distribution.findX(0.25, 1), 1e-8);
        assertEquals(Math.atan(100.0) / Math.PI, distribution.integrate(100.0, 1), 1e-10);
        assertEquals(Math.tan(Math.PI * 0.4999), distribution.findX(0.4999, 1), 0.01);
    }

    @Test
    void calculaConGradosDeLibertadGrandes() {
        // Al crecer DOF, la distribución se aproxima a la normal estándar.
        assertEquals(1.95996, distribution.findX(0.475, 1_000_000), 0.00002);
    }

    @Test
    void rechazaEntradasFueraDelDominio() {
        assertThrows(IllegalArgumentException.class, () -> distribution.findX(-0.1, 1));
        assertThrows(IllegalArgumentException.class, () -> distribution.findX(0.5, 1));
        assertThrows(IllegalArgumentException.class, () -> distribution.findX(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> distribution.findX(Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class, () -> distribution.findX(0.2, 0));
        assertThrows(IllegalArgumentException.class, () -> distribution.integrate(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> distribution.integrate(Double.NaN, 1));
    }

    @Test
    void programaGuardaResultadoYRechazaErrores(@TempDir Path directory) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classes = Path.of("target/classes").toAbsolutePath().toString();
        Process valid = new ProcessBuilder(java, "-cp", classes, App.class.getName(), "0.20", "6")
                .directory(directory.toFile()).redirectErrorStream(true).start();
        assertTrue(valid.waitFor(10, TimeUnit.SECONDS));
        assertEquals(0, valid.exitValue());
        assertTrue(new String(valid.getInputStream().readAllBytes(), StandardCharsets.UTF_8).contains("0.55338"));
        assertEquals("0.55338\n", Files.readString(directory.resolve("output/output6.txt")));

        Process invalid = new ProcessBuilder(java, "-cp", classes, App.class.getName(), "0.5", "6")
                .directory(directory.toFile()).redirectErrorStream(true).start();
        assertTrue(invalid.waitFor(10, TimeUnit.SECONDS));
        assertNotEquals(0, invalid.exitValue());
        assertTrue(new String(invalid.getInputStream().readAllBytes(), StandardCharsets.UTF_8).contains("No se pudo calcular"));
    }
}
