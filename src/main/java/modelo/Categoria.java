package modelo;

import java.math.BigDecimal;

public class Categoria {

    private int idCategoria;
    private String nome;
    private int capacidade;
    private BigDecimal preco;

    public Categoria(int idCategoria, String nome, int capacidade, BigDecimal preco){
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.capacidade = capacidade;
        this.preco = preco;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public BigDecimal getPreco(){
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
