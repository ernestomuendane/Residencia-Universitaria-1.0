package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Categoria {

    private int idCategoria;
    private String nome;
    private int capacidade;
    private BigDecimal preco;
    private final List<Quarto> quartos = new ArrayList<>();

    public Categoria(int idCategoria, String nome, int capacidade, BigDecimal preco) {
        this.idCategoria = idCategoria;
        setNome(nome);
        setCapacidade(capacidade);
        setPreco(preco);
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser positiva");
        }
        for (Quarto q : quartos) {
            if (q.getCamas().size() > capacidade) {
                throw new IllegalStateException("O quarto " + q.getNumero()
                        + " já tem mais camas do que a nova capacidade");
            }
        }
        this.capacidade = capacidade;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        if (preco.signum() < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo");
        }
        this.preco = preco;
    }

    public List<Quarto> getQuartos() {
        return Collections.unmodifiableList(quartos);
    }

    void registarQuarto(Quarto quarto) {
        if (!quartos.contains(quarto)) {
            quartos.add(quarto);
        }
    }

    void removerQuarto(Quarto quarto) {
        quartos.remove(quarto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        return idCategoria == ((Categoria) o).idCategoria;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCategoria);
    }

    @Override
    public String toString() {
        return "Categoria{idCategoria=" + idCategoria +
                ", nome='" + nome + '\'' +
                ", capacidade=" + capacidade +
                ", preco=" + preco + '}';
    }
}
