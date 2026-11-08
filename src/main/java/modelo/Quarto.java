package modelo;

public class Quarto {

    private int idQuarto;
    private int numero;
    private Categoria categoria;
    private Residencia residencia;

    public Quarto(int idQuarto, int numero, Categoria categoria, Residencia residencia) {
        this.idQuarto = idQuarto;
        this.numero = numero;
        this.categoria = categoria;
        this.residencia = residencia;
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

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Residencia getResidencia() {
        return residencia;
    }
}
