package visao;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

public class MenuPrincipal extends JMenuBar {

    public interface Acoes {
        void carregarExemplo();

        void exportarSeparadorAtual();

        void recarregar();

        void cancelarPendentesSemPagamento();

        void atualizacaoAutomatica(boolean ligada);

        void sair();

        void mostrarAtalhos();

        void mostrarSobre();
    }

    public MenuPrincipal(Acoes acoes, int intervaloAtualizacaoSegundos) {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        JMenu ficheiro = new JMenu("Ficheiro");
        ficheiro.setMnemonic(KeyEvent.VK_F);

        JMenuItem exemplo = new JMenuItem("Carregar dados de exemplo");
        exemplo.addActionListener(e -> acoes.carregarExemplo());

        JMenuItem exportar = new JMenuItem("Exportar separador atual para CSV...");
        exportar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ctrl));
        exportar.addActionListener(e -> acoes.exportarSeparadorAtual());

        JMenuItem recarregar = new JMenuItem("Recarregar da base de dados");
        recarregar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        recarregar.addActionListener(e -> acoes.recarregar());

        JMenuItem sair = new JMenuItem("Sair");
        sair.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl));
        sair.addActionListener(e -> acoes.sair());

        ficheiro.add(exemplo);
        ficheiro.add(exportar);
        ficheiro.addSeparator();
        ficheiro.add(recarregar);
        ficheiro.addSeparator();
        ficheiro.add(sair);

        JMenu operacoes = new JMenu("Operações");
        operacoes.setMnemonic(KeyEvent.VK_O);
        JMenuItem pendentes = new JMenuItem("Cancelar contractos pendentes sem pagamento");
        pendentes.addActionListener(e -> acoes.cancelarPendentesSemPagamento());
        operacoes.add(pendentes);

        JMenu ver = new JMenu("Ver");
        ver.setMnemonic(KeyEvent.VK_V);
        JCheckBoxMenuItem automatica = new JCheckBoxMenuItem(
                "Atualização automática (" + intervaloAtualizacaoSegundos + " s)", true);
        automatica.addItemListener(e -> acoes.atualizacaoAutomatica(e.getStateChange() == ItemEvent.SELECTED));
        ver.add(automatica);

        JMenu ajuda = new JMenu("Ajuda");
        ajuda.setMnemonic(KeyEvent.VK_A);
        JMenuItem atalhos = new JMenuItem("Atalhos de teclado");
        atalhos.addActionListener(e -> acoes.mostrarAtalhos());
        JMenuItem sobre = new JMenuItem("Sobre");
        sobre.addActionListener(e -> acoes.mostrarSobre());
        ajuda.add(atalhos);
        ajuda.add(sobre);

        add(ficheiro);
        add(operacoes);
        add(ver);
        add(ajuda);
    }
}
