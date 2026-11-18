package modelo;

import java.time.LocalDateTime;
import java.util.Objects;

public class Gestao {

    private final Administrador administrador;
    private final Residencia residencia;
    private final LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private EstadoGestao estado;

    public Gestao(Administrador administrador, Residencia residencia,
                  LocalDateTime dataInicio, LocalDateTime dataFim) {
        this(administrador, residencia, dataInicio, dataFim, EstadoGestao.ATIVA);
    }

    public Gestao(Administrador administrador, Residencia residencia,
                  LocalDateTime dataInicio, LocalDateTime dataFim, EstadoGestao estado) {
        Objects.requireNonNull(administrador, "Administrador obrigatório");
        Objects.requireNonNull(residencia, "Residência obrigatória");
        Objects.requireNonNull(dataInicio, "Data de início obrigatória");
        Objects.requireNonNull(estado, "Estado obrigatório");
        if (dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior ao início");
        }
        if (estado == EstadoGestao.ATIVA) {
            administrador.validarGestaoPossivel();
            residencia.validarGestaoPossivel();
        }
        this.administrador = administrador;
        this.residencia = residencia;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.estado = estado;

        administrador.registarGestao(this);
        residencia.registarGestao(this);
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public EstadoGestao getEstado() {
        return estado;
    }

    public boolean estaAtiva() {
        return estado == EstadoGestao.ATIVA;
    }

    public void encerrar(LocalDateTime fim) {
        Objects.requireNonNull(fim, "Data de fim obrigatória");
        if (!estaAtiva()) {
            throw new IllegalStateException("A gestão já está encerrada");
        }
        if (fim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior ao início");
        }
        this.dataFim = fim;
        this.estado = EstadoGestao.ENCERRADA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gestao)) return false;
        Gestao g = (Gestao) o;
        return administrador.equals(g.administrador)
                && residencia.equals(g.residencia)
                && dataInicio.equals(g.dataInicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(administrador, residencia, dataInicio);
    }

    @Override
    public String toString() {
        return "Gestao{administrador=" + administrador.getIdAdministrador() +
                ", residencia=" + residencia.getIdResidencia() +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", estado=" + estado + '}';
    }
}

