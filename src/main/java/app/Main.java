package app;

import servico.ServicoResidencia;
import visao.EcraArranque;
import visao.JanelaPrincipal;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignorada) {
            }
            ServicoResidencia servico = new ServicoResidencia();
            new EcraArranque(servico).iniciar(() -> new JanelaPrincipal(servico).setVisible(true));
        });
    }
}