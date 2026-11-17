package visao;

import modelo.*;
import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PainelResumo extends JPanel {

    private final ServicoResidencia servico;

    private final JLabel residencias = valor();
    private final JLabel quartos = valor();
    private final JLabel camasLivres = valor();
    private final JLabel estudantes = valor();
    private final JLabel administradores = valor();
    private final JLabel requisicoesPendentes = valor();
    private final JLabel contractosAtivos = valor();
    private final JLabel totalRecebido = valor();
    private final JProgressBar ocupacao = new JProgressBar();
    private final JLabel ultima = new JLabel(" ");

    public PainelResumo(ServicoResidencia servico, Tarefas tarefas) {
        super(new BorderLayout(10, 10));
        this.servico = servico;
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel cartoes = new JPanel(new GridLayout(2, 4, 12, 12));
        cartoes.add(cartao("Residências", residencias));
        cartoes.add(cartao("Quartos", quartos));
        cartoes.add(cartao("Camas livres", camasLivres));
        cartoes.add(cartao("Estudantes", estudantes));
        cartoes.add(cartao("Administradores", administradores));
        cartoes.add(cartao("Requisições pendentes", requisicoesPendentes));
        cartoes.add(cartao("Contractos ativos", contractosAtivos));
        cartoes.add(cartao("Total recebido", totalRecebido));

        ocupacao.setStringPainted(true);
        ocupacao.setPreferredSize(new Dimension(100, 26));
        JPanel barra = new JPanel(new BorderLayout(8, 4));
        barra.setBorder(BorderFactory.createTitledBorder("Ocupação das camas"));
        barra.add(ocupacao, BorderLayout.CENTER);

        JButton atualizar = new JButton("Atualizar da base de dados (F5)");
        atualizar.addActionListener(e -> tarefas.executar(this, "Recarregar da base de dados", () -> servico::recarregar));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(ultima, BorderLayout.CENTER);
        rodape.add(atualizar, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.add(cartoes, BorderLayout.CENTER);
        centro.add(barra, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        servico.aoAlterar(this::atualizar);
        atualizar();
    }

    private static JLabel valor() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 30f));
        return l;
    }

    private static JPanel cartao(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)), new EmptyBorder(10, 10, 10, 10)));
        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        p.add(valor, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    private void atualizar() {
        int nQuartos = 0, camas = 0, livres = 0, ocupadas = 0;
        for (Residencia r : servico.getResidencias()) {
            nQuartos += r.getQuartos().size();
            for (Cama c : r.getCamas()) {
                camas++;
                if (c.getEstado() == EstadoCama.LIVRE) livres++;
                if (c.getEstado() == EstadoCama.OCUPADA) ocupadas++;
            }
        }
        long pendentes = servico.getRequisicoes().stream().filter(Requisicao::estaPendente).count();
        long ativos = servico.getContractos().stream().filter(c -> c.getEstado() == EstadoContracto.ATIVO).count();
        BigDecimal recebido = servico.getPagamentos().stream()
                .map(Pagamento::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        residencias.setText(String.valueOf(servico.getResidencias().size()));
        quartos.setText(String.valueOf(nQuartos));
        camasLivres.setText(livres + " / " + camas);
        estudantes.setText(String.valueOf(servico.getEstudantes().size()));
        administradores.setText(String.valueOf(servico.getAdministradores().size()));
        requisicoesPendentes.setText(String.valueOf(pendentes));
        contractosAtivos.setText(String.valueOf(ativos));
        totalRecebido.setText(recebido.toPlainString());

        ocupacao.setMaximum(Math.max(1, camas));
        ocupacao.setValue(ocupadas);
        int pct = camas == 0 ? 0 : Math.round(100f * ocupadas / camas);
        ocupacao.setString(ocupadas + " de " + camas + " camas ocupadas (" + pct + "%)");
        ultima.setText("Última atualização: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
