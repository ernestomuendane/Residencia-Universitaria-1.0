package modelo;

public enum EstadoGestao {
    ATIVA("Ativa"),
    ENCERRADA("Encerrada");

    private final String rotulo;

    EstadoGestao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}

