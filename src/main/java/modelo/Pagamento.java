package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Pagamento {

    private final int idPagamento;
    private final LocalDateTime data;
    private MetodoPagamento metodo;
    private int totalMeses;
    private BigDecimal valorTotal;
    private final Contracto contracto;

    public Pagamento(int idPagamento, LocalDateTime data, MetodoPagamento metodo,
                     int totalMeses, Contracto contracto) {
        this.idPagamento = idPagamento;
        this.data = data != null ? data : LocalDateTime.now();
        this.contracto = Objects.requireNonNull(contracto, "Contracto obrigatório");
        setMetodo(metodo);
        validarMeses(totalMeses);
        this.totalMeses = totalMeses;
        this.valorTotal = contracto.getPrecoMensal().multiply(BigDecimal.valueOf(totalMeses));

        contracto.registarPagamento(this);
    }

    private Pagamento(int idPagamento, LocalDateTime data, Contracto contracto, BigDecimal valorTotal) {
        this.idPagamento = idPagamento;
        this.data = data;
        this.contracto = contracto;
        this.valorTotal = valorTotal;
    }

    public static Pagamento restaurar(int idPagamento, LocalDateTime data, MetodoPagamento metodo,
                                      int totalMeses, BigDecimal valorTotal, Contracto contracto) {
        Objects.requireNonNull(contracto, "Contracto obrigatório");
        Objects.requireNonNull(valorTotal, "Valor obrigatório");
        validarMeses(totalMeses);
        Pagamento p = new Pagamento(idPagamento, data, contracto, valorTotal);
        p.setMetodo(metodo);
        p.totalMeses = totalMeses;
        contracto.restaurarPagamento(p);
        return p;
    }

    private static void validarMeses(int meses) {
        if (meses <= 0) {
            throw new IllegalArgumentException("O número de meses deve ser positivo");
        }
    }

    public int getIdPagamento() {
        return idPagamento;
    }

    public LocalDateTime getData() {
        return data;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPagamento metodo) {
        this.metodo = Objects.requireNonNull(metodo, "Método obrigatório");
    }

    public int getTotalMeses() {
        return totalMeses;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Contracto getContracto() {
        return contracto;
    }

    public Estudante getEstudante() {
        return contracto.getEstudante();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pagamento)) return false;
        return idPagamento == ((Pagamento) o).idPagamento;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idPagamento);
    }

    @Override
    public String toString() {
        return "Pagamento{idPagamento=" + idPagamento +
                ", data=" + data +
                ", metodo=" + metodo +
                ", totalMeses=" + totalMeses +
                ", valorTotal=" + valorTotal +
                ", contracto=" + contracto.getId() + '}';
    }
}
