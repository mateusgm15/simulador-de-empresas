import java.util.ArrayList;
import java.util.List;

public class CatalogoEventos {

    public static List<Evento> criar() {
        List<Evento> eventos = new ArrayList<>();

        // Marketing
        eventos.add(new Evento("Mudança leve na regulamentação de privacidade de dados", Area.MARKETING, -6));
        eventos.add(new Evento("Ação promocional da concorrência reduz o engajamento", Area.MARKETING, -7));
        eventos.add(new Evento("Redução na comissão de plataformas publicitárias", Area.MARKETING, 10));
        eventos.add(new Evento("Atualização no algoritmo de buscas reduz tráfego orgânico", Area.MARKETING, -9));

        // Tecnologia
        eventos.add(new Evento("Incentivo fiscal para modernização de software", Area.TECNOLOGIA, 12));
        eventos.add(new Evento("Lentidão em servidores de nuvem no horário de pico", Area.TECNOLOGIA, -5));
        eventos.add(new Evento("Novas ferramentas de automação otimizam rotinas", Area.TECNOLOGIA, 13));
        eventos.add(new Evento("Incidente de phishing exige auditoria e pausa preventiva", Area.TECNOLOGIA, -10));

        // Matéria-prima
        eventos.add(new Evento("Oscilação nos custos de ligas metálicas e polímeros", Area.MATERIA_PRIMA, -11));
        eventos.add(new Evento("Desconto por compra em volume de fornecedor de insumos", Area.MATERIA_PRIMA, 15));
        eventos.add(new Evento("Restrição na oferta de insumos importados", Area.MATERIA_PRIMA, -14));

        // Transporte
        eventos.add(new Evento("Paralisação de 48h em rodovia secundária", Area.TRANSPORTE, -8));
        eventos.add(new Evento("Nova alça de acesso rodoviário diminui tempo de viagem", Area.TRANSPORTE, 9));
        eventos.add(new Evento("Isenção de pedágio noturno para frotas de carga", Area.TRANSPORTE, 8));
        eventos.add(new Evento("Otimização de rotas reduz consumo de combustível", Area.TRANSPORTE, 15));

        return eventos;
    }
}