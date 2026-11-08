public enum Area {
    MARKETING("Marketing"),
    TECNOLOGIA("Tecnologia"),
    TRANSPORTE("Transporte"),
    MATERIA_PRIMA("Matéria-prima");

    private final String nome;

    Area(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}