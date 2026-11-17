package modelo;


import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Administrador extends Utente {
    private final int idAdministrador;
    private final LocalDateTime dataInicio;
    private String grau;
    private final List<Gestao> gestoes = new ArrayList<>();
    private final List<Requisicao> requisicoes = new ArrayList<>();

    public Administrador(int idAdministrador, String nome, String telefone, String email, LocalDateTime dataInicio, String grau, List<Gestao> gestoes) {
        super(nome, telefone, email);
        this.idAdministrador = idAdministrador;
        this.dataInicio = Objects.requireNonNull(dataInicio, "A data de início é obrigatória");
        setGrau(grau);
    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    @Override
    public int getId() {
        return idAdministrador;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        if (grau == null || grau.isBlank()) {
            throw new IllegalArgumentException("O grau é obrigatório");
        }
        this.grau = grau;
    }
    public Gestao gerir(Residencia residencia, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return new Gestao(this, residencia, dataInicio, dataFim);
    }

    public List<Gestao> getGestoes() {
        return Collections.unmodifiableList(gestoes);
    }

    public List<Residencia> getResidenciasGeridas() {
        return gestoes.stream()
                .filter(Gestao::estaAtiva)
                .map(Gestao::getResidencia)
                .collect(Collectors.toList());
    }

    public boolean gere(Residencia residencia) {
        return gestoes.stream()
                .anyMatch(g -> g.estaAtiva() && g.getResidencia().equals(residencia));
    }

    public void aprovarRequisicao(Requisicao requisicao) {
        requisicao.decidir(this, ResultadoRequisicao.APROVADA);
    }

    public void rejeitarRequisicao(Requisicao requisicao) {
        requisicao.decidir(this, ResultadoRequisicao.REJEITADA);
    }

    public List<Requisicao> getRequisicoes() {
        return Collections.unmodifiableList(requisicoes);
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
    public String toString() {
        return "Administrador{" +
                "idAdministrador=" + idAdministrador +
                ", dataInicio=" + dataInicio +
                ", grau='" + grau + '\'' +
                '}';
    }
}

