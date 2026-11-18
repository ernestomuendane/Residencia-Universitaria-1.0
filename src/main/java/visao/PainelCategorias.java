package visao;

import modelo.Categoria;
import modelo.Quarto;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PainelCategorias extends JPanel implements Exportavel {

    private final ServicoResidencia servico;
    private final DefaultTableModel modelo = Util.modelo("ID", "Nome", "Capacidade (camas)", "Preço mensal", "Nº quartos");
    private final JTable tabela = Util.tabela(modelo);
    private final List<Categoria> linhas = new ArrayList<>();

    private final JTextField nome = new JTextField(14);
    private final JTextField capacidade = new JTextField(14);
    private final JTextField preco = new JTextField(14);

    public PainelCategorias(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel form = Util.formulario("Categoria de quarto");
        Util.campo(form, 0, "Nome:", nome);
        Util.campo(form, 1, "Capacidade:", capacidade);
        Util.campo(form, 2, "Preço mensal (por cama):", preco);
        JButton adicionar = new JButton("Adicionar");
        JButton guardar = new JButton("Guardar alterações");
        Util.linhaBotoes(form, 3, adicionar, guardar);
        Util.enterAciona(adicionar, nome, capacidade, preco);

        adicionar.addActionListener(e -> tarefas.executar(this, "Criar categoria", () -> {
            String n = Util.obrigatorio(nome.getText(), "O nome");
            int cap = Util.inteiro(capacidade.getText(), "A capacidade");
            BigDecimal p = Util.decimal(preco.getText(), "O preço");
            return () -> servico.criarCategoria(n, cap, p);
        }, this::limpar));

        guardar.addActionListener(e -> tarefas.executar(this, "Alterar categoria", () -> {
            Categoria c = Util.exigir(Util.selecionado(tabela, linhas), "Seleciona uma categoria na tabela");
            String n = Util.obrigatorio(nome.getText(), "O nome");
            int cap = Util.inteiro(capacidade.getText(), "A capacidade");
            BigDecimal p = Util.decimal(preco.getText(), "O preço");
            return () -> servico.alterarCategoria(c, n, cap, p);
        }));

        tabela.getSelectionModel().addListSelectionListener(e -> {
            Categoria c = Util.selecionado(tabela, linhas);
            if (!e.getValueIsAdjusting() && c != null) {
                nome.setText(c.getNome());
                capacidade.setText(String.valueOf(c.getCapacidade()));
                preco.setText(c.getPreco().toPlainString());
            }
        });

        Util.aoDuploClique(tabela, () -> {
            Categoria c = Util.selecionado(tabela, linhas);
            if (c == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Categoria ").append(c.getIdCategoria()).append(": ").append(c.getNome()).append('\n');
            sb.append("Capacidade: ").append(c.getCapacidade()).append(" cama(s) por quarto\n");
            sb.append("Preço mensal por cama: ").append(c.getPreco().toPlainString()).append("\n\n");
            sb.append("Quartos desta categoria (").append(c.getQuartos().size()).append("):\n");
            for (Quarto q : c.getQuartos()) {
                sb.append("  - Quarto ").append(q.getNumero()).append(" · ").append(q.getResidencia().getNome()).append('\n');
            }
            Util.detalhes(this, "Categoria " + c.getNome(), sb.toString());
        });

        JPanel oeste = new JPanel(new BorderLayout());
        oeste.add(form, BorderLayout.NORTH);
        add(oeste, BorderLayout.WEST);
        add(Util.comPesquisa(tabela, "Categorias (duplo clique para detalhes)"), BorderLayout.CENTER);

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private void limpar() {
        nome.setText("");
        capacidade.setText("");
        preco.setText("");
    }

    private void atualizar() {
        Util.carregar(tabela, modelo, linhas, servico.getCategorias(),
                c -> new Object[]{c.getIdCategoria(), c.getNome(), c.getCapacidade(),
                        c.getPreco().toPlainString(), c.getQuartos().size()});
    }

    @Override public JTable getTabelaPrincipal() { return tabela; }
    @Override public String getNomeExportacao() { return "categorias"; }
}
