import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Simulador {

    // Rendimento "de mercado" sorteado para CADA investimento, em toda rodada (em %).
    private static final int RENDIMENTO_MIN = -8;
    private static final int RENDIMENTO_MAX = 12;

    /** Rendimento de um investimento: sorteado na rodadaOrigem, creditado na rodadaPagamento. */
    public record Rendimento(Empresa empresa, Area area, int rodadaOrigem, int rodadaPagamento,
                             BigDecimal investido, int percentual) {
        public BigDecimal valor() {
            return Evento.calcularPercentual(investido, percentual);
        }
    }

    /** Resultado de uma empresa na rodada. */
    public record Resultado(Empresa empresa, BigDecimal valorBase, BigDecimal impactoEvento,
                            List<Rendimento> recebidos, int rendimentosACaminho) {
        public BigDecimal totalRecebido() {
            return recebidos.stream().map(Rendimento::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private final List<Empresa> empresas;
    private final List<Evento> eventosDisponiveis;
    private final List<Rendimento> pendencias = new ArrayList<>();
    private final int totalRodadas;
    private final Random random;
    private int rodadaAtual = 1;
    private Area ultimaArea = null; // área do último evento sorteado

    public Simulador(List<Empresa> empresas, int totalRodadas, List<Evento> catalogo) {
        this(empresas, totalRodadas, catalogo, new Random());
    }

    // Random injetado: permite testar com sorteio previsível (ex.: new Random(42)).
    public Simulador(List<Empresa> empresas, int totalRodadas, List<Evento> catalogo, Random random) {
        if (empresas.size() < 2) {
            throw new IllegalArgumentException("São necessárias pelo menos 2 empresas.");
        }
        if (totalRodadas < 1) {
            throw new IllegalArgumentException("A partida precisa de pelo menos 1 rodada.");
        }
        this.empresas = List.copyOf(empresas);
        this.eventosDisponiveis = new ArrayList<>(catalogo);
        this.totalRodadas = totalRodadas;
        this.random = random;
    }

    /**
     * Sorteia um evento (que não se repete na partida). Retorna null se acabaram.
     * Primeiro sorteia a ÁREA, evitando repetir a da rodada anterior; depois o evento dentro dela.
     * Assim as áreas se alternam e nenhuma é favorecida por ter mais eventos no catálogo.
     */
    public Evento sortearEvento() {
        if (eventosDisponiveis.isEmpty()) {
            return null;
        }

        List<Area> areas = eventosDisponiveis.stream().map(Evento::getArea).distinct().toList();
        List<Area> diferentes = areas.stream().filter(a -> a != ultimaArea).toList();
        if (!diferentes.isEmpty()) {
            areas = diferentes;
        }
        Area area = areas.get(random.nextInt(areas.size()));

        List<Evento> daArea = eventosDisponiveis.stream().filter(e -> e.getArea() == area).toList();
        Evento sorteado = daArea.get(random.nextInt(daArea.size()));

        eventosDisponiveis.remove(sorteado);
        ultimaArea = area;
        return sorteado;
    }

    /**
     * Processa a rodada para todas as empresas (o evento pode ser null):
     * 1) aplica o evento na hora;
     * 2) sorteia o rendimento de cada investimento e o agenda (agora ou em rodadas futuras);
     * 3) credita os rendimentos que venceram;
     * 4) zera os investimentos para a próxima rodada.
     */
    public List<Resultado> processarRodada(Evento evento) {
        List<Resultado> resultados = new ArrayList<>();

        for (Empresa empresa : empresas) {
            // 1) evento
            BigDecimal base = BigDecimal.ZERO;
            BigDecimal impactoEvento = BigDecimal.ZERO;
            if (evento != null) {
                base = empresa.getInvestimento(evento.getArea());
                impactoEvento = evento.calcularImpacto(base);
            }
            empresa.aplicarImpacto(impactoEvento);

            // 2) rendimento de cada área investida, com prazo sorteado
            for (Area area : Area.values()) {
                BigDecimal investido = empresa.getInvestimento(area);
                if (investido.signum() == 0) {
                    continue;
                }
                int percentual = RENDIMENTO_MIN + random.nextInt(RENDIMENTO_MAX - RENDIMENTO_MIN + 1);
                int atraso = sortearAtraso();
                pendencias.add(new Rendimento(empresa, area, rodadaAtual, rodadaAtual + atraso,
                        investido, percentual));
            }
            empresa.limparInvestimentos();

            // 3) credita o que venceu (na última rodada, tudo que sobrou é liquidado)
            List<Rendimento> recebidos = new ArrayList<>();
            Iterator<Rendimento> it = pendencias.iterator();
            while (it.hasNext()) {
                Rendimento r = it.next();
                boolean venceu = r.rodadaPagamento() <= rodadaAtual || rodadaAtual == totalRodadas;
                if (r.empresa() == empresa && venceu) {
                    empresa.aplicarImpacto(r.valor());
                    recebidos.add(r);
                    it.remove();
                }
            }

            int aCaminho = (int) pendencias.stream().filter(p -> p.empresa() == empresa).count();
            resultados.add(new Resultado(empresa, base, impactoEvento, recebidos, aCaminho));
        }
        return resultados;
    }

    /** 50% de chance de render na hora, 30% na rodada seguinte, 20% daqui a duas rodadas. */
    private int sortearAtraso() {
        int sorte = random.nextInt(10);
        if (sorte < 5) {
            return 0;
        }
        return sorte < 8 ? 1 : 2;
    }

    public void avancarRodada() {
        rodadaAtual++;
    }

    public boolean terminou() {
        return rodadaAtual > totalRodadas;
    }

    public List<Empresa> getRanking() {
        return empresas.stream()
                .sorted(Comparator.comparing(Empresa::getPatrimonio).reversed())
                .toList();
    }

    public List<Empresa> getEmpresas() {
        return empresas;
    }

    public int getRodadaAtual() {
        return rodadaAtual;
    }

    public int getTotalRodadas() {
        return totalRodadas;
    }
}