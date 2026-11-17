package visao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class Exportador {

    private Exportador() {
    }

    public static void csv(File ficheiro, List<String> cabecalho, List<List<String>> linhas) {
        try (BufferedWriter w = Files.newBufferedWriter(ficheiro.toPath(), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            escrever(w, cabecalho);
            for (List<String> linha : linhas) {
                escrever(w, linha);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível escrever " + ficheiro + ": " + e.getMessage(), e);
        }
    }

    private static void escrever(BufferedWriter w, List<String> campos) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            String c = campos.get(i) == null ? "" : campos.get(i);
            if (c.contains(";") || c.contains("\"") || c.contains("\n")) {
                c = '"' + c.replace("\"", "\"\"") + '"';
            }
            sb.append(c);
        }
        w.write(sb.toString());
        w.newLine();
    }
}
