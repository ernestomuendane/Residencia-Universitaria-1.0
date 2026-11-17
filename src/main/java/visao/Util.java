package visao;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class Util {

    public static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Util() {
    }

    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    public static JTable tabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        t.getTableHeader().setReorderingAllowed(false);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);          // clicar no cabeçalho ordena
        instalarMenuContexto(t);
        return t;
    }

    private static void instalarMenuContexto(JTable t) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copiar = new JMenuItem("Copiar linha");
        copiar.addActionListener(e -> {
            int linha = t.getSelectedRow();
            if (linha < 0) return;
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < t.getColumnCount(); c++) {
                if (c > 0) sb.append('\t');
                sb.append(t.getValueAt(linha, c));
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        });
        menu.add(copiar);
        t.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { popup(e); }
            @Override public void mouseReleased(MouseEvent e) { popup(e); }
            private void popup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int linha = t.rowAtPoint(e.getPoint());
                if (linha >= 0) {
                    t.setRowSelectionInterval(linha, linha);
                    menu.show(t, e.getX(), e.getY());
                }
            }
        });
    }

    public static <T> T selecionado(JTable tabela, List<T> dados) {
        int vista = tabela.getSelectedRow();
        if (vista < 0) return null;
        int i = tabela.convertRowIndexToModel(vista);   // a ordenação e o filtro mudam a ordem visível
        return i < dados.size() ? dados.get(i) : null;
    }

    public static <T> void carregar(JTable tabela, DefaultTableModel modelo, List<T> destino,
                                    Collection<T> origem, Function<T, Object[]> linha) {
        T anterior = selecionado(tabela, destino);
        destino.clear();
        destino.addAll(origem);
        modelo.setRowCount(0);
        for (T x : destino) {
            modelo.addRow(linha.apply(x));
        }
        int i = anterior == null ? -1 : destino.indexOf(anterior);
        int vista = i < 0 ? -1 : tabela.convertRowIndexToView(i);
        if (vista >= 0) {
            tabela.setRowSelectionInterval(vista, vista);
        }
    }

    public static JPanel comPesquisa(JTable tabela, String titulo) {
        JTextField pesquisa = new JTextField(18);
        pesquisa.setToolTipText("Escreve para filtrar (Esc limpa)");
        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) tabela.getRowSorter();

        pesquisa.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = pesquisa.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
            }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        pesquisa.registerKeyboardAction(e -> pesquisa.setText(""),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        topo.add(new JLabel("Pesquisar:"));
        topo.add(pesquisa);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        p.add(topo, BorderLayout.NORTH);
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return p;
    }

    public static void aoDuploClique(JTable tabela, Runnable acao) {
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1
                        && tabela.rowAtPoint(e.getPoint()) >= 0) {
                    acao.run();
                }
            }
        });
    }

    public static void enterAciona(JButton botao, JTextField... campos) {
        for (JTextField c : campos) {
            c.addActionListener(e -> botao.doClick());
        }
    }

    public static void detalhes(Component pai, String titulo, String texto) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(pai), titulo, Dialog.ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        area.setCaretPosition(0);
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> d.dispose());
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.getRootPane().setDefaultButton(fechar);
        d.add(new JScrollPane(area), BorderLayout.CENTER);
        d.add(botoes(fechar), BorderLayout.SOUTH);
        d.setSize(540, 400);
        d.setLocationRelativeTo(pai);
        d.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    public static <T> JComboBox<T> combo(Function<T, String> texto) {
        JComboBox<T> c = new JComboBox<>();
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                                                          boolean sel, boolean foco) {
                super.getListCellRendererComponent(lista, valor, i, sel, foco);
                setText(valor == null ? "" : texto.apply((T) valor));
                return this;
            }
        });
        return c;
    }

    public static <T> void preencher(JComboBox<T> combo, Collection<T> itens) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        for (T t : itens) {
            combo.addItem(t);
        }
        if (anterior != null) {
            combo.setSelectedItem(anterior);
        }
    }

    public static void campo(JPanel painel, int linha, String rotulo, JComponent componente) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = linha;
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        painel.add(new JLabel(rotulo), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        painel.add(componente, g);
    }

    public static JPanel formulario(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        return p;
    }

    public static JPanel botoes(JButton... botoes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        for (JButton b : botoes) {
            p.add(b);
        }
        return p;
    }

    public static void linhaBotoes(JPanel form, int linha, JButton... botoes) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = linha;
        g.gridx = 0;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        form.add(botoes(botoes), g);
    }

    public static JScrollPane comTitulo(JComponent c, String titulo) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createTitledBorder(titulo));
        return s;
    }

    public static String obrigatorio(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return texto.trim();
    }

    public static int inteiro(String texto, String campo) {
        try {
            return Integer.parseInt(obrigatorio(texto, campo));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " deve ser um número inteiro");
        }
    }

    public static BigDecimal decimal(String texto, String campo) {
        try {
            return new BigDecimal(obrigatorio(texto, campo).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " deve ser um número");
        }
    }

    public static LocalDate data(String texto, String campo) {
        try {
            return LocalDate.parse(obrigatorio(texto, campo), FMT_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(campo + " inválida (usa dd/MM/aaaa)");
        }
    }

    public static <T> T exigir(T valor, String mensagem) {
        if (valor == null) {
            throw new IllegalStateException(mensagem);
        }
        return valor;
    }

    public static String fmt(LocalDate d) {
        return d == null ? "—" : d.format(FMT_DATA);
    }

    public static String fmt(LocalDateTime d) {
        return d == null ? "—" : d.format(FMT_DATA_HORA);
    }

    public static String hoje() {
        return LocalDate.now().format(FMT_DATA);
    }

    public static void info(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
}
