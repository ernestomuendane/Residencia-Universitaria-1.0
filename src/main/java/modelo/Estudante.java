package modelo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Estudante extends Utente{
     private final int idEstudante;
     private final Sexo  sexo;
     private String curso;
    private final List<Requisicao> requisicoes = new ArrayList<>();

    public Estudante( int idEstudante, String nome, String telefone, String email, Sexo sexo, String curso) {
        super(nome, telefone, email);
        this.idEstudante = idEstudante;
        this.sexo = Objects.requireNonNull(sexo, "O sexo é obrigatório");
        setCurso(curso);
    }

    public int getIdEstudante(){
        return idEstudante;
    }

    @Override
    public int getId() {
        return idEstudante;
    }

    public Sexo getSexo(){
        return sexo;
    }

    public String getCurso(){
        return curso;
    }

    public void setCurso(String curso) {
        if (curso == null || curso.isBlank()) {
            throw new IllegalArgumentException("O curso é obrigatório");
        }
        this.curso = curso;
    }

    public Requisicao fazerRequisicao(int idRequisicao, Residencia residencia) {
        return new Requisicao(idRequisicao, LocalDateTime.now(), this, residencia);
    }

    public List<Requisicao> getRequisicoes() {
        return Collections.unmodifiableList(requisicoes);
    }

    public List<Contracto> getContractos() {
        return requisicoes.stream()
                .map(Requisicao::getContracto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<Contracto> getContractoAtivo() {
        return getContractos().stream()
                .filter(c -> c.getEstado() == EstadoContracto.ATIVO)
                .findFirst();
    }

    void registarRequisicao(Requisicao requisicao) {
        if (!requisicoes.contains(requisicao)) {
            requisicoes.add(requisicao);
        }
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
