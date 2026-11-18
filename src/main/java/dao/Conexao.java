package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexao {

    private static final String URL_POR_DEFEITO = "jdbc:mariadb://localhost:3306/rue";
    private static final Properties FICHEIRO = lerFicheiro();

    static {
        DriverManager.setLoginTimeout(5);
    }

    private Conexao() {
    }

    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(
                valor("rue.url", URL_POR_DEFEITO),
                valor("rue.utilizador", "root"),
                valor("rue.palavraPasse", ""));
    }

    public static boolean disponivel() {
        try (Connection c = obter()) {
            return c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private static String valor(String chave, String defeito) {
        return System.getProperty(chave, FICHEIRO.getProperty(chave, defeito));
    }

    private static Properties lerFicheiro() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("rue.properties")) {
            p.load(in);
        } catch (IOException ignorada) {
        }
        return p;
    }
}