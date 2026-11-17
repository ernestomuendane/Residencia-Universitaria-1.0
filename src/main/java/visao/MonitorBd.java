package visao;

import servico.ServicoResidencia;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class MonitorBd {

    private final ScheduledExecutorService agenda = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "rue-monitor");
        t.setDaemon(true);
        return t;
    });
    private final Tarefas tarefas;
    private ScheduledFuture<?> atualizacaoAutomatica;

    public MonitorBd(ServicoResidencia servico, BarraEstado barra, Tarefas tarefas) {
        this.tarefas = tarefas;
        agenda.scheduleWithFixedDelay(() -> {
            long inicio = System.nanoTime();
            boolean ativa = servico.baseDeDadosDisponivel();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            SwingUtilities.invokeLater(() -> barra.ligacao(ativa, ms)); // de volta à EDT
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void atualizacaoAutomatica(boolean ligada, int segundos) {
        if (atualizacaoAutomatica != null) {
            atualizacaoAutomatica.cancel(false);
            atualizacaoAutomatica = null;
        }
        if (ligada) {
            atualizacaoAutomatica = agenda.scheduleWithFixedDelay(
                    () -> SwingUtilities.invokeLater(tarefas::recarregarSeLivre),
                    segundos, segundos, TimeUnit.SECONDS);
        }
    }

    public void parar() {
        agenda.shutdownNow();
    }
}
