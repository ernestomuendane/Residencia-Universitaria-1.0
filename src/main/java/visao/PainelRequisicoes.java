package visao;

import modelo.*;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PainelRequisicoes extends JPanel implements Exportavel {

    private final ServicoResidencia servico;
    private final DefaultTableModel modelo = Util.modelo("ID", "Data", "Estudante", "Residência", "Resultado", "Decidida por", "Contracto");
    private final JTable tabela = Util.tabela(modelo);
    private final List<Requisicao> linhas = new ArrayList<>();

    private final JComboBox<Estudante> estudante = Util.combo(e -> e.getIdEstudante() + " · " + e.getNome());
    private final JComboBox<Residencia> residencia = Util.combo(Residencia::getNome);
    private final JComboBox<Administrador> administrador = Util.combo(a -> a.getIdAdministrador() + " · " + a.getNome());

    public PainelRequisicoes(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(8, 8));
        this.servico = servico;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel formNova = Util.formulario("Nova requisição");
        Util.campo(formNova, 0, "Estudante:", estudante);
        Util.campo(formNova, 1, "Residência:", residencia);
        JButton criar = new JButton("Submeter requisição");
        Util.linhaBotoes(formNova, 2, criar);

        criar.addActionListener(e -> tarefas.executar(this, "Submeter requisição", () -> {
            Estudante s = Util.exigir((Estudante) estudante.getSelectedItem(), "Regista primeiro um estudante");
            Residencia r = Util.exigir((Residencia) residencia.getSelectedItem(), "Cria primeiro uma residência");
            return () -> servico.criarRequisicao(s, r);
        }));

        JPanel formDecisao = Util.formulario("Decisão (requisição selecionada)");
        Util.campo(formDecisao, 0, "Administrador:", administrador);
        JButton aprovar = new JButton("Aprovar");
        JButton rejeitar = new JButton("Rejeitar");
        Util.linhaBotoes(formDecisao, 1, aprovar, rejeitar);

        aprovar.addActionListener(e -> tarefas.executar(this, "Aprovar requisição", () -> {
            Administrador a = admin();
            Requisicao r = requisicao();
            return () -> servico.aprovarRequisicao(a, r);
        }));
        rejeitar.addActionListener(e -> tarefas.executar(this, "Rejeitar requisição", () -> {
            Administrador a = admin();
            Requisicao r = requisicao();
            return () -> servico.rejeitarRequisicao(a, r);
        }));

        Util.aoDuploClique(tabela, () -> {
            Requisicao r = Util.selecionado(tabela, linhas);
            if (r == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Requisição ").append(r.getIdRequisicao()).append(" · ").append(Util.fmt(r.getData())).append('\n');
            sb.append("Estudante: ").append(r.getEstudante().getNome()).append(" (").append(r.getEstudante().getCurso()).append(")\n");
            sb.append("Residência: ").append(r.getResidencia().getNome()).append('\n');
            sb.append("Resultado: ").append(r.getResultado()).append('\n');
            sb.append("Decidida por: ").append(r.getAdministrador() == null ? "—" : r.getAdministrador().getNome()).append('\n');
            if (r.getContracto() != null) {
                Contracto c = r.getContracto();
                sb.append("\nContracto #").append(c.getId()).append(" · ").append(c.getEstado())
                        .append(" · cama ").append(c.getCama().getIdCama()).append('\n');
            }
            Util.detalhes(this, "Requisição " + r.getIdRequisicao(), sb.toString());
        });

        JPanel oeste = new JPanel();
        oeste.setLayout(new BoxLayout(oeste, BoxLayout.Y_AXIS));
        oeste.add(formNova);
        oeste.add(Box.createVerticalStrut(8));
        oeste.add(formDecisao);
        JPanel oesteWrap = new JPanel(new BorderLayout());
        oesteWrap.add(oeste, BorderLayout.NORTH);
        add(oesteWrap, BorderLayout.WEST);
        add(Util.comPesquisa(tabela, "Requisições (duplo clique para detalhes)"), BorderLayout.CENTER);

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private Requisicao requisicao() {
        return Util.exigir(Util.selecionado(tabela, linhas), "Seleciona uma requisição na tabela");
    }

    private Administrador admin() {
        return Util.exigir((Administrador) administrador.getSelectedItem(), "Regista primeiro um administrador");
    }

    private void atualizar() {
        Util.preencher(estudante, servico.getEstudantes());
        Util.preencher(residencia, servico.getResidencias());
        Util.preencher(administrador, servico.getAdministradores());
        Util.carregar(tabela, modelo, linhas, servico.getRequisicoes(), r -> new Object[]{
                r.getIdRequisicao(), Util.fmt(r.getData()), r.getEstudante().getNome(),
                r.getResidencia().getNome(), r.getResultado(),
                r.getAdministrador() == null ? "—" : r.getAdministrador().getNome(),
                r.getContracto() == null ? "—" : "#" + r.getContracto().getId()});
    }

    @Override public JTable getTabelaPrincipal() { return tabela; }
    @Override public String getNomeExportacao() { return "requisicoes"; }
}