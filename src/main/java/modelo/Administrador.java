package modelo;


import java.sql.Date;

public class Administrador extends Utente{
    private int idAdministrador;
    private Date dataInicio;
    private String grau;
    public Administrador(int idAdministrador, String nome, String telefone, String email, Date dataInicio, String grau){
        super(nome, telefone, email);
        this.idAdministrador = idAdministrador;
        this.dataInicio = dataInicio;
        this.grau = grau;

    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        this.grau = grau;
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "idAdministrador=" + idAdministrador +
                ", dataInicio=" + dataInicio +
                ", grau='" + grau + '\'' +
                '}';
    }
}
