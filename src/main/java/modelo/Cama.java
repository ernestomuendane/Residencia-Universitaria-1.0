package modelo;

import java.util.Objects;

public class Cama {

    private final int idCama;
    private EstadoCama estado;
    private final Quarto quarto;
    private final List<Contracto> contractos = new ArrayList<>();

    public Cama(int idCama, EstadoCama estado, Quarto quarto) {
        this.idCama = idCama;
        this.estado = Objects.requireNonNull(estado, "Estado obrigatório");
        this.quarto = Objects.requireNonNull(quarto, "Quarto obrigatório");

        quarto.registarCama(this);
    }

    public Cama(int idCama, Quarto quarto) {
        this(idCama, EstadoCama.LIVRE, quarto);
    }

    public int getIdCama() {
        return idCama;
    }

    public EstadoCama getEstado() {
        return estado;
    }

    public void setEstado(EstadoCama novo) {
        Objects.requireNonNull(novo, "Estado obrigatório");
        if (getContractoAtivo().isPresent() && novo != EstadoCama.OCUPADA) {
            throw new IllegalStateException("A cama tem um contracto ativo");
        }
        this.estado = novo;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public boolean estaLivre() {
        return estado == EstadoCama.LIVRE;
    }

    public List<Contracto> getContractos() {
        return Collections.unmodifiableList(contractos);
    }

    public Optional<Contracto> getContractoAtivo() {
        return contractos.stream()
                .filter(c -> c.getEstado() == EstadoContracto.ATIVO)
                .findFirst();
    }

    void ocupar() {
        if (estado != EstadoCama.LIVRE) {
            throw new IllegalStateException("A cama " + idCama + " não está livre (" + estado + ")");
        }
        estado = EstadoCama.OCUPADA;
    }

    void libertar() {
        estado = EstadoCama.LIVRE;
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
