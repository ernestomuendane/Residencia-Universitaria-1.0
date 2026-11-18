package visao;

import servico.ServicoResidencia;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Tarefas {

    private final ServicoResidencia servico;
    private final ExecutorService bd = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "rue-bd");
        t.setDaemon(true);
        return t;
    });
    private final AtomicInteger escritas = new AtomicInteger();
    private final List<Consumer<Boolean>> ouvintesOcupado = new ArrayList<>();
    private final List<Consumer<String>> ouvintesMensagem = new ArrayList<>();
    private int emCurso; // só é tocado na EDT

    public Tarefas(ServicoResidencia servico) {
        this.servico = servico;
    }

    public void aoOcupar(Consumer<Boolean> ouvinte) {
        ouvintesOcupado.add(ouvinte);
    }

    public void aoMensagem(Consumer<String> ouvinte) {
        ouvintesMensagem.add(ouvinte);
    }

    public boolean ocupado() {
        return emCurso > 0;
    }

    public void encerrar() {
        bd.shutdown();
    }

    public void executar(Component pai, String descricao, Supplier<Runnable> preparar) {
        executar(pai, descricao, preparar, null);
    }

    public void executar(Component pai, String descricao, Supplier<Runnable> preparar, Runnable depois) {
        Runnable trabalho;
        try {
            trabalho = preparar.get();
        } catch (RuntimeException e) {
            avisar(pai, e);
            return;
        }
        submeter(pai, descricao, () -> {
            trabalho.run();
            return null;
        }, depois);
    }

    private void submeter(Component pai, String descricao, java.util.concurrent.Callable<Void> trabalho,
                          Runnable depois) {
        emCurso++;
        escritas.incrementAndGet();
        if (emCurso == 1) {
            ouvintesOcupado.forEach(o -> o.accept(true));
        }
        mensagem(descricao + "...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                return trabalho.call();
            }

            @Override
            protected void done() {
                emCurso--;
                if (emCurso == 0) {
                    ouvintesOcupado.forEach(o -> o.accept(false)); // liberta a janela antes de abrir diálogos
                }
                try {
                    get();
                    if (depois != null) {
                        depois.run();
                    }
                    mensagem(descricao + ": concluído");
                } catch (ExecutionException e) {
                    avisar(pai, e.getCause());
                    mensagem(descricao + ": falhou");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (emCurso == 0) {
                        servico.notificarAlteracao(); // as vistas só leem o modelo com a fila vazia
                    }
                }
            }
        };
        bd.execute(worker);
    }

    public void recarregarSeLivre() {
        if (emCurso > 0) {
            return;
        }
        final int geracao = escritas.get();
        SwingWorker<ServicoResidencia.Instantaneo, Void> worker = new SwingWorker<ServicoResidencia.Instantaneo, Void>() {
            @Override
            protected ServicoResidencia.Instantaneo doInBackground() {
                return servico.ler(null);
            }

            @Override
            protected void done() {
                try {
                    ServicoResidencia.Instantaneo foto = get();
                    if (emCurso == 0 && escritas.get() == geracao) {
                        servico.aplicar(foto);
                        servico.notificarAlteracao();
                        mensagem("Dados atualizados a partir da base de dados");
                    }
                } catch (ExecutionException e) {
                    mensagem("Não foi possível atualizar: " + e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        bd.execute(worker);
    }

    private void mensagem(String texto) {
        ouvintesMensagem.forEach(o -> o.accept(texto));
    }

    static void avisar(Component pai, Throwable erro) {
        String msg = erro.getMessage() == null ? erro.toString() : erro.getMessage();
        boolean regra = erro instanceof IllegalStateException || erro instanceof IllegalArgumentException;
        JOptionPane.showMessageDialog(pai, msg,
                regra ? "Operação não permitida" : "Erro",
                regra ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
