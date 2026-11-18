package visao;

import modelo.*;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PainelPagamentos extends JPanel implements Exportavel {

    private final ServicoResidencia servico;
    private final DefaultTableModel modelo = Util.modelo("ID", "Data", "Estudante", "Contracto", "Método", "Meses", "Valor total");
    private final JTable tabela = Util.tabela(modelo);
    private final List<Pagamento> linhas = new ArrayList<>();

    private final JComboBox<Contracto> contracto = Util.combo(c ->
            "Contracto #" + c.getId() + " · " + c.getEstudante().getNome() + " · " + c.getEstado()
                    + " · " + c.getPrecoMensal().toPlainString() + "/mês");
    private final JComboBox<MetodoPagamento> metodo = new JComboBox<>(MetodoPagamento.values());
    private final JSpinner meses = new JSpinner(new SpinnerNumberModel(1, 1, 60, 1));
    private final JLabel valor = new JLabel("—");

    public PainelPagamentos(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel form = Util.formulario("Novo pagamento (ativa o contracto)");
        Util.campo(form, 0, "Contracto:", contracto);
        Util.campo(form, 1, "Método:", metodo);
        Util.campo(form, 2, "Nº de meses:", meses);
        Util.campo(form, 3, "Valor total:", valor);
        JButton registar = new JButton("Registar pagamento");
        Util.linhaBotoes(form, 4, registar);

        registar.addActionListener(e -> tarefas.executar(this, "Registar pagamento", () -> {
            Contracto c = Util.exigir((Contracto) contracto.getSelectedItem(), "Não há contractos em aberto");
            MetodoPagamento m = (MetodoPagamento) metodo.getSelectedItem();
            int n = (Integer) meses.getValue();
            return () -> servico.registarPagamento(c, m, n);
        }));

        contracto.addActionListener(e -> atualizarValor());
        meses.addChangeListener(e -> atualizarValor());

        JPanel oeste = new JPanel(new BorderLayout());
        oeste.add(form, BorderLayout.NORTH);
        add(oeste, BorderLayout.WEST);
        add(Util.comPesquisa(tabela, "Pagamentos"), BorderLayout.CENTER);

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private void atualizar() {
        List<Contracto> abertos = servico.getContractos().stream()
                .filter(c -> c.getEstado() != EstadoContracto.CANCELADO && !c.temPagamento())
                .collect(Collectors.toList());
        Util.preencher(contracto, abertos);
        atualizarValor();
        Util.carregar(tabela, modelo, linhas, servico.getPagamentos(), p -> new Object[]{
                p.getIdPagamento(), Util.fmt(p.getData()), p.getEstudante().getNome(),
                "#" + p.getContracto().getId(), p.getMetodo(), p.getTotalMeses(),
                p.getValorTotal().toPlainString()});
    }

    private void atualizarValor() {
        Contracto c = (Contracto) contracto.getSelectedItem();
        if (c == null) {
            valor.setText("—");
            return;
        }
        BigDecimal total = c.getPrecoMensal().multiply(BigDecimal.valueOf((Integer) meses.getValue()));
        valor.setText(total.toPlainString());
    }

    @Override public JTable getTabelaPrincipal() { return tabela; }
    @Override public String getNomeExportacao() { return "pagamentos"; }
}
