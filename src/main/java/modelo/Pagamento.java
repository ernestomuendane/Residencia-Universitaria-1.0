package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pagamento {

    private int idPagamento;
    private LocalDateTime data = LocalDateTime.now();
    private String metodo;
    private int totalMeses;
    private BigDecimal valorTotal;
    private Contracto contracto;

    public Pagamento(int idPagamento, LocalDateTime data, String metodo, int totalMeses, BigDecimal valorTotal, Contracto contracto){
        this.idPagamento = idPagamento;
        this.data = data;
        this.metodo = metodo;
        this.totalMeses = totalMeses;
        this.valorTotal = valorTotal;
        this.contracto = contracto;
    }

    public int getIdPagamento(){
        return  idPagamento;
    }

    public LocalDateTime getData() {
        return data;
    }

    public String getMetodo() {
        return metodo;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Contracto getContracto() {
        return contracto;
    }


}
