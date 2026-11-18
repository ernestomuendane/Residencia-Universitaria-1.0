package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Cama {

    private final int idCama;
    private EstadoCama estado;
    private final Quarto quarto;
    private final List<Contracto> contractos = new ArrayList<>(); // histórico: vários contractos, um de cada vez

    public Cama(int idCama, EstadoCama estado, Quarto quarto) {
        this.idCama = idCama;
        this.estado = Objects.requireNonNull(estado, "Estado obrigatório");
        this.quarto = Objects.requireNonNull(quarto, "Quarto obrigatório");

        quarto.registarCama(this);
    }

    public Cama(int idCama, Quarto quarto) {
        this(idCama, EstadoCama.DISPONIVEL, quarto);
    }

    public int getIdCama() {
        return idCama;
    }

    public EstadoCama getEstado() {
        return estado;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public boolean estaDisponivel() {
        return estado == EstadoCama.DISPONIVEL;
    }

    public List<Contracto> getContractos() {
        return Collections.unmodifiableList(contractos);
    }

    public Optional<Contracto> getContractoEmCurso() {
        for (Contracto c : contractos) {
            if (c.getEstado() != EstadoContracto.CANCELADO) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public void colocarEmManutencao() {
        if (estado != EstadoCama.DISPONIVEL && estado != EstadoCama.MANUTENCAO) {
            throw new IllegalStateException("A cama " + idCama + " está " + estado.toString().toLowerCase()
                    + ": só uma cama disponível pode ir para manutenção");
        }
        estado = EstadoCama.MANUTENCAO;
    }

    public void sairDeManutencao() {
        if (estado != EstadoCama.MANUTENCAO && estado != EstadoCama.DISPONIVEL) {
            throw new IllegalStateException("A cama " + idCama + " está " + estado.toString().toLowerCase()
                    + ": o estado é gerido pelo contracto");
        }
        estado = EstadoCama.DISPONIVEL;
    }

    void reservar() {
        if (estado != EstadoCama.DISPONIVEL) {
            throw new IllegalStateException("A cama " + idCama + " não está disponível (" + estado + ")");
        }
        estado = EstadoCama.RESERVADA;
    }

    void ocupar() {
        if (estado != EstadoCama.RESERVADA) {
            throw new IllegalStateException("A cama " + idCama + " não está reservada (" + estado + ")");
        }
        estado = EstadoCama.OCUPADA;
    }

    void libertar() {
        estado = EstadoCama.DISPONIVEL;
    }

    void registarContracto(Contracto contracto) {
        if (!contractos.contains(contracto)) {
            contractos.add(contracto);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cama)) return false;
        return idCama == ((Cama) o).idCama;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCama);
    }

    @Override
    public String toString() {
        return "Cama{idCama=" + idCama +
                ", estado=" + estado +
                ", quarto=" + quarto.getNumero() + '}';
    }
}
