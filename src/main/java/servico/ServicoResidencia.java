package servico;

import dao.*;
import modelo.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ServicoResidencia {

    private final CategoriaDao categoriaDao = new CategoriaDao();
    private final ResidenciaDao residenciaDao = new ResidenciaDao();
    private final QuartoDao quartoDao = new QuartoDao();
    private final CamaDao camaDao = new CamaDao();
    private final EstudanteDao estudanteDao = new EstudanteDao();
    private final AdministradorDao administradorDao = new AdministradorDao();
    private final GestaoDao gestaoDao = new GestaoDao();
    private final RequisicaoDao requisicaoDao = new RequisicaoDao();
    private final ContractoDao contractoDao = new ContractoDao();
    private final PagamentoDao pagamentoDao = new PagamentoDao();

    private final List<Categoria> categorias = new CopyOnWriteArrayList<>();
    private final List<Residencia> residencias = new CopyOnWriteArrayList<>();
    private final List<Estudante> estudantes = new CopyOnWriteArrayList<>();
    private final List<Administrador> administradores = new CopyOnWriteArrayList<>();
    private final List<Requisicao> requisicoes = new CopyOnWriteArrayList<>();

    private final List<Runnable> ouvintes = new ArrayList<>();

    public ServicoResidencia() {
    }

    public void aoAlterar(Runnable ouvinte) {
        ouvintes.add(ouvinte);
    }

    public void notificarAlteracao() {
        for (Runnable o : ouvintes) {
            o.run();
        }
    }

    public interface Progresso {
        void passo(int numero, int total, String mensagem);
    }

    public static final class Instantaneo {
        private final List<Categoria> categorias;
        private final List<Residencia> residencias;
        private final List<Estudante> estudantes;
        private final List<Administrador> administradores;
        private final List<Requisicao> requisicoes;

        private Instantaneo(Collection<Categoria> c, Collection<Residencia> r, Collection<Estudante> e,
                            Collection<Administrador> a, Collection<Requisicao> q) {
            this.categorias = new ArrayList<>(c);
            this.residencias = new ArrayList<>(r);
            this.estudantes = new ArrayList<>(e);
            this.administradores = new ArrayList<>(a);
            this.requisicoes = new ArrayList<>(q);
        }
    }

    public boolean baseDeDadosDisponivel() {
        return Conexao.disponivel();
    }

    private static void passo(Progresso p, int n, int total, String mensagem) {
        if (p != null) {
            p.passo(n, total, mensagem);
        }
    }

    public Instantaneo ler(Progresso p) {
        final int total = 10;
        passo(p, 0, total, "A ligar à base de dados...");
        try (Connection c = Conexao.obter()) {
            passo(p, 1, total, "A ler categorias...");
            Map<Integer, Categoria> cats = categoriaDao.carregar(c);
            passo(p, 2, total, "A ler residências...");
            Map<Integer, Residencia> ress = residenciaDao.carregar(c);
            passo(p, 3, total, "A ler quartos...");
            Map<Integer, Quarto> quartos = quartoDao.carregar(c, ress, cats);
            passo(p, 4, total, "A ler camas...");
            Map<Integer, Cama> camas = camaDao.carregar(c, quartos);
            passo(p, 5, total, "A ler estudantes...");
            Map<Integer, Estudante> ests = estudanteDao.carregar(c);
            passo(p, 6, total, "A ler administradores...");
            Map<Integer, Administrador> adms = administradorDao.carregar(c);
            passo(p, 7, total, "A ler gestões...");
            gestaoDao.carregar(c, adms, ress);
            passo(p, 8, total, "A ler requisições...");
            Map<Integer, Requisicao> reqs = requisicaoDao.carregar(c, ests, ress, adms);
            passo(p, 9, total, "A ler contractos e pagamentos...");
            Map<Integer, Contracto> cons = contractoDao.carregar(c, reqs, camas, adms);
            pagamentoDao.carregar(c, cons);
            passo(p, 10, total, "Concluído");
            return new Instantaneo(cats.values(), ress.values(), ests.values(), adms.values(), reqs.values());
        } catch (SQLException e) {
            throw new DaoException("Erro ao ler a base de dados: " + e.getMessage(), e);
        }
    }

    public void aplicar(Instantaneo i) {
        categorias.clear();
        categorias.addAll(i.categorias);
        residencias.clear();
        residencias.addAll(i.residencias);
        estudantes.clear();
        estudantes.addAll(i.estudantes);
        administradores.clear();
        administradores.addAll(i.administradores);
        requisicoes.clear();
        requisicoes.addAll(i.requisicoes);
    }

    public void recarregar(Progresso progresso) {
        aplicar(ler(progresso));
    }

    public void recarregar() {
        recarregar(null);
    }

    private <T> T persistir(Transacao.Operacao<T> operacao) {
        try {
            return Transacao.executar(operacao);
        } catch (RuntimeException e) {
            try {
                recarregar();
            } catch (RuntimeException ignorada) {
            }
            throw e;
        }
    }

    public List<Categoria> getCategorias() {
        return Collections.unmodifiableList(categorias);
    }

    public List<Residencia> getResidencias() {
        return Collections.unmodifiableList(residencias);
    }

    public List<Estudante> getEstudantes() {
        return Collections.unmodifiableList(estudantes);
    }

    public List<Administrador> getAdministradores() {
        return Collections.unmodifiableList(administradores);
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

    public List<Pagamento> getPagamentos() {
        return getContractos().stream()
                .map(Contracto::getPagamento)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean estaVazio() {
        return categorias.isEmpty() && residencias.isEmpty() && estudantes.isEmpty()
                && administradores.isEmpty() && requisicoes.isEmpty();
    }

    public Categoria criarCategoria(String nome, int capacidade, BigDecimal preco) {
        return persistir(c -> {
            Categoria cat = categoriaDao.inserir(c, nome, capacidade, preco);
            categorias.add(cat);
            return cat;
        });
    }

    public void alterarCategoria(Categoria categoria, String nome, int capacidade, BigDecimal preco) {
        persistir(c -> {
            categoria.setNome(nome);
            categoria.setCapacidade(capacidade);
            categoria.setPreco(preco);
            categoriaDao.atualizar(c, categoria);
            return null;
        });
    }

    public Residencia criarResidencia(String nome, String tipo) {
        return persistir(c -> {
            Residencia r = residenciaDao.inserir(c, nome, tipo);
            residencias.add(r);
            return r;
        });
    }

    public void alterarResidencia(Residencia residencia, String nome, String tipo) {
        persistir(c -> {
            residencia.setNome(nome);
            residencia.setTipo(tipo);
            residenciaDao.atualizar(c, residencia);
            return null;
        });
    }

    public Quarto criarQuarto(Residencia residencia, int numero, Categoria categoria) {
        return persistir(c -> {
            boolean repetido = residencia.getQuartos().stream().anyMatch(q -> q.getNumero() == numero);
            if (repetido) {
                throw new IllegalStateException("Já existe o quarto " + numero
                        + " na residência " + residencia.getNome());
            }
            return quartoDao.inserir(c, residencia, numero, categoria);
        });
    }

    public Cama criarCama(Quarto quarto) {
        return persistir(c -> {
            if (quarto.estaCheio()) {
                throw new IllegalStateException("Capacidade do quarto " + quarto.getNumero() + " excedida ("
                        + quarto.getCategoria().getCapacidade() + ")");
            }
            return camaDao.inserir(c, quarto);
        });
    }

    public void alterarEstadoCama(Cama cama, EstadoCama estado) {
        persistir(c -> {
            if (estado == EstadoCama.MANUTENCAO) {
                cama.colocarEmManutencao();
            } else if (estado == EstadoCama.DISPONIVEL) {
                cama.sairDeManutencao();
            } else {
                throw new IllegalStateException("O estado " + estado
                        + " é definido pelos contractos; só se escolhe à mão Disponível ou Em manutenção");
            }
            camaDao.atualizar(c, cama);
            return null;
        });
    }

    public Estudante registarEstudante(String nome, String telefone, String email, Sexo sexo, String curso) {
        return persistir(c -> {
            Estudante e = estudanteDao.inserir(c, nome, telefone, email, sexo, curso);
            estudantes.add(e);
            return e;
        });
    }

    public void alterarEstudante(Estudante estudante, String telefone, String email, String curso) {
        persistir(c -> {
            estudante.setTelefone(telefone);
            estudante.setEmail(email);
            estudante.setCurso(curso);
            estudanteDao.atualizar(c, estudante);
            return null;
        });
    }

    public Administrador registarAdministrador(String nome, String telefone, String email,
                                               LocalDateTime dataInicio, String grau) {
        return persistir(c -> {
            Administrador a = administradorDao.inserir(c, nome, telefone, email, dataInicio, grau);
            administradores.add(a);
            return a;
        });
    }

    public void alterarAdministrador(Administrador administrador, String telefone, String email, String grau) {
        persistir(c -> {
            administrador.setTelefone(telefone);
            administrador.setEmail(email);
            administrador.setGrau(grau);
            administradorDao.atualizar(c, administrador);
            return null;
        });
    }

    public Gestao atribuirGestao(Administrador admin, Residencia residencia, LocalDateTime inicio) {
        return persistir(c -> {
            admin.validarGestaoPossivel();
            boolean repetida = admin.getGestoes().stream()
                    .anyMatch(g -> g.getResidencia().equals(residencia) && g.getDataInicio().equals(inicio));
            if (repetida) {
                throw new IllegalStateException("Já existe uma gestão deste administrador nesta residência com esse início.");
            }
            residencia.validarGestaoPossivel();
            return gestaoDao.inserir(c, admin, residencia, inicio);
        });
    }

    public void encerrarGestao(Gestao gestao, LocalDateTime fim) {
        persistir(c -> {
            gestao.encerrar(fim);
            gestaoDao.atualizar(c, gestao);
            return null;
        });
    }

    public Requisicao criarRequisicao(Estudante estudante, Residencia residencia) {
        return persistir(c -> {
            Requisicao r = requisicaoDao.inserir(c, estudante, residencia);
            requisicoes.add(r);
            return r;
        });
    }

    public void aprovarRequisicao(Requisicao requisicao) {
        persistir(c -> {
            responsavel(requisicao.getResidencia()).aprovarRequisicao(requisicao);
            requisicaoDao.atualizar(c, requisicao);
            return null;
        });
    }

    public void rejeitarRequisicao(Requisicao requisicao) {
        persistir(c -> {
            responsavel(requisicao.getResidencia()).rejeitarRequisicao(requisicao);
            requisicaoDao.atualizar(c, requisicao);
            return null;
        });
    }

    private static Administrador responsavel(Residencia residencia) {
        return residencia.getAdministradorAtivo().orElseThrow(() -> new IllegalStateException(
                "A residência " + residencia.getNome() + " não tem administrador responsável neste momento"));
    }

    public Contracto criarContracto(Requisicao requisicao, Cama cama, LocalDateTime inicial, LocalDateTime fim) {
        return persistir(c -> {
            if (!requisicao.estaAprovada()) {
                throw new IllegalStateException("A requisição não está aprovada");
            }
            if (requisicao.getContracto() != null) {
                throw new IllegalStateException("A requisição já tem contracto");
            }
            Administrador admin = responsavel(requisicao.getResidencia());
            Contracto contracto = contractoDao.inserir(c, requisicao, cama, admin, inicial, fim);
            camaDao.atualizar(c, cama);
            return contracto;
        });
    }

    public void cancelarContracto(Contracto contracto) {
        persistir(c -> {
            contracto.cancelar(responsavel(contracto.getResidencia()));
            contractoDao.atualizar(c, contracto);
            camaDao.atualizar(c, contracto.getCama());
            return null;
        });
    }

    public int cancelarPendentesSemPagamento() {
        return persistir(c -> {
            int cancelados = 0;
            for (Contracto contracto : getContractos()) {
                if (contracto.deveSerCancelado()) {
                    contracto.cancelar(responsavel(contracto.getResidencia()));
                    contractoDao.atualizar(c, contracto);
                    camaDao.atualizar(c, contracto.getCama());
                    cancelados++;
                }
            }
            return cancelados;
        });
    }

    public Pagamento registarPagamento(Contracto contracto, MetodoPagamento metodo, int meses) {
        return persistir(c -> {
            if (contracto.temPagamento()) {
                throw new IllegalStateException("O contracto já tem pagamento");
            }
            Pagamento pagamento = pagamentoDao.inserir(c, contracto, metodo, meses);
            contractoDao.atualizar(c, contracto);
            camaDao.atualizar(c, contracto.getCama());
            return pagamento;
        });
    }

    public void carregarExemplo() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        Categoria normal = criarCategoria("Normal", 2, new BigDecimal("2500"));
        Categoria suite = criarCategoria("Suite", 1, new BigDecimal("4500"));

        Residencia central = criarResidencia("Residência Central", "Mista");
        Residencia norte = criarResidencia("Residência Norte", "Feminina");

        for (int n = 101; n <= 103; n++) {
            Quarto q = criarQuarto(central, n, normal);
            criarCama(q);
            criarCama(q);
        }
        criarCama(criarQuarto(central, 201, suite));
        for (int n = 101; n <= 102; n++) {
            Quarto q = criarQuarto(norte, n, normal);
            criarCama(q);
            criarCama(q);
        }

        Administrador a1 = registarAdministrador("Ana Macamo", "841234567", "ana.macamo@uni.mz", agora.minusYears(2), "Sénior");
        Administrador a2 = registarAdministrador("Paulo Nhaca", "829876543", "paulo.nhaca@uni.mz", agora.minusMonths(8), "Júnior");
        registarAdministrador("Rita Sitoe", "834445566", "rita.sitoe@uni.mz", agora.minusMonths(1), "Júnior");
        atribuirGestao(a1, central, agora.minusMonths(6));
        atribuirGestao(a2, norte, agora.minusMonths(3));

        registarEstudante("Carlos Mondlane", "845550101", "carlos@estudante.uni.mz", Sexo.MASCULINO, "Engenharia Informática");
        registarEstudante("Maria Chissano", "825550202", "maria@estudante.uni.mz", Sexo.FEMININO, "Medicina");
    }
}

