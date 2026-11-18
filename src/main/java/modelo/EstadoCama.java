package modelo;

public enum EstadoCama {
    DISPONIVEL("Disponível"),
    RESERVADA("Reservada"),
    OCUPADA("Ocupada"),
    MANUTENCAO("Em manutenção");

    private final String rotulo;

    EstadoCama(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}