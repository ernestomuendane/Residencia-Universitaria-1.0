package visao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BarraEstado extends JPanel {

    private static final DateTimeFormatter RELOGIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JLabel mensagem = new JLabel("Pronto");
    private final JProgressBar progresso = new JProgressBar();
    private final JLabel ligacao = new JLabel("● a verificar...");
    private final JLabel relogio = new JLabel();

    public BarraEstado() {
        super(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(3, 8, 3, 8));

        progresso.setIndeterminate(true);
        progresso.setVisible(false);
        progresso.setPreferredSize(new Dimension(110, 14));

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        direita.setOpaque(false);
        direita.add(progresso);
        direita.add(ligacao);
        direita.add(relogio);

        add(mensagem, BorderLayout.CENTER);
        add(direita, BorderLayout.EAST);

        Timer t = new Timer(1000, e -> relogio.setText(LocalDateTime.now().format(RELOGIO)));
        t.setInitialDelay(0);
        t.start();
    }

    public void mensagem(String texto) {
        mensagem.setText(texto);
    }

    public void ocupado(boolean ocupado) {
        progresso.setVisible(ocupado);
    }

    public void ligacao(boolean ativa, long milissegundos) {
        ligacao.setForeground(ativa ? new Color(30, 130, 60) : new Color(190, 40, 40));
        ligacao.setText(ativa ? "● BD ligada (" + milissegundos + " ms)" : "● BD sem resposta");
    }
}
