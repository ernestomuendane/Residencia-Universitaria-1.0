package modelo;

public class Estudante extends Utente{
     private int idEstudante;
     private String sexo;
     private String curso;

    public Estudante( int idEstudante, String nome, String telefone, String email, String sexo, String curso) {
        super(nome, telefone, email);
        this.idEstudante = idEstudante;
        this.sexo = sexo;
        this.curso = curso;

    }

    public int getIdEstudante(){
        return idEstudante;
    }

    public String getSexo(){
        return sexo;
    }

    public String getCurso(){
        return curso;
    }

    public void setCurso(String curso){
        this.curso = curso;
    }

    @Override
    public String toString() {
        return "Estudante{" +
                "idEstudante=" + idEstudante +
                ", sexo='" + sexo + '\'' +
                ", curso='" + curso + '\'' +
                '}';
    }
}
