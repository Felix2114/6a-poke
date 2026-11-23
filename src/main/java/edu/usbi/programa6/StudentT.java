package edu.usbi.programa6;

/** Distribución t de Student: integración de Simpson y búsqueda del límite x. */
public final class StudentT {
    private static final double INTEGRATION_ERROR = 1e-12;
    private static final double SEARCH_ERROR = 1e-10;
    private static final int MAX_SEGMENTS = 1_310_720;
    private static final int MAX_SEARCH_STEPS = 2048;

    /** Encuentra x >= 0 tal que la integral de 0 a x sea p. */
    public double findX(double p, int dof) {
        validateDof(dof);
        if (!Double.isFinite(p) || p < 0.0 || p >= 0.5) {
            throw new IllegalArgumentException("p debe ser un número finito entre 0 (incluido) y 0.5 (excluido).");
        }
        if (p == 0.0) {
            return 0.0;
        }

        double lower = 0.0;
        double upper = 1.0;
        // Primero se encierra la solución; después se reduce el intervalo a la mitad.
        // Así también se alcanzan valores grandes de x cuando p está cerca de 0.5.
        for (int i = 0; i < MAX_SEARCH_STEPS; i++) {
            double error = p - integrate(upper, dof);
            if (Math.abs(error) <= SEARCH_ERROR) {
                return upper;
            }
            if (error < 0) {
                break;
            }
            lower = upper;
            upper *= 2.0;
            if (!Double.isFinite(upper)) {
                throw new ArithmeticException("No se pudo alcanzar la precisión solicitada para p.");
            }
        }
        for (int i = 0; i < MAX_SEARCH_STEPS; i++) {
            double x = lower + (upper - lower) / 2.0;
            double error = p - integrate(x, dof);
            if (Math.abs(error) <= SEARCH_ERROR) {
                return x;
            }
            if (x == lower || x == upper) {
                throw new ArithmeticException("No se pudo alcanzar la precisión solicitada para p.");
            }
            if (error > 0) {
                lower = x;
            } else {
                upper = x;
            }
        }
        throw new ArithmeticException("La búsqueda no convergió.");
    }

    /** Integral de la densidad t desde 0 hasta x, usando Simpson compuesto. */
    public double integrate(double x, int dof) {
        validateDof(dof);
        if (!Double.isFinite(x) || x < 0) {
            throw new IllegalArgumentException("x debe ser un número finito mayor o igual que cero.");
        }
        if (x == 0.0) {
            return 0.0;
        }
        // t = tan(theta) mantiene acotado el intervalo incluso para valores grandes de x.
        double limit = Math.atan(x);
        double coefficient = coefficient(dof);
        int segments = 10;
        double previous = simpson(limit, dof, coefficient, segments);
        while (segments < MAX_SEGMENTS) {
            segments *= 2;
            double current = simpson(limit, dof, coefficient, segments);
            if (Math.abs(current - previous) / 15.0 <= INTEGRATION_ERROR) {
                return current;
            }
            previous = current;
        }
        throw new ArithmeticException("La integración no convergió.");
    }

    private double simpson(double limit, int dof, double coefficient, int segments) {
        double width = limit / segments;
        double sum = densityAfterSubstitution(0, dof, coefficient)
                + densityAfterSubstitution(limit, dof, coefficient);
        for (int i = 1; i < segments; i++) {
            sum += (i % 2 == 0 ? 2 : 4) * densityAfterSubstitution(i * width, dof, coefficient);
        }
        return sum * width / 3.0;
    }

    private double densityAfterSubstitution(double theta, int dof, double coefficient) {
        double cosine = Math.cos(theta);
        double tangent = Math.tan(theta);
        return coefficient * Math.exp(-((dof + 1.0) / 2.0) * Math.log1p(tangent * tangent / dof)
                - 2.0 * Math.log(cosine));
    }

    private double coefficient(int dof) {
        // c(v+2) = c(v) * (v+1) / sqrt(v*(v+2)); evita desbordar Gamma.
        // Para valores grandes se usa la expansión asintótica del cociente de Gamma.
        if (dof >= 10_000) {
            double inverse = 1.0 / dof;
            return (1.0 - inverse / 4.0 + inverse * inverse / 32.0
                    + 5.0 * inverse * inverse * inverse / 128.0) / Math.sqrt(2.0 * Math.PI);
        }
        int v = dof % 2 == 0 ? 2 : 1;
        double result = v == 1 ? 1.0 / Math.PI : 1.0 / (2.0 * Math.sqrt(2.0));
        for (; v < dof; v += 2) {
            result *= (v + 1.0) / Math.sqrt((double) v * (v + 2.0));
        }
        return result;
    }

    private void validateDof(int dof) {
        if (dof <= 0) {
            throw new IllegalArgumentException("DOF debe ser un entero mayor que cero.");
        }
    }
}
