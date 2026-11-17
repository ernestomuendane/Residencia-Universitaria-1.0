package visao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;


public class Ocupado extends JComponent {

    private final Timer atraso = new Timer(250, e -> {
        mostrarAviso = true;
        repaint();
    });
    private boolean mostrarAviso;
    private Component focoAnterior;

    public Ocupado() {
        setOpaque(false);
        setFocusable(true);
        atraso.setRepeats(false);
        addMouseListener(new MouseAdapter() { });
        addMouseMotionListener(new MouseAdapter() { });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                e.consume();
            }
        });
    }

    public void ativar() {
        focoAnterior = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        mostrarAviso = false;
        setVisible(true);
        requestFocusInWindow();
        atraso.restart();
    }

    public void desativar() {
        atraso.stop();
        mostrarAviso = false;
        setVisible(false);
        if (focoAnterior != null) {
            focoAnterior.requestFocusInWindow();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!mostrarAviso) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
        String texto = "A processar...";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(texto)) / 2;
        int y = getHeight() / 2;
        g2.setColor(new Color(40, 40, 40));
        g2.drawString(texto, x, y);
        g2.dispose();
    }
}
