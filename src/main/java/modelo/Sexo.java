package modelo;

public enum Sexo {
    MASCULINO("Masculino"),
    FEMININO("Feminino");

    private final String rotulo;

    Sexo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
