package visao;

import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JanelaPrincipal extends JFrame implements MenuPrincipal.Acoes {

    private static final int INTERVALO_ATUALIZACAO_S = 30;

    private final ServicoResidencia servico;
    private final Tarefas tarefas;
    private final BarraEstado barra = new BarraEstado();
    private final Ocupado ocupado = new Ocupado();
    private final MonitorBd monitor;
    private final JTabbedPane abas = new JTabbedPane();
    private final PainelContractos painelContractos;
    private long ultimaAtualizacaoPorFoco;

    public JanelaPrincipal(ServicoResidencia servico) {
        super("Sistema de Gestão de Residência Universitária");
        this.servico = servico;
        this.tarefas = new Tarefas(servico);
        this.monitor = new MonitorBd(servico, barra, tarefas);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);

        setGlassPane(ocupado);
        tarefas.aoOcupar(emCurso -> {
            barra.ocupado(emCurso);
            if (emCurso) ocupado.ativar(); else ocupado.desativar();
        });
        tarefas.aoMensagem(barra::mensagem);

        painelContractos = new PainelContractos(servico, tarefas);
        abas.addTab("Resumo", new PainelResumo(servico, tarefas));
        abas.addTab("Residências", new PainelResidencias(servico, tarefas));
        abas.addTab("Categorias", new PainelCategorias(servico, tarefas));
        abas.addTab("Estudantes", new PainelEstudantes(servico, tarefas));
        abas.addTab("Administradores", new PainelAdministradores(servico, tarefas));
        abas.addTab("Requisições", new PainelRequisicoes(servico, tarefas));
        abas.addTab("Contractos", painelContractos);
        abas.addTab("Pagamentos", new PainelPagamentos(servico, tarefas));

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.add(abas, BorderLayout.CENTER);
        conteudo.add(barra, BorderLayout.SOUTH);
        setContentPane(conteudo);

        setJMenuBar(new MenuPrincipal(this, INTERVALO_ATUALIZACAO_S));
        instalarAtalhosDeSeparadores();
        instalarEventosDaJanela();

        monitor.atualizacaoAutomatica(true, INTERVALO_ATUALIZACAO_S);
    }

    private void instalarAtalhosDeSeparadores() {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        InputMap teclas = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acoes = getRootPane().getActionMap();
        for (int i = 0; i < abas.getTabCount(); i++) {
            final int indice = i;
            String nome = "separador" + i;
            teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, ctrl), nome);
            acoes.put(nome, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abas.setSelectedIndex(indice);
                }
            });
        }
    }

    private void instalarEventosDaJanela() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sair();
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                long agora = System.currentTimeMillis();
                if (agora - ultimaAtualizacaoPorFoco > 5000) {
                    ultimaAtualizacaoPorFoco = agora;
                    tarefas.recarregarSeLivre();
                }
            }
        });

        abas.addChangeListener(e -> barra.mensagem("Separador: " + abas.getTitleAt(abas.getSelectedIndex())));
    }

    @Override
    public void carregarExemplo() {
        if (!servico.estaVazio()) {
            Util.info(this, "Os dados de exemplo só podem ser carregados com o sistema vazio.");
            return;
        }
        tarefas.executar(this, "Carregar dados de exemplo", () -> servico::carregarExemplo);
    }

    @Override
    public void recarregar() {
        if (!tarefas.ocupado()) {
            tarefas.executar(this, "Recarregar da base de dados", () -> servico::recarregar);
        }
    }

    @Override
    public void cancelarPendentesSemPagamento() {
        if (!tarefas.ocupado()) {
            painelContractos.cancelarPendentesSemPagamento(tarefas);
        }
    }

    @Override
    public void atualizacaoAutomatica(boolean ligada) {
        monitor.atualizacaoAutomatica(ligada, INTERVALO_ATUALIZACAO_S);
        barra.mensagem(ligada ? "Atualização automática ligada" : "Atualização automática desligada");
    }

    @Override
    public void sair() {
        if (tarefas.ocupado()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "Ainda há operações em curso. Sair mesmo assim?", "Sair", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) {
                return;
            }
        }
        monitor.parar();
        tarefas.encerrar();
        dispose();
    }

    @Override
    public void mostrarAtalhos() {
        Util.detalhes(this, "Atalhos de teclado",
                "F5            recarregar da base de dados\n"
                        + "Ctrl+E        exportar o separador atual para CSV\n"
                        + "Ctrl+1 ... 8  mudar de separador\n"
                        + "Ctrl+Q        sair\n\n"
                        + "Nas tabelas:\n"
                        + "  clique no cabeçalho   ordenar\n"
                        + "  caixa Pesquisar       filtrar (Esc limpa)\n"
                        + "  duplo clique          ver detalhes\n"
                        + "  botão direito         copiar linha\n\n"
                        + "Nos formulários, Enter equivale ao botão principal.");
    }

    @Override
    public void mostrarSobre() {
        Util.detalhes(this, "Sobre",
                "Sistema de Gestão de Residência Universitária\n"
                        + "Java Swing + MariaDB\n\n"
                        + "Fluxo: Requisição → (aprovada) → Contracto (reserva a cama)\n"
                        + "       → Pagamento (ativa o contracto, ocupa a cama)\n"
                        + "       → Cancelamento (liberta a cama).");
    }

    @Override
    public void exportarSeparadorAtual() {
        Component atual = abas.getSelectedComponent();
        if (!(atual instanceof Exportavel)) {
            Util.info(this, "Este separador não tem uma tabela para exportar.");
            return;
        }
        Exportavel exportavel = (Exportavel) atual;
        JTable tabela = exportavel.getTabelaPrincipal();

        JFileChooser escolha = new JFileChooser();
        escolha.setFileFilter(new FileNameExtensionFilter("Ficheiros CSV (*.csv)", "csv"));
        escolha.setSelectedFile(new File(exportavel.getNomeExportacao() + ".csv"));
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = escolha.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".csv")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".csv");
        }
        final File ficheiro = destino;

        List<String> cabecalho = new ArrayList<>();
        for (int c = 0; c < tabela.getColumnCount(); c++) {
            cabecalho.add(tabela.getColumnName(c));
        }
        List<List<String>> linhas = new ArrayList<>();
        for (int r = 0; r < tabela.getRowCount(); r++) {
            List<String> linha = new ArrayList<>();
            for (int c = 0; c < tabela.getColumnCount(); c++) {
                linha.add(String.valueOf(tabela.getValueAt(r, c)));
            }
            linhas.add(linha);
        }

        tarefas.executar(this, "Exportar para CSV",
                () -> () -> Exportador.csv(ficheiro, cabecalho, linhas),
                () -> Util.info(this, linhas.size() + " linha(s) exportadas para\n" + ficheiro.getAbsolutePath()));
    }
}
