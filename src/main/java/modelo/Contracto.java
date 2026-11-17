package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Contracto {

    private final int idContracto;
    private final LocalDate dataAssinatura;
    private final LocalDate dataInicial;
    private final LocalDate dataFinal;
    private EstadoContracto estado;
    private final Requisicao requisicao;
    private final Cama cama;
    private final List<Pagamento> pagamentos = new ArrayList<>();

    public Contracto(int idContracto, LocalDate dataAssinatura, LocalDate dataInicial,
                     LocalDate dataFinal, Requisicao requisicao, Cama cama) {
        Objects.requireNonNull(requisicao, "Requisição obrigatória");
        Objects.requireNonNull(cama, "Cama obrigatória");
        Objects.requireNonNull(dataInicial, "Data inicial obrigatória");
        Objects.requireNonNull(dataFinal, "Data final obrigatória");

        if (!dataFinal.isAfter(dataInicial)) {
            throw new IllegalArgumentException("A data final deve ser posterior à data inicial");
        }
        if (!requisicao.estaAprovada()) {
            throw new IllegalStateException("A requisição não está aprovada");
        }
        if (requisicao.getContracto() != null) {
            throw new IllegalStateException("A requisição já tem contracto");
        }
        if (!cama.getQuarto().getResidencia().equals(requisicao.getResidencia())) {
            throw new IllegalArgumentException("A cama não pertence à residência da requisição");
        }

        this.idContracto = idContracto;
        this.dataAssinatura = dataAssinatura != null ? dataAssinatura : LocalDate.now();
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.estado = EstadoContracto.PENDENTE;
        this.requisicao = requisicao;
        this.cama = cama;

        requisicao.setContracto(this);
        cama.registarContracto(this);
    }

    public int getId() {
        return idContracto;
    }

    public LocalDate getDataAssinatura() {
        return dataAssinatura;
    }

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public EstadoContracto getEstado() {
        return estado;
    }

    public Requisicao getRequisicao() {
        return requisicao;
    }

    public Cama getCama() {
        return cama;
    }

    public Estudante getEstudante() {
        return requisicao.getEstudante();
    }

    public Residencia getResidencia() {
        return requisicao.getResidencia();
    }

    public BigDecimal getPrecoMensal() {
        return cama.getQuarto().getCategoria().getPreco();
    }

    public void ativar() {
        if (estado != EstadoContracto.PENDENTE) {
            throw new IllegalStateException("Só um contracto pendente pode ser ativado");
        }
        cama.ocupar();
        estado = EstadoContracto.ATIVO;
    }

    public void terminar() {
        if (estado != EstadoContracto.ATIVO) {
            throw new IllegalStateException("Só um contracto ativo pode ser terminado");
        }
        estado = EstadoContracto.TERMINADO;
        cama.libertar();
    }

    public void cancelar() {
        if (estado == EstadoContracto.TERMINADO || estado == EstadoContracto.CANCELADO) {
            throw new IllegalStateException("O contracto já está " + estado);
        }
        if (estado == EstadoContracto.ATIVO) {
            cama.libertar();
        }
        estado = EstadoContracto.CANCELADO;
    }

    public List<Pagamento> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }

    public BigDecimal getTotalPago() {
        return pagamentos.stream()
                .map(Pagamento::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void registarPagamento(Pagamento pagamento) {
        if (estado == EstadoContracto.TERMINADO || estado == EstadoContracto.CANCELADO) {
            throw new IllegalStateException("Não é possível pagar um contracto " + estado);
        }
        if (!pagamentos.contains(pagamento)) {
            pagamentos.add(pagamento);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contracto)) return false;
        return idContracto == ((Contracto) o).idContracto;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idContracto);
    }

    @Override
    public String toString() {
        return "Contracto{idContracto=" + idContracto +
                ", dataAssinatura=" + dataAssinatura +
                ", dataInicial=" + dataInicial +
                ", dataFinal=" + dataFinal +
                ", estado=" + estado +
                ", cama=" + cama.getIdCama() + '}';
    }
}
