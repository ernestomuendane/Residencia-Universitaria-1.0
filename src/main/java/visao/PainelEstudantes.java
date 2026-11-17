
    package visao;

import modelo.Estudante;
import modelo.Requisicao;
import modelo.Sexo;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

    public class PainelEstudantes extends JPanel implements Exportavel {

        private final ServicoResidencia servico;
        private final DefaultTableModel modelo = Util.modelo("ID", "Nome", "Telefone", "Email", "Sexo", "Curso", "Requisições");
        private final JTable tabela = Util.tabela(modelo);
        private final List<Estudante> linhas = new ArrayList<>();

        private final JTextField nome = new JTextField(14);
        private final JTextField telefone = new JTextField(14);
        private final JTextField email = new JTextField(14);
        private final JComboBox<Sexo> sexo = new JComboBox<>(Sexo.values());
        private final JTextField curso = new JTextField(14);

        public PainelEstudantes(ServicoResidencia servico, Tarefas tarefas) {
            super(new BorderLayout(8, 8));
            this.servico = servico;
            setBorder(new EmptyBorder(8, 8, 8, 8));

            JPanel form = Util.formulario("Estudante");
            Util.campo(form, 0, "Nome:", nome);
            Util.campo(form, 1, "Telefone:", telefone);
            Util.campo(form, 2, "Email:", email);
            Util.campo(form, 3, "Sexo:", sexo);
            Util.campo(form, 4, "Curso:", curso);
            JButton registar = new JButton("Registar");
            JButton guardar = new JButton("Guardar alterações");
            Util.linhaBotoes(form, 5, registar, guardar);
            Util.enterAciona(registar, nome, telefone, email, curso);

            registar.addActionListener(e -> tarefas.executar(this, "Registar estudante", () -> {
                String n = Util.obrigatorio(nome.getText(), "O nome");
                String t = telefone.getText().trim();
                String m = email.getText().trim();
                Sexo s = (Sexo) sexo.getSelectedItem();
                String c = curso.getText();
                return () -> servico.registarEstudante(n, t, m, s, c);
            }, () -> {
                nome.setText("");
                telefone.setText("");
                email.setText("");
                curso.setText("");
            }));

            guardar.addActionListener(e -> tarefas.executar(this, "Alterar estudante", () -> {
                Estudante s = Util.exigir(Util.selecionado(tabela, linhas), "Seleciona um estudante na tabela");
                String t = telefone.getText().trim();
                String m = email.getText().trim();
                String c = curso.getText();
                return () -> servico.alterarEstudante(s, t, m, c);
            }));

            tabela.getSelectionModel().addListSelectionListener(e -> {
                Estudante s = Util.selecionado(tabela, linhas);
                if (!e.getValueIsAdjusting() && s != null) {
                    nome.setText(s.getNome());
                    telefone.setText(s.getTelefone());
                    email.setText(s.getEmail());
                    sexo.setSelectedItem(s.getSexo());
                    curso.setText(s.getCurso());
                }
            });

            Util.aoDuploClique(tabela, () -> {
                Estudante s = Util.selecionado(tabela, linhas);
                if (s == null) return;
                StringBuilder sb = new StringBuilder();
                sb.append(s.getNome()).append(" (id ").append(s.getIdEstudante()).append(")\n");
                sb.append("Curso: ").append(s.getCurso()).append(" · Sexo: ").append(s.getSexo()).append('\n');
                sb.append("Telefone: ").append(s.getTelefone()).append(" · Email: ").append(s.getEmail()).append("\n\n");
                sb.append("Requisições:\n");
                if (s.getRequisicoes().isEmpty()) sb.append("  (nenhuma)\n");
                for (Requisicao r : s.getRequisicoes()) {
                    sb.append("  #").append(r.getIdRequisicao()).append(" · ").append(r.getResidencia().getNome())
                            .append(" · ").append(r.getResultado()).append(" · ").append(Util.fmt(r.getData())).append('\n');
                }
                s.getContractoAtivo().ifPresent(c -> sb.append("\nContracto ativo: #").append(c.getId())
                        .append(" · cama ").append(c.getCama().getIdCama()).append('\n'));
                Util.detalhes(this, s.getNome(), sb.toString());
            });

            JPanel oeste = new JPanel(new BorderLayout());
            oeste.add(form, BorderLayout.NORTH);
            add(oeste, BorderLayout.WEST);
            add(Util.comPesquisa(tabela, "Estudantes (duplo clique para detalhes)"), BorderLayout.CENTER);

            servico.aoAlterar(this::atualizar);
            atualizar();
        }

        private void atualizar() {
            Util.carregar(tabela, modelo, linhas, servico.getEstudantes(), s -> new Object[]{
                    s.getIdEstudante(), s.getNome(), s.getTelefone(), s.getEmail(),
                    s.getSexo(), s.getCurso(), s.getRequisicoes().size()});
        }

        @Override public JTable getTabelaPrincipal() { return tabela; }
        @Override public String getNomeExportacao() { return "estudantes"; }
    }

