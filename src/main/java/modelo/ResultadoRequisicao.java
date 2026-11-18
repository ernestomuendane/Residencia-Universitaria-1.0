package modelo;

public enum ResultadoRequisicao {
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    NAO_APROVADO("Não aprovado");

    private final String rotulo;

    ResultadoRequisicao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
