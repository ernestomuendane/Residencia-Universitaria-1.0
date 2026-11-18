package visao;

import modelo.Administrador;
import modelo.Gestao;
import modelo.Residencia;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PainelAdministradores extends JPanel implements Exportavel {

    private final ServicoResidencia servico;

    private final DefaultTableModel modeloAdm = Util.modelo("ID", "Nome", "Telefone", "Email", "Início de funções", "Grau", "Gere agora");
    private final JTable tabelaAdm = Util.tabela(modeloAdm);
    private final List<Administrador> linhasAdm = new ArrayList<>();

    private final DefaultTableModel modeloGestao = Util.modelo("Residência", "Início", "Fim", "Estado");
    private final JTable tabelaGestao = Util.tabela(modeloGestao);
    private final List<Gestao> linhasGestao = new ArrayList<>();

    private final JTextField nome = new JTextField(14);
    private final JTextField telefone = new JTextField(14);
    private final JTextField email = new JTextField(14);
    private final JTextField dataInicio = new JTextField(Util.agora(), 14);
    private final JTextField grau = new JTextField(14);

    private final JComboBox<Residencia> residencia = Util.combo(Residencia::getNome);
    private final JTextField inicioGestao = new JTextField(Util.agora(), 14);
    private final JTextField fimGestao = new JTextField(Util.agora(), 14);

    public PainelAdministradores(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel form = Util.formulario("Administrador");
        Util.campo(form, 0, "Nome:", nome);
        Util.campo(form, 1, "Telefone:", telefone);
        Util.campo(form, 2, "Email:", email);
        Util.campo(form, 3, "Início (dd/MM/aaaa HH:mm):", dataInicio);
        Util.campo(form, 4, "Grau:", grau);
        JButton registar = new JButton("Registar");
        JButton guardar = new JButton("Guardar alterações");
        Util.linhaBotoes(form, 5, registar, guardar);
        Util.enterAciona(registar, nome, telefone, email, dataInicio, grau);

        registar.addActionListener(e -> tarefas.executar(this, "Registar administrador", () -> {
            String n = Util.obrigatorio(nome.getText(), "O nome");
            String t = telefone.getText().trim();
            String m = email.getText().trim();
            LocalDateTime d = Util.dataHora(dataInicio.getText(), "A data de início");
            String g = grau.getText();
            return () -> servico.registarAdministrador(n, t, m, d, g);
        }, () -> {
            nome.setText("");
            telefone.setText("");
            email.setText("");
            grau.setText("");
        }));
        guardar.addActionListener(e -> tarefas.executar(this, "Alterar administrador", () -> {
            Administrador a = Util.exigir(Util.selecionado(tabelaAdm, linhasAdm), "Seleciona um administrador");
            String t = telefone.getText().trim();
            String m = email.getText().trim();
            String g = grau.getText();
            return () -> servico.alterarAdministrador(a, t, m, g);
        }));

        JPanel formGestao = Util.formulario("Gestão (do administrador selecionado)");
        Util.campo(formGestao, 0, "Residência:", residencia);
        Util.campo(formGestao, 1, "Início (dd/MM/aaaa HH:mm):", inicioGestao);
        Util.campo(formGestao, 2, "Fim (dd/MM/aaaa HH:mm):", fimGestao);
        JButton atribuir = new JButton("Atribuir gestão");
        JButton encerrar = new JButton("Encerrar gestão");
        Util.linhaBotoes(formGestao, 3, atribuir, encerrar);

        atribuir.addActionListener(e -> tarefas.executar(this, "Atribuir gestão", () -> {
            Administrador a = Util.exigir(Util.selecionado(tabelaAdm, linhasAdm), "Seleciona um administrador");
            Residencia r = Util.exigir((Residencia) residencia.getSelectedItem(), "Cria primeiro uma residência");
            LocalDateTime inicio = Util.dataHora(inicioGestao.getText(), "A data de início");
            return () -> servico.atribuirGestao(a, r, inicio);
        }));
        encerrar.addActionListener(e -> tarefas.executar(this, "Encerrar gestão", () -> {
            Gestao g = Util.exigir(Util.selecionado(tabelaGestao, linhasGestao), "Seleciona uma gestão na tabela inferior");
            LocalDateTime fim = Util.dataHora(fimGestao.getText(), "A data de fim");
            return () -> servico.encerrarGestao(g, fim);
        }));

        tabelaAdm.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Administrador a = Util.selecionado(tabelaAdm, linhasAdm);
            if (a != null) {
                nome.setText(a.getNome());
                telefone.setText(a.getTelefone());
                email.setText(a.getEmail());
                dataInicio.setText(Util.fmt(a.getDataInicio()));
                grau.setText(a.getGrau());
            }
            atualizarGestoes();
        });

        JPanel oeste = new JPanel();
        oeste.setLayout(new BoxLayout(oeste, BoxLayout.Y_AXIS));
        oeste.add(form);
        oeste.add(Box.createVerticalStrut(8));
        oeste.add(formGestao);
        JPanel oesteWrap = new JPanel(new BorderLayout());
        oesteWrap.add(oeste, BorderLayout.NORTH);
        add(oesteWrap, BorderLayout.WEST);

        JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        centro.add(Util.comPesquisa(tabelaAdm, "Administradores"));
        centro.add(Util.comTitulo(tabelaGestao, "Gestões do administrador selecionado"));
        add(centro, BorderLayout.CENTER);

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private void atualizar() {
        Util.preencher(residencia, servico.getResidencias());
        Util.carregar(tabelaAdm, modeloAdm, linhasAdm, servico.getAdministradores(), a -> new Object[]{
                a.getIdAdministrador(), a.getNome(), a.getTelefone(), a.getEmail(),
                Util.fmt(a.getDataInicio()), a.getGrau(),
                a.getResidenciasGeridas().isEmpty() ? "—" : a.getResidenciasGeridas().get(0).getNome()});
        atualizarGestoes();
    }

    private void atualizarGestoes() {
        Administrador a = Util.selecionado(tabelaAdm, linhasAdm);
        List<Gestao> origem = a == null ? Collections.emptyList() : a.getGestoes();
        Util.carregar(tabelaGestao, modeloGestao, linhasGestao, origem, g -> new Object[]{
                g.getResidencia().getNome(), Util.fmt(g.getDataInicio()), Util.fmt(g.getDataFim()), g.getEstado()});
    }

    @Override public JTable getTabelaPrincipal() { return tabelaAdm; }
    @Override public String getNomeExportacao() { return "administradores"; }
}
