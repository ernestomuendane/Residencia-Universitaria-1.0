package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Residencia {
    private final int idResidencia;
    private  String nome;
    private String tipo;
    private final List<Quarto> quartos = new ArrayList<>();      // composição
    private final List<Gestao> gestoes = new ArrayList<>();
    private final List<Requisicao> requisicoes = new ArrayList<>();


    public Residencia(int idResidencia, String nome, String tipo) {
        this.idResidencia = idResidencia;
        setNome(nome);
        setTipo(tipo);
    }

    public int getIdResidencia(){
        return idResidencia;
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }
        this.nome = nome;
    }

    public String getTipo(){
        return tipo;
    }

    public void setTipo( String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("O tipo é obrigatório");
        }
        this.tipo = tipo;
    }

    public Quarto adicionarQuarto(int idQuarto, int numero, Categoria categoria) {
        return new Quarto(idQuarto, numero, categoria, this);
    }

    public List<Quarto> getQuartos() {
        return Collections.unmodifiableList(quartos);
    }

    public List<Cama> getCamas() {
        return quartos.stream()
                .flatMap(q -> q.getCamas().stream())
                .collect(Collectors.toList());
    }

    public List<Cama> getCamasLivres() {
        return getCamas().stream()
                .filter(Cama::estaLivre)
                .collect(Collectors.toList());
    }


    public List<Gestao> getGestoes() {
        return Collections.unmodifiableList(gestoes);
    }

    public List<Administrador> getAdministradores() {
        return gestoes.stream()
                .filter(Gestao::estaAtiva)
                .map(Gestao::getAdministrador)
                .collect(Collectors.toList());
    }

    public List<Requisicao> getRequisicoes() {
        return Collections.unmodifiableList(requisicoes);
    }

    public void mostrarInfo() {
        System.out.println("Residência " + idResidencia + " - " + nome + " (" + tipo + ")");
        System.out.println("Quartos: " + quartos.size()
                + " | Camas: " + getCamas().size()
                + " | Camas livres: " + getCamasLivres().size());
    }

    void registarQuarto(Quarto quarto) {
        boolean numeroRepetido = quartos.stream()
                .anyMatch(q -> q.getNumero() == quarto.getNumero() && !q.equals(quarto));
        if (numeroRepetido) {
            throw new IllegalStateException("Já existe o quarto " + quarto.getNumero()
                    + " na residência " + nome);
        }
        if (!quartos.contains(quarto)) {
            quartos.add(quarto);
        }
    }

    void registarGestao(Gestao gestao) {
        if (!gestoes.contains(gestao)) {
            gestoes.add(gestao);
        }
    }

    void registarRequisicao(Requisicao requisicao) {
        if (!requisicoes.contains(requisicao)) {
            requisicoes.add(requisicao);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Residencia)) return false;
        return idResidencia == ((Residencia) o).idResidencia;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idResidencia);
    }

    @Override
    public String toString() {
        return "Residencia{idResidencia=" + idResidencia +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' + '}';
    }
}
