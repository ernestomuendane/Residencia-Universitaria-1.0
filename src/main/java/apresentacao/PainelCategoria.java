package apresentacao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PainelCategoria extends JPanel {

    private JTextField campoNome = new JTextField(15);
    private JTextField campoPreco = new JTextField(15);
    private JTextField campoCapacidade = new JTextField(15);

    private JButton botaoGuardar = new JButton("Guardar");
    private JButton botaoActualizar = new JButton("Actualizar");
    private JButton botaoEliminar = new JButton("Eliminar");
    private JButton botaoLimpar = new JButton("Limpar");

    private DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nome", "Preço", "Capacidade"}, 0);
    private JTable tabela = new JTable(modelo);

    public PainelCategoria() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new GridLayout(3, 2, 5, 5));
        formulario.add(new JLabel("Nome:"));
        formulario.add(campoNome);
        formulario.add(new JLabel("Preço:"));
        formulario.add(campoPreco);
        formulario.add(new JLabel("Capacidade:"));
        formulario.add(campoCapacidade);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(botaoGuardar);
        botoes.add(botaoActualizar);
        botoes.add(botaoEliminar);
        botoes.add(botaoLimpar);

        JPanel topo = new JPanel(new BorderLayout(5, 5));
        topo.add(formulario, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // apagar quando ligar à base de dados
        modelo.addRow(new Object[]{1, "Exemplo A", 1000, 1});
        modelo.addRow(new Object[]{2, "Exemplo B", 500, 4});
    }
}