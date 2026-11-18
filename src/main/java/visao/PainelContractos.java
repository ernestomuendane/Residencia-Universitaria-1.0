package visao;

import modelo.*;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PainelContractos extends JPanel implements Exportavel {

    private final ServicoResidencia servico;
    private final DefaultTableModel modelo = Util.modelo("ID", "Estudante", "Residência", "Cama", "Assinatura", "Início", "Fim", "Estado", "Total pago", "Gerido por");
    private final JTable tabela = Util.tabela(modelo);
    private final List<Contracto> linhas = new ArrayList<>();

    private final JComboBox<Requisicao> requisicao = Util.combo(r ->
            "Requisição #" + r.getIdRequisicao() + " · " + r.getEstudante().getNome() + " · " + r.getResidencia().getNome());
    private final JComboBox<Cama> cama = Util.combo(c ->
            "Cama " + c.getIdCama() + " · Quarto " + c.getQuarto().getNumero());
    private final JTextField inicio = new JTextField(Util.agora(), 14);
    private final JTextField fim = new JTextField(Util.fmt(LocalDateTime.now().plusMonths(6)), 14);
    private final JLabel responsavel = new JLabel("—");

    public PainelContractos(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel formNovo = Util.formulario("Novo contracto (de requisição aprovada)");
        Util.campo(formNovo, 0, "Requisição:", requisicao);
        Util.campo(formNovo, 1, "Cama disponível:", cama);
        Util.campo(formNovo, 2, "Início (dd/MM/aaaa HH:mm):", inicio);
        Util.campo(formNovo, 3, "Fim (dd/MM/aaaa HH:mm):", fim);
        Util.campo(formNovo, 4, "Gerido por:", responsavel);
        JButton criar = new JButton("Gerar contracto");
        Util.linhaBotoes(formNovo, 5, criar);
        Util.enterAciona(criar, inicio, fim);

        criar.addActionListener(e -> tarefas.executar(this, "Gerar contracto", () -> {
            Requisicao r = Util.exigir((Requisicao) requisicao.getSelectedItem(), "Não há requisições aprovadas sem contracto");
            Cama c = Util.exigir((Cama) cama.getSelectedItem(), "Não há camas disponíveis nesta residência");
            LocalDateTime ini = Util.dataHora(inicio.getText(), "A data inicial");
            LocalDateTime f = Util.dataHora(fim.getText(), "A data final");
            return () -> servico.criarContracto(r, c, ini, f);
        }));

        JPanel formEstado = Util.formulario("Cancelamento");
        JButton cancelar = new JButton("Cancelar contracto selecionado");
        JButton cancelarPendentes = new JButton("Cancelar pendentes sem pagamento");
        Util.linhaBotoes(formEstado, 0, cancelar);
        Util.linhaBotoes(formEstado, 1, cancelarPendentes);

        cancelar.addActionListener(e -> {
            Contracto c = Util.selecionado(tabela, linhas);
            if (c != null && JOptionPane.showConfirmDialog(this,
                    "Cancelar o contracto #" + c.getId() + " de " + c.getEstudante().getNome()
                            + "? A cama " + c.getCama().getIdCama() + " fica disponível.",
                    "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            tarefas.executar(this, "Cancelar contracto", () -> {
                Contracto alvo = contracto();
                return () -> servico.cancelarContracto(alvo);
            });
        });
        cancelarPendentes.addActionListener(e -> cancelarPendentesSemPagamento(tarefas));

        requisicao.addActionListener(e -> atualizarCamas()); // a residência da requisição decide camas e responsável

        Util.aoDuploClique(tabela, () -> {
            Contracto c = Util.selecionado(tabela, linhas);
            if (c == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Contracto ").append(c.getId()).append(" · ").append(c.getEstado()).append('\n');
            sb.append("Estudante: ").append(c.getEstudante().getNome()).append('\n');
            sb.append("Residência: ").append(c.getResidencia().getNome()).append(" · cama ")
                    .append(c.getCama().getIdCama()).append(" (quarto ").append(c.getCama().getQuarto().getNumero())
                    .append(", ").append(c.getCama().getEstado()).append(")\n");
            sb.append("Período: ").append(Util.fmt(c.getDataInicial())).append(" a ").append(Util.fmt(c.getDataFinal()))
                    .append("\nAssinado em ").append(Util.fmt(c.getDataAssinatura())).append('\n');
            sb.append("Gerido por: ").append(c.getAdministrador() == null ? "—" : c.getAdministrador().getNome()).append('\n');
            sb.append("Preço mensal: ").append(c.getPrecoMensal().toPlainString()).append("\n\nPagamento:\n");
            Pagamento p = c.getPagamento();
            if (p == null) {
                sb.append("  (sem pagamento)\n");
            } else {
                sb.append("  #").append(p.getIdPagamento()).append(" · ").append(Util.fmt(p.getData())).append(" · ")
                        .append(p.getMetodo()).append(" · ").append(p.getTotalMeses()).append(" mês(es) · ")
                        .append(p.getValorTotal().toPlainString()).append('\n');
            }
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

    public void cancelarPendentesSemPagamento(Tarefas tarefas) {
        long n = servico.getContractos().stream().filter(Contracto::deveSerCancelado).count();
        if (n == 0) {
            Util.info(this, "Não há contractos pendentes sem pagamento.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Cancelar " + n + " contracto(s) pendente(s) sem pagamento?",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        tarefas.executar(this, "Cancelar pendentes sem pagamento", () -> servico::cancelarPendentesSemPagamento);
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
                c.getEstado(), c.getTotalPago().toPlainString(),
                c.getAdministrador() == null ? "—" : c.getAdministrador().getNome()});
    }

    private void atualizarCamas() {
        Requisicao r = (Requisicao) requisicao.getSelectedItem();
        List<Cama> livres = r == null ? Collections.<Cama>emptyList() : r.getResidencia().getCamasLivres();
        Util.preencher(cama, livres);
        responsavel.setText(r == null ? "—" : r.getResidencia().getAdministradorAtivo()
                .map(Administrador::getNome).orElse("sem administrador ativo"));
    }

    @Override public JTable getTabelaPrincipal() { return tabela; }
    @Override public String getNomeExportacao() { return "contractos"; }
}
