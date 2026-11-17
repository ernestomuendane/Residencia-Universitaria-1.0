package visao;

import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JanelaPrincipal extends JFrame {

    private static final int INTERVALO_ATUALIZACAO_S = 30;

    private final ServicoResidencia servico;
    private final Tarefas tarefas;
    private final BarraEstado barra = new BarraEstado();
    private final Ocupado ocupado = new Ocupado();
    private final MonitorBd monitor;
    private final JTabbedPane abas = new JTabbedPane();
    private long ultimaAtualizacaoPorFoco;

    public JanelaPrincipal(ServicoResidencia servico) {
        super("Sistema de Gestão de Residência Universitária");
        this.servico = servico;
        this.tarefas = new Tarefas(servico);
        this.monitor = new MonitorBd(servico, barra, tarefas);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // quem decide é o WindowListener
        setSize(1200, 780);
        setLocationRelativeTo(null);

        setGlassPane(ocupado);
        tarefas.aoOcupar(emCurso -> {
            barra.ocupado(emCurso);
            if (emCurso) ocupado.ativar(); else ocupado.desativar();
        });
        tarefas.aoMensagem(barra::mensagem);

        abas.addTab("Resumo", new PainelResumo(servico, tarefas));
        abas.addTab("Residências", new PainelResidencias(servico, tarefas));
        abas.addTab("Categorias", new PainelCategorias(servico, tarefas));
        abas.addTab("Estudantes", new PainelEstudantes(servico, tarefas));
        abas.addTab("Administradores", new PainelAdministradores(servico, tarefas));
        abas.addTab("Requisições", new PainelRequisicoes(servico, tarefas));
        abas.addTab("Contractos", new PainelContractos(servico, tarefas));
        abas.addTab("Pagamentos", new PainelPagamentos(servico, tarefas));

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.add(abas, BorderLayout.CENTER);
        conteudo.add(barra, BorderLayout.SOUTH);
        setContentPane(conteudo);

        setJMenuBar(criarMenu());
        instalarAtalhosDeSeparadores();
        instalarEventosDaJanela();

        monitor.atualizacaoAutomatica(true, INTERVALO_ATUALIZACAO_S);
    }

    private JMenuBar criarMenu() {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem exemplo = new JMenuItem("Carregar dados de exemplo");
        exemplo.addActionListener(e -> carregarExemplo());

        JMenuItem exportar = new JMenuItem("Exportar separador atual para CSV...");
        exportar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ctrl));
        exportar.addActionListener(e -> exportarSeparadorAtual());

        JMenuItem recarregar = new JMenuItem("Recarregar da base de dados");
        recarregar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        recarregar.addActionListener(e -> {
            if (!tarefas.ocupado()) {
                tarefas.executar(this, "Recarregar da base de dados", () -> servico::recarregar);
            }
        });

        JMenuItem sair = new JMenuItem("Sair");
        sair.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl));
        sair.addActionListener(e -> sair());

        JMenu ficheiro = new JMenu("Ficheiro");
        ficheiro.add(exemplo);
        ficheiro.add(exportar);
        ficheiro.addSeparator();
        ficheiro.add(recarregar);
        ficheiro.addSeparator();
        ficheiro.add(sair);

        JCheckBoxMenuItem automatica = new JCheckBoxMenuItem(
                "Atualização automática (" + INTERVALO_ATUALIZACAO_S + " s)", true);
        automatica.addItemListener(e -> {
            boolean ligada = e.getStateChange() == ItemEvent.SELECTED;
            monitor.atualizacaoAutomatica(ligada, INTERVALO_ATUALIZACAO_S);
            barra.mensagem(ligada ? "Atualização automática ligada" : "Atualização automática desligada");
        });
        JMenu ver = new JMenu("Ver");
        ver.add(automatica);

        JMenuItem atalhos = new JMenuItem("Atalhos de teclado");
        atalhos.addActionListener(e -> Util.detalhes(this, "Atalhos de teclado",
                "F5            recarregar da base de dados\n"
                        + "Ctrl+E        exportar o separador atual para CSV\n"
                        + "Ctrl+1 ... 8  mudar de separador\n"
                        + "Ctrl+Q        sair\n\n"
                        + "Nas tabelas:\n"
                        + "  clique no cabeçalho   ordenar\n"
                        + "  caixa Pesquisar       filtrar (Esc limpa)\n"
                        + "  duplo clique          ver detalhes\n"
                        + "  botão direito         copiar linha\n\n"
                        + "Nos formulários, Enter equivale ao botão principal."));
        JMenu ajuda = new JMenu("Ajuda");
        ajuda.add(atalhos);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(ficheiro);
        menuBar.add(ver);
        menuBar.add(ajuda);
        return menuBar;
    }

    private void instalarAtalhosDeSeparadores() {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
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

    private void sair() {
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

    private void carregarExemplo() {
        if (!servico.estaVazio()) {
            Util.info(this, "Os dados de exemplo só podem ser carregados com o sistema vazio.");
            return;
        }
        tarefas.executar(this, "Carregar dados de exemplo", () -> servico::carregarExemplo);
    }

    private void exportarSeparadorAtual() {
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
        for (int r = 0; r < tabela.getRowCount(); r++) {   // ordem e filtro que o utilizador está a ver
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