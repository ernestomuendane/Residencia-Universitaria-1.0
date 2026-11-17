package visao;

import modelo.*;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PainelResidencias extends JPanel implements Exportavel {

    private final ServicoResidencia servico;

    private final DefaultTableModel modeloRes = Util.modelo("ID", "Nome", "Tipo", "Quartos", "Camas", "Camas livres", "Administrador ativo");
    private final JTable tabelaRes = Util.tabela(modeloRes);
    private final List<Residencia> linhasRes = new ArrayList<>();

    private final DefaultTableModel modeloQuartos = Util.modelo("ID", "Nº", "Categoria", "Camas / Capacidade");
    private final JTable tabelaQuartos = Util.tabela(modeloQuartos);
    private final List<Quarto> linhasQuartos = new ArrayList<>();

    private final DefaultTableModel modeloCamas = Util.modelo("ID", "Estado");
    private final JTable tabelaCamas = Util.tabela(modeloCamas);
    private final List<Cama> linhasCamas = new ArrayList<>();

    private final JTextField nome = new JTextField(14);
    private final JTextField tipo = new JTextField(14);
    private final JTextField numeroQuarto = new JTextField(14);
    private final JComboBox<Categoria> categoria = Util.combo(c -> c.getNome() + " (" + c.getCapacidade() + " camas)");
    private final JComboBox<EstadoCama> estadoCama = new JComboBox<>(EstadoCama.values());

    public PainelResidencias(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel formRes = Util.formulario("Residência");
        Util.campo(formRes, 0, "Nome:", nome);
        Util.campo(formRes, 1, "Tipo (ex.: Mista):", tipo);
        JButton addRes = new JButton("Adicionar");
        JButton guardarRes = new JButton("Guardar alterações");
        Util.linhaBotoes(formRes, 2, addRes, guardarRes);
        Util.enterAciona(addRes, nome, tipo);

        addRes.addActionListener(e -> tarefas.executar(this, "Criar residência", () -> {
            String n = Util.obrigatorio(nome.getText(), "O nome");
            String t = Util.obrigatorio(tipo.getText(), "O tipo");
            return () -> servico.criarResidencia(n, t);
        }, () -> {
            nome.setText("");
            tipo.setText("");
        }));
        guardarRes.addActionListener(e -> tarefas.executar(this, "Alterar residência", () -> {
            Residencia r = Util.exigir(Util.selecionado(tabelaRes, linhasRes), "Seleciona uma residência");
            String n = Util.obrigatorio(nome.getText(), "O nome");
            String t = Util.obrigatorio(tipo.getText(), "O tipo");
            return () -> servico.alterarResidencia(r, n, t);
        }));

        JPanel formQuarto = Util.formulario("Quarto (na residência selecionada)");
        Util.campo(formQuarto, 0, "Número:", numeroQuarto);
        Util.campo(formQuarto, 1, "Categoria:", categoria);
        JButton addQuarto = new JButton("Adicionar quarto");
        Util.linhaBotoes(formQuarto, 2, addQuarto);
        Util.enterAciona(addQuarto, numeroQuarto);

        addQuarto.addActionListener(e -> tarefas.executar(this, "Criar quarto", () -> {
            Residencia r = Util.exigir(Util.selecionado(tabelaRes, linhasRes), "Seleciona uma residência");
            Categoria c = Util.exigir((Categoria) categoria.getSelectedItem(), "Cria primeiro uma categoria");
            int numero = Util.inteiro(numeroQuarto.getText(), "O número do quarto");
            return () -> servico.criarQuarto(r, numero, c);
        }, () -> numeroQuarto.setText("")));

        JPanel formCama = Util.formulario("Cama (no quarto selecionado)");
        Util.campo(formCama, 0, "Estado:", estadoCama);
        JButton addCama = new JButton("Adicionar cama");
        JButton alterarEstado = new JButton("Alterar estado");
        Util.linhaBotoes(formCama, 1, addCama, alterarEstado);

        addCama.addActionListener(e -> tarefas.executar(this, "Criar cama", () -> {
            Quarto q = Util.exigir(Util.selecionado(tabelaQuartos, linhasQuartos), "Seleciona um quarto");
            return () -> servico.criarCama(q);
        }));
        alterarEstado.addActionListener(e -> tarefas.executar(this, "Alterar estado da cama", () -> {
            Cama c = Util.exigir(Util.selecionado(tabelaCamas, linhasCamas), "Seleciona uma cama");
            EstadoCama novo = (EstadoCama) estadoCama.getSelectedItem();
            return () -> servico.alterarEstadoCama(c, novo);
        }));

        JPanel oeste = new JPanel();
        oeste.setLayout(new BoxLayout(oeste, BoxLayout.Y_AXIS));
        oeste.add(formRes);
        oeste.add(Box.createVerticalStrut(8));
        oeste.add(formQuarto);
        oeste.add(Box.createVerticalStrut(8));
        oeste.add(formCama);
        JPanel oesteWrap = new JPanel(new BorderLayout());
        oesteWrap.add(oeste, BorderLayout.NORTH);
        add(oesteWrap, BorderLayout.WEST);

        JPanel centro = new JPanel(new GridLayout(3, 1, 8, 8));
        centro.add(Util.comPesquisa(tabelaRes, "Residências"));
        centro.add(Util.comTitulo(tabelaQuartos, "Quartos da residência selecionada"));
        centro.add(Util.comTitulo(tabelaCamas, "Camas do quarto selecionado"));
        add(centro, BorderLayout.CENTER);

        tabelaRes.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Residencia r = Util.selecionado(tabelaRes, linhasRes);
            if (r != null) {
                nome.setText(r.getNome());
                tipo.setText(r.getTipo());
            }
            atualizarQuartos();
        });
        tabelaQuartos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) atualizarCamas();
        });
        tabelaCamas.getSelectionModel().addListSelectionListener(e -> {
            Cama c = Util.selecionado(tabelaCamas, linhasCamas);
            if (!e.getValueIsAdjusting() && c != null) estadoCama.setSelectedItem(c.getEstado());
        });

        Util.aoDuploClique(tabelaCamas, () -> {
            Cama c = Util.selecionado(tabelaCamas, linhasCamas);
            if (c == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Cama ").append(c.getIdCama()).append(" · Quarto ").append(c.getQuarto().getNumero())
                    .append(" · ").append(c.getQuarto().getResidencia().getNome()).append('\n');
            sb.append("Estado: ").append(c.getEstado()).append("\n\nHistórico de contractos:\n");
            if (c.getContractos().isEmpty()) sb.append("  (nenhum)\n");
            for (Contracto k : c.getContractos()) {
                sb.append("  #").append(k.getId()).append(" · ").append(k.getEstudante().getNome())
                        .append(" · ").append(Util.fmt(k.getDataInicial())).append(" a ")
                        .append(Util.fmt(k.getDataFinal())).append(" · ").append(k.getEstado()).append('\n');
            }
            Util.detalhes(this, "Cama " + c.getIdCama(), sb.toString());
        });

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private void atualizar() {
        Util.preencher(categoria, servico.getCategorias());
        Util.carregar(tabelaRes, modeloRes, linhasRes, servico.getResidencias(), r -> new Object[]{
                r.getIdResidencia(), r.getNome(), r.getTipo(), r.getQuartos().size(),
                r.getCamas().size(), r.getCamasLivres().size(),
                r.getAdministradores().isEmpty() ? "—" : r.getAdministradores().get(0).getNome()});
        garantirSelecao(tabelaRes);
        atualizarQuartos();
    }

    private void atualizarQuartos() {
        Residencia r = Util.selecionado(tabelaRes, linhasRes);
        List<Quarto> origem = r == null ? Collections.emptyList() : r.getQuartos();
        Util.carregar(tabelaQuartos, modeloQuartos, linhasQuartos, origem, q -> new Object[]{
                q.getIdQuarto(), q.getNumero(), q.getCategoria().getNome(),
                q.getCamas().size() + " / " + q.getCategoria().getCapacidade()});
        garantirSelecao(tabelaQuartos);
        atualizarCamas();
    }

    private void atualizarCamas() {
        Quarto q = Util.selecionado(tabelaQuartos, linhasQuartos);
        List<Cama> origem = q == null ? Collections.emptyList() : q.getCamas();
        Util.carregar(tabelaCamas, modeloCamas, linhasCamas, origem,
                c -> new Object[]{c.getIdCama(), c.getEstado()});
    }

    private void garantirSelecao(JTable t) {
        if (t.getSelectedRow() < 0 && t.getRowCount() > 0) {
            t.setRowSelectionInterval(0, 0);
        }
    }

    @Override public JTable getTabelaPrincipal() { return tabelaRes; }
    @Override public String getNomeExportacao() { return "residencias"; }
}