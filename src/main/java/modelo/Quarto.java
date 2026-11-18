package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Quarto {

    private final int idQuarto;
    private final int numero;
    private Categoria categoria;
    private final Residencia residencia;
    private final List<Cama> camas = new ArrayList<>();

    public Quarto(int idQuarto, int numero, Categoria categoria, Residencia residencia) {
        if (numero <= 0) {
            throw new IllegalArgumentException("O número do quarto deve ser positivo");
        }
        this.idQuarto = idQuarto;
        this.numero = numero;
        this.categoria = Objects.requireNonNull(categoria, "Categoria obrigatória");
        this.residencia = Objects.requireNonNull(residencia, "Residência obrigatória");

        residencia.registarQuarto(this);
        categoria.registarQuarto(this);
    }

    public int getIdQuarto() {
        return idQuarto;
    }

    public int getNumero() {
        return numero;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria nova) {
        Objects.requireNonNull(nova, "Categoria obrigatória");
        if (camas.size() > nova.getCapacidade()) {
            throw new IllegalStateException("O quarto tem mais camas do que a capacidade da nova categoria");
        }
        this.categoria.removerQuarto(this);
        this.categoria = nova;
        nova.registarQuarto(this);
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public Cama adicionarCama(int idCama) {
        return new Cama(idCama, EstadoCama.DISPONIVEL, this);
    }

    public List<Cama> getCamas() {
        return Collections.unmodifiableList(camas);
    }

    public List<Cama> getCamasLivres() {
        return camas.stream().filter(Cama::estaDisponivel).collect(Collectors.toList());
    }

    public boolean estaCheio() {
        return camas.size() >= categoria.getCapacidade();
    }

    void registarCama(Cama cama) {
        if (camas.contains(cama)) {
            return;
        }
        if (estaCheio()) {
            throw new IllegalStateException("Capacidade do quarto " + numero + " excedida ("
                    + categoria.getCapacidade() + ")");
        }
        camas.add(cama);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quarto)) return false;
        return idQuarto == ((Quarto) o).idQuarto;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idQuarto);
    }

    @Override
    public String toString() {
        return "Quarto{idQuarto=" + idQuarto +
                ", numero=" + numero +
                ", categoria=" + categoria.getNome() +
                ", residencia=" + residencia.getNome() + '}';
    }
}
