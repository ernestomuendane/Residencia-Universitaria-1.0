package modelo;

public enum EstadoContracto {
    PENDENTE("Pendente"),
    ATIVO("Ativo"),
    CANCELADO("Cancelado");

    private final String rotulo;

    EstadoContracto(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
