package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Gestao {
    private final Administrador administrador;
    private final Residencia residencia;
    private LocalDateTime dataInicio = LocalDateTime.now();
    private LocalDateTime dataFim;
    private EstadoGestao estado;

    public Gestao(Administrador administrador, Residencia residencia,LocalDateTime dataInicio, LocalDateTime dataFim, EstadoGestao estado){
        this.administrador = Objects.requireNonNull(administrador, "Administrador obrigatório");
        this.residencia = Objects.requireNonNull(residencia, "Residência obrigatória");
        this.dataInicio = Objects.requireNonNull(dataInicio, "Data de início obrigatória");
        if (dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior ao início");
        }
        this.dataFim = dataFim;
        this.estado = EstadoGestao.ATIVA;

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

    public EstadoGestao getEstado() {
        return estado;
    }

    public void encerrar(LocalDateTime fim) {
        Objects.requireNonNull(fim, "Data de fim obrigatória");
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
        return "Gestao{" +
                "administrador=" + administrador +
                ", residencia=" + residencia +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", estado=" + estado +
                '}';
    }
}
