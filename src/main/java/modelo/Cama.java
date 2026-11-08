package modelo;

public class Cama {

    private int idCama;
    private String estado;
    private Quarto quarto;

    public Cama(int idCama, String estado, Quarto quarto){
        this.idCama = idCama;
        this.estado = estado;
        this.quarto = quarto;
    }

    public int getIdCama() {
        return idCama;
    }

    public String getEstado(){
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Quarto getQuarto() {
        return quarto;
    }
}
