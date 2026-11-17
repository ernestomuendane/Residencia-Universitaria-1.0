package visao;

import modelo.*;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PainelContractos extends JPanel implements Exportavel {

    private final ServicoResidencia servico;
    private final DefaultTableModel modelo = Util.modelo("ID", "Estudante", "Residência", "Cama", "Assinatura", "Início", "Fim", "Estado", "Total pago");
    private final JTable tabela = Util.tabela(modelo);
    private final List<Contracto> linhas = new ArrayList<>();

    private final JComboBox<Requisicao> requisicao = Util.combo(r ->
            "Requisição #" + r.getIdRequisicao() + " · " + r.getEstudante().getNome() + " · " + r.getResidencia().getNome());
    private final JComboBox<Cama> cama = Util.combo(c ->
            "Cama " + c.getIdCama() + " · Quarto " + c.getQuarto().getNumero());
    private final JTextField inicio = new JTextField(Util.hoje(), 14);
    private final JTextField fim = new JTextField(Util.fmt(LocalDate.now().plusMonths(6)), 14);

    public PainelContractos(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel formNovo = Util.formulario("Novo contracto (de requisição aprovada)");
        Util.campo(formNovo, 0, "Requisição:", requisicao);
        Util.campo(formNovo, 1, "Cama livre:", cama);
        Util.campo(formNovo, 2, "Início (dd/MM/aaaa):", inicio);
        Util.campo(formNovo, 3, "Fim (dd/MM/aaaa):", fim);
        JButton criar = new JButton("Criar contracto");
        Util.linhaBotoes(formNovo, 4, criar);
        Util.enterAciona(criar, inicio, fim);

        criar.addActionListener(e -> tarefas.executar(this, "Criar contracto", () -> {
            Requisicao r = Util.exigir((Requisicao) requisicao.getSelectedItem(), "Não há requisições aprovadas sem contracto");
            Cama c = Util.exigir((Cama) cama.getSelectedItem(), "Não há camas livres nesta residência");
            LocalDate ini = Util.data(inicio.getText(), "A data inicial");
            LocalDate f = Util.data(fim.getText(), "A data final");
            return () -> servico.criarContracto(r, c, ini, f);
        }));

        JPanel formEstado = Util.formulario("Estado (contracto selecionado)");
        JButton ativar = new JButton("Ativar");
        JButton terminar = new JButton("Terminar");
        JButton cancelar = new JButton("Cancelar");
        Util.linhaBotoes(formEstado, 0, ativar, terminar, cancelar);

        ativar.addActionListener(e -> tarefas.executar(this, "Ativar contracto", () -> {
            Contracto c = contracto();
            return () -> servico.ativarContracto(c);
        }));
        terminar.addActionListener(e -> tarefas.executar(this, "Terminar contracto", () -> {
            Contracto c = contracto();
            return () -> servico.terminarContracto(c);
        }));
        cancelar.addActionListener(e -> {
            Contracto c = Util.selecionado(tabela, linhas);
            if (c != null && JOptionPane.showConfirmDialog(this,
                    "Cancelar o contracto #" + c.getId() + " de " + c.getEstudante().getNome() + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            tarefas.executar(this, "Cancelar contracto", () -> {
                Contracto alvo = contracto();
                return () -> servico.cancelarContracto(alvo);
            });
        });

        // as camas disponíveis dependem da residência da requisição escolhida
        requisicao.addActionListener(e -> atualizarCamas());

        Util.aoDuploClique(tabela, () -> {
            Contracto c = Util.selecionado(tabela, linhas);
            if (c == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Contracto ").append(c.getId()).append(" · ").append(c.getEstado()).append('\n');
            sb.append("Estudante: ").append(c.getEstudante().getNome()).append('\n');
            sb.append("Residência: ").append(c.getResidencia().getNome()).append(" · cama ")
                    .append(c.getCama().getIdCama()).append(" (quarto ").append(c.getCama().getQuarto().getNumero()).append(")\n");
            sb.append("Período: ").append(Util.fmt(c.getDataInicial())).append(" a ").append(Util.fmt(c.getDataFinal()))
                    .append(" · assinado em ").append(Util.fmt(c.getDataAssinatura())).append('\n');
            sb.append("Preço mensal: ").append(c.getPrecoMensal().toPlainString()).append("\n\nPagamentos:\n");
            if (c.getPagamentos().isEmpty()) sb.append("  (nenhum)\n");
            for (Pagamento p : c.getPagamentos()) {
                sb.append("  #").append(p.getIdPagamento()).append(" · ").append(Util.fmt(p.getData())).append(" · ")
                        .append(p.getMetodo()).append(" · ").append(p.getTotalMeses()).append(" mês(es) · ")
                        .append(p.getValorTotal().toPlainString()).append('\n');
            }
            sb.append("\nTotal pago: ").append(c.getTotalPago().toPlainString()).append('\n');
            Util.detalhes(this, "Contracto " + c.getId(), sb.toString());
        });

        JPanel oeste = new JPanel();
        oeste.setLayout(new BoxLayout(oeste, BoxLayout.Y_AXIS));
        oeste.add(formNovo);
        oeste.add(Box.createVerticalStrut(8));
        oeste.add(formEstado);
        JPanel oesteWrap = new JPanel(new BorderLayout());
        oesteWrap.add(oeste, BorderLayout.NORTH);
        add(oesteWrap, BorderLayout.WEST);
        add(Util.comPesquisa(tabela, "Contractos (duplo clique para detalhes)"), BorderLayout.CENTER);

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private Contracto contracto() {
        return Util.exigir(Util.selecionado(tabela, linhas), "Seleciona um contracto na tabela");
    }

    private void atualizar() {
        List<Requisicao> elegiveis = servico.getRequisicoes().stream()
                .filter(r -> r.estaAprovada() && r.getContracto() == null)
                .collect(Collectors.toList());
        Util.preencher(requisicao, elegiveis);
        atualizarCamas();
        Util.carregar(tabela, modelo, linhas, servico.getContractos(), c -> new Object[]{
                c.getId(), c.getEstudante().getNome(), c.getResidencia().getNome(),
                c.getCama().getIdCama() + " (Q" + c.getCama().getQuarto().getNumero() + ")",
                Util.fmt(c.getDataAssinatura()), Util.fmt(c.getDataInicial()), Util.fmt(c.getDataFinal()),
                c.getEstado(), c.getTotalPago().toPlainString()});
    }

    private void atualizarCamas() {
        Requisicao r = (Requisicao) requisicao.getSelectedItem();
        List<Cama> livres = r == null ? Collections.emptyList() : r.getResidencia().getCamasLivres();
        Util.preencher(cama, livres);
    }

    @Override public JTable getTabelaPrincipal() { return tabela; }
    @Override public String getNomeExportacao() { return "contractos"; }
}