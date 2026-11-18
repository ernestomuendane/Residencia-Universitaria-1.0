package modelo;

import java.time.LocalDateTime;
import java.util.Objects;

public class Requisicao {

    private final int idRequisicao;
    private final LocalDateTime data;
    private ResultadoRequisicao resultado;
    private final Estudante estudante;
    private final Residencia residencia;
    private Administrador administrador;
    private Contracto contracto;

    public Requisicao(int idRequisicao, LocalDateTime data, Estudante estudante, Residencia residencia) {
        this(idRequisicao, data, ResultadoRequisicao.PENDENTE, estudante, residencia);
    }

    public Requisicao(int idRequisicao, LocalDateTime data, ResultadoRequisicao resultado,
                      Estudante estudante, Residencia residencia) {
        this.idRequisicao = idRequisicao;
        this.data = data != null ? data : LocalDateTime.now();
        this.resultado = Objects.requireNonNull(resultado, "Resultado obrigatório");
        this.estudante = Objects.requireNonNull(estudante, "Estudante obrigatório");
        this.residencia = Objects.requireNonNull(residencia, "Residência obrigatória");

        estudante.registarRequisicao(this);
        residencia.registarRequisicao(this);
    }

    public Requisicao(int idRequisicao, LocalDateTime data, ResultadoRequisicao resultado,
                      Estudante estudante, Residencia residencia, Administrador administrador) {
        this(idRequisicao, data, resultado, estudante, residencia);
        if (administrador != null) {
            this.administrador = administrador;
            administrador.registarRequisicao(this);
        }
    }

    public int getIdRequisicao() {
        return idRequisicao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public ResultadoRequisicao getResultado() {
        return resultado;
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public Contracto getContracto() {
        return contracto;
    }

    public boolean estaPendente() {
        return resultado == ResultadoRequisicao.PENDENTE;
    }

    public boolean estaAprovada() {
        return resultado == ResultadoRequisicao.APROVADO;
    }

    void decidir(Administrador admin, ResultadoRequisicao novoResultado) {
        Objects.requireNonNull(admin, "Administrador obrigatório");
        if (novoResultado == ResultadoRequisicao.PENDENTE) {
            throw new IllegalArgumentException("A decisão deve ser APROVADO ou NAO_APROVADO");
        }
        if (!estaPendente()) {
            throw new IllegalStateException("A requisição " + idRequisicao + " já foi decidida");
        }
        if (!admin.gere(residencia)) {
            throw new IllegalStateException("O administrador não gere a residência " + residencia.getNome());
        }
        this.administrador = admin;
        this.resultado = novoResultado;
        admin.registarRequisicao(this);
    }

    void setContracto(Contracto contracto) {
        if (!estaAprovada()) {
            throw new IllegalStateException("Só requisições aprovadas geram contracto");
        }
        if (this.contracto != null) {
            throw new IllegalStateException("A requisição já tem contracto");
        }
        this.contracto = contracto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Requisicao)) return false;
        return idRequisicao == ((Requisicao) o).idRequisicao;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idRequisicao);
    }

    @Override
    public String toString() {
        return "Requisicao{idRequisicao=" + idRequisicao +
                ", data=" + data +
                ", resultado=" + resultado +
                ", estudante=" + estudante.getIdEstudante() +
                ", residencia=" + residencia.getIdResidencia() + '}';
    }
}