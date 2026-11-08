package modelo;

public class Residencia {
    private int idResidencia;
    private  String nome;
    private String tipo;

    public int getIdResidencia(){
        return idResidencia;
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public String getTipo(){
        return tipo;
    }

    public void setTipo( String tipo) {
        this.tipo = tipo;
    }
}
