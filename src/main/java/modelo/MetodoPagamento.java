package modelo;

public enum MetodoPagamento {
    NUMERARIO("Numerário"),
    TRANSFERENCIA("Transferência"),
    CARTAO("Cartão"),
    MOBILE_MONEY("Mobile money");

    private final String rotulo;

    MetodoPagamento(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
