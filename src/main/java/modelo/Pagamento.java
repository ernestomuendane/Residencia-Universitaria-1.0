package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Pagamento {

    private final int idPagamento;
    private final LocalDateTime data = LocalDateTime.now();
    private MetodoPagamento metodo;
    private int totalMeses;
    private BigDecimal valorTotal;
    private final Contracto contracto;

    public Pagamento(int idPagamento, LocalDateTime data, MetodoPagemento metodo, int totalMeses, BigDecimal valorTotal, Contracto contracto){
        this.idPagamento = idPagamento;
        this.data = data != null ? data : LocalDateTime.now();
        this.contracto = Objects.requireNonNull(contracto, "Contracto Obrigatório!");
        setMetodo(metodo);
        setTotalMeses(totalMeses);

        contracto.registarPagamento((this));
    }

    public int getIdPagamento(){
        return  idPagamento;
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

    public int getTotalMeses(){
        return totalMeses;
    }

    public void setTotalMeses(int totalMeses) {
        if (totalMeses <= 0) {
            throw new IllegalArgumentException("O número de meses deve ser positivo");
        }
        this.totalMeses = totalMeses;
        this.valorTotal = contracto.getPrecoMensal().multiply(BigDecimal.valueOf(totalMeses));
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
