import java.math.BigDecimal;
import java.math.RoundingMode;

public class Evento {
    private final String nome;
    private final Area area;
    private final int percentual; // 12 = +12%, -6 = -6%

    public Evento(String nome, Area area, int percentual) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O evento precisa de um nome.");
        }
        if (area == null) {
            throw new IllegalArgumentException("O evento precisa de uma área.");
        }
        this.nome = nome;
        this.area = area;
        this.percentual = percentual;
    }

    /** Impacto = valor investido na área × percentual do evento. */
    public BigDecimal calcularImpacto(BigDecimal valorInvestido) {
        return calcularPercentual(valorInvestido, percentual);
    }

    /** valor × percentual ÷ 100, com 2 casas decimais. */
    public static BigDecimal calcularPercentual(BigDecimal valor, int percentual) {
        return valor
                .multiply(BigDecimal.valueOf(percentual))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public String getNome() {
        return nome;
    }

    public Area getArea() {
        return area;
    }

    public int getPercentual() {
        return percentual;
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %+d%%]", nome, area, percentual);
    }
}