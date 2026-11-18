package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Contracto {

    private final int idContracto;
    private final LocalDateTime dataAssinatura;
    private final LocalDateTime dataInicial;
    private final LocalDateTime dataFinal;
    private EstadoContracto estado;
    private final Requisicao requisicao;
    private final Cama cama;
    private final Administrador administrador;
    private Pagamento pagamento;

    public Contracto(int idContracto, LocalDateTime dataAssinatura, LocalDateTime dataInicial,
                     LocalDateTime dataFinal, Requisicao requisicao, Cama cama, Administrador administrador) {
        this(idContracto, dataAssinatura, dataInicial, dataFinal, EstadoContracto.PENDENTE,
                requisicao, cama, administrador, true);
    }

    public Contracto(int idContracto, LocalDateTime dataAssinatura, LocalDateTime dataInicial,
                     LocalDateTime dataFinal, EstadoContracto estado, Requisicao requisicao,
                     Cama cama, Administrador administrador) {
        this(idContracto, dataAssinatura, dataInicial, dataFinal, estado, requisicao, cama, administrador, false);
    }

    private Contracto(int idContracto, LocalDateTime dataAssinatura, LocalDateTime dataInicial,
                      LocalDateTime dataFinal, EstadoContracto estado, Requisicao requisicao,
                      Cama cama, Administrador administrador, boolean novo) {
        Objects.requireNonNull(estado, "Estado obrigatório");
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
        if (novo) {
            Objects.requireNonNull(administrador, "Administrador obrigatório");
            if (!administrador.gere(requisicao.getResidencia())) {
                throw new IllegalStateException("O administrador não é o responsável pela residência "
                        + requisicao.getResidencia().getNome());
            }
            cama.reservar(); // falha se a cama não estiver disponível; só depois se liga tudo
        }

        this.idContracto = idContracto;
        this.dataAssinatura = dataAssinatura != null ? dataAssinatura : LocalDateTime.now();
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.estado = estado;
        this.requisicao = requisicao;
        this.cama = cama;
        this.administrador = administrador;

        requisicao.setContracto(this);
        cama.registarContracto(this);
    }

    public int getId() {
        return idContracto;
    }

    public LocalDateTime getDataAssinatura() {
        return dataAssinatura;
    }

    public LocalDateTime getDataInicial() {
        return dataInicial;
    }

    public LocalDateTime getDataFinal() {
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

    public Administrador getAdministrador() {
        return administrador;
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


    public void cancelar(Administrador responsavel) {
        Objects.requireNonNull(responsavel, "Administrador obrigatório");
        if (estado == EstadoContracto.CANCELADO) {
            throw new IllegalStateException("O contracto já está cancelado");
        }
        if (!responsavel.gere(getResidencia())) {
            throw new IllegalStateException("O administrador não é o responsável pela residência "
                    + getResidencia().getNome());
        }
        cama.libertar();
        estado = EstadoContracto.CANCELADO;
    }

    public boolean deveSerCancelado() {
        return estado == EstadoContracto.PENDENTE && pagamento == null;
    }


    public Pagamento getPagamento() {
        return pagamento;
    }

    public boolean temPagamento() {
        return pagamento != null;
    }

    public BigDecimal getTotalPago() {
        return pagamento == null ? BigDecimal.ZERO : pagamento.getValorTotal();
    }

    void registarPagamento(Pagamento novo) {
        if (estado == EstadoContracto.CANCELADO) {
            throw new IllegalStateException("Não é possível pagar um contracto cancelado");
        }
        if (pagamento != null) {
            throw new IllegalStateException("O contracto já tem pagamento");
        }
        if (estado == EstadoContracto.PENDENTE) {
            cama.ocupar();
            estado = EstadoContracto.ATIVO;
        }
        pagamento = novo;
    }

    void restaurarPagamento(Pagamento restaurado) {
        this.pagamento = restaurado;
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