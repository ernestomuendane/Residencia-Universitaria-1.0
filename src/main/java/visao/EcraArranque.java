package visao;

import servico.ServicoResidencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EcraArranque extends JFrame {

    private final ServicoResidencia servico;
    private final JLabel mensagem = new JLabel("A iniciar...");
    private final JProgressBar barra = new JProgressBar(0, 100);

    public EcraArranque(ServicoResidencia servico) {
        super("Residência Universitária");
        this.servico = servico;
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Sistema de Gestão de Residência Universitária");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        barra.setStringPainted(true);

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY), new EmptyBorder(20, 28, 20, 28)));
        p.add(titulo, BorderLayout.NORTH);
        p.add(mensagem, BorderLayout.CENTER);
        p.add(barra, BorderLayout.SOUTH);
        setContentPane(p);
        setSize(460, 150);
        setLocationRelativeTo(null);
    }

    public void iniciar(Runnable aoConcluir) {
        setVisible(true);
        barra.setValue(0);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                servico.recarregar((n, total, texto) -> {
                    setProgress(100 * n / total);   // dispara PropertyChange "progress" na EDT
                    publish(texto);                 // entrega mensagens a process(), na EDT
                });
                return null;
            }

            @Override
            protected void process(List<String> textos) {
                mensagem.setText(textos.get(textos.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get();
                    dispose();
                    aoConcluir.run();
                } catch (ExecutionException e) {
                    falhou(e.getCause(), aoConcluir);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                barra.setValue((Integer) evt.getNewValue());
            }
        });
        worker.execute();
    }

    private void falhou(Throwable erro, Runnable aoConcluir) {
        Object[] opcoes = {"Tentar novamente", "Sair"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Não foi possível ligar à base de dados rue.\n" + erro.getMessage()
                        + "\n\nVerifica se o MariaDB está a correr, se o script sql/rue.sql foi executado\n"
                        + "e se o utilizador e a palavra-passe em rue.properties estão certos.",
                "Erro de ligação", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, opcoes, opcoes[0]);
        if (escolha == 0) {
            iniciar(aoConcluir);
        } else {
            System.exit(1);
        }
    }
}