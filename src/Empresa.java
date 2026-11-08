import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

public class Empresa {
    private final String nome;
    private final BigDecimal capitalInicial;
    private BigDecimal ganhos = BigDecimal.ZERO;
    private BigDecimal perdas = BigDecimal.ZERO; // sempre positivo
    private final Map<Area, BigDecimal> investimentos = new EnumMap<>(Area.class);

    public Empresa(String nome, BigDecimal capitalInicial) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("A empresa precisa de um nome.");
        }
        if (capitalInicial == null || capitalInicial.signum() <= 0) {
            throw new IllegalArgumentException("O capital inicial deve ser maior que zero.");
        }
        this.nome = nome.trim();
        this.capitalInicial = capitalInicial.setScale(2, RoundingMode.HALF_UP);
        limparInvestimentos();
    }

    /** Fonte única da verdade: capital inicial + ganhos - perdas. */
    public BigDecimal getPatrimonio() {
        return capitalInicial.add(ganhos).subtract(perdas);
    }

    public BigDecimal getCaixaLivre() {
        BigDecimal investido = investimentos.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return getPatrimonio().subtract(investido);
    }

    /** Define (substitui) o valor investido em uma área na rodada atual. */
    public void investir(Area area, BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("O valor não pode ser negativo.");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP); // centavos, no máximo
        BigDecimal livreSemEstaArea = getCaixaLivre().add(investimentos.get(area));
        if (valor.compareTo(livreSemEstaArea) > 0) {
            throw new IllegalArgumentException("Valor acima do caixa disponível.");
        }
        investimentos.put(area, valor);
    }

    public BigDecimal getInvestimento(Area area) {
        return investimentos.get(area);
    }

    public void limparInvestimentos() {
        for (Area area : Area.values()) {
            investimentos.put(area, BigDecimal.ZERO.setScale(2));
        }
    }

    public void aplicarImpacto(BigDecimal impacto) {
        if (impacto.signum() >= 0) {
            ganhos = ganhos.add(impacto);
        } else {
            perdas = perdas.add(impacto.abs());
        }
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getCapitalInicial() {
        return capitalInicial;
    }

    public BigDecimal getGanhos() {
        return ganhos;
    }

    public BigDecimal getPerdas() {
        return perdas;
    }
}