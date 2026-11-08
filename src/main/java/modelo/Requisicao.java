package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Requisicao {
    private int idRequisicao;
    private LocalDateTime data = LocalDateTime.now();
    private String resultado;
    private Estudante estudante;
    private Residencia residencia;
    private Contracto contracto;

    public Requisicao(int idRequisicao, LocalDateTime data, String resultado){
        this.idRequisicao = idRequisicao;
        this.data = data;
        this.resultado = resultado;
    }

    public int getIdRequisicao() {
        return idRequisicao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public String getResultado(){
        return  resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Estudante getEstudante(){return estudante;}


}
