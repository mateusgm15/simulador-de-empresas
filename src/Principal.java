import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Principal {

    private static final NumberFormat MOEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private static final Scanner entrada = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== SIMULADOR DE INVESTIMENTOS EMPRESARIAIS ===\n");

        BigDecimal capital = lerCapital();
        int qtdEmpresas = lerInteiro("Quantidade de empresas (2 a 8): ", 2, 8);
        int rodadas = lerInteiro("Quantidade de rodadas (1 a 20): ", 1, 20);

        List<Empresa> empresas = new ArrayList<>();
        for (int i = 1; i <= qtdEmpresas; i++) {
            empresas.add(new Empresa(lerTexto("Nome da empresa " + i + ": "), capital));
        }

        Simulador simulador = new Simulador(empresas, rodadas, CatalogoEventos.criar());

        while (!simulador.terminou()) {
            jogarRodada(simulador);
            simulador.avancarRodada();
        }

        mostrarRanking("RANKING FINAL", simulador);
    }

    private static void jogarRodada(Simulador simulador) {
        System.out.println("\n########## RODADA " + simulador.getRodadaAtual()
                + " de " + simulador.getTotalRodadas() + " ##########");

        for (Empresa empresa : simulador.getEmpresas()) {
            coletarInvestimentos(empresa);
            limparTela(); // o próximo grupo não vê as escolhas do anterior
        }

        System.out.println("Investimentos encerrados. Pressione ENTER para sortear o evento...");
        entrada.nextLine();

        Evento evento = simulador.sortearEvento();
        if (evento != null) {
            System.out.println("\n>>> EVENTO: " + evento);
        } else {
            System.out.println("\n>>> Sem eventos disponíveis nesta rodada.");
        }

        for (Simulador.Resultado r : simulador.processarRodada(evento)) {
            System.out.println("  " + r.empresa().getNome());
            if (evento != null) {
                System.out.printf("    evento: %s (investido em %s: %s)%n",
                        formatar(r.impactoEvento()), evento.getArea(), formatar(r.valorBase()));
            }
            System.out.printf("    rendimentos recebidos: %s | a caminho: %d%n",
                    formatar(r.totalRecebido()), r.rendimentosACaminho());
            for (Simulador.Rendimento d : r.recebidos()) {
                System.out.printf("      %-14s (rodada %d) %s %+d%% = %s%n", d.area(), d.rodadaOrigem(),
                        formatar(d.investido()), d.percentual(), formatar(d.valor()));
            }
        }

        mostrarRanking("CLASSIFICAÇÃO PARCIAL", simulador);
    }

    private static void coletarInvestimentos(Empresa empresa) {
        while (true) {
            System.out.println("\n--- " + empresa.getNome().toUpperCase()
                    + " | patrimônio: " + formatar(empresa.getPatrimonio()) + " ---");

            for (Area area : Area.values()) {
                while (true) {
                    BigDecimal valor = lerValor("  " + area + " (livre: "
                            + formatar(empresa.getCaixaLivre()) + "): ");
                    try {
                        empresa.investir(area, valor);
                        break;
                    } catch (IllegalArgumentException e) {
                        System.out.println("  Erro: " + e.getMessage());
                    }
                }
            }

            mostrarResumo(empresa);
            System.out.print("ENTER para confirmar ou R para refazer: ");
            if (!entrada.nextLine().trim().equalsIgnoreCase("r")) {
                return;
            }
            empresa.limparInvestimentos();
        }
    }

    private static void mostrarResumo(Empresa empresa) {
        System.out.println("\n  Resumo dos investimentos de " + empresa.getNome() + ":");
        for (Area area : Area.values()) {
            System.out.printf("    %-14s %s%n", area, formatar(empresa.getInvestimento(area)));
        }
        BigDecimal naoInvestido = empresa.getCaixaLivre();
        System.out.println("    Total investido: " + formatar(empresa.getPatrimonio().subtract(naoInvestido))
                + " | fica no caixa: " + formatar(naoInvestido));
    }

    private static void mostrarRanking(String titulo, Simulador simulador) {
        System.out.println("\n=== " + titulo + " ===");
        int posicao = 1;
        for (Empresa e : simulador.getRanking()) {
            System.out.printf("%dº %-20s patrimônio: %-16s ganhos: %-14s perdas: %s%n",
                    posicao++, e.getNome(), formatar(e.getPatrimonio()),
                    formatar(e.getGanhos()), formatar(e.getPerdas()));
        }
    }

    // ---------- utilitários de entrada ----------

    private static BigDecimal lerCapital() {
        while (true) {
            BigDecimal valor = lerValor("Capital inicial de cada empresa (R$): ");
            if (valor.signum() > 0) {
                return valor;
            }
            System.out.println("O capital deve ser maior que zero.");
        }
    }

    private static BigDecimal lerValor(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = entrada.nextLine().trim();
            try {
                BigDecimal valor = converterValor(texto);
                if (valor.signum() < 0) {
                    System.out.println("Não aceitamos valores negativos.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Exemplos: 20000, 20.000 ou 20.000,50");
            }
        }
    }

    /**
     * Converte o texto digitado em valor. "." e "," funcionam igual:
     * - separador seguido de exatamente 3 dígitos = milhar (30,000 / 30.000 / 1.250.000)
     * - separador seguido de 1 ou 2 dígitos = centavos (20,5 / 20.50 / 20.000,50)
     */
    private static BigDecimal converterValor(String texto) {
        String t = texto.replace("R$", "").replace(" ", "");
        int ultimo = Math.max(t.lastIndexOf('.'), t.lastIndexOf(','));
        if (ultimo >= 0) {
            String antes = t.substring(0, ultimo).replace(".", "").replace(",", "");
            String depois = t.substring(ultimo + 1);
            t = depois.length() == 3 ? antes + depois : antes + "." + depois;
        }
        return new BigDecimal(t);
    }

    private static int lerInteiro(String mensagem, int min, int max) {
        while (true) {
            System.out.print(mensagem);
            try {
                int valor = Integer.parseInt(entrada.nextLine().trim());
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem abaixo
            }
            System.out.println("Digite um número entre " + min + " e " + max + ".");
        }
    }

    private static String lerTexto(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = entrada.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("O nome não pode ficar vazio.");
        }
    }

    private static String formatar(BigDecimal valor) {
        return MOEDA.format(valor);
    }

    private static void limparTela() {
        System.out.println("\n".repeat(40));
    }
}