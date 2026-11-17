package apresentacao;

import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        setTitle("Sistema de Gestão para Residência Universitária");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane separadores = new JTabbedPane();
        separadores.addTab("Estudantes", new JPanel());
        separadores.addTab("Residências", new JPanel());
        separadores.addTab("Quartos", new JPanel());
        separadores.addTab("Camas", new JPanel());
        separadores.addTab("Categorias", new JPanel());

        add(separadores, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}