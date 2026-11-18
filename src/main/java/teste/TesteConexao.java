package teste;

import dao.Conexao;
import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {

        try (Connection conexao = Conexao.obter()) {

            if (conexao.isValid(2)) {
                System.out.println("Conexão estabelecida com sucesso!");
                System.out.println("Base de dados: " + conexao.getCatalog());
            }

        } catch (SQLException e) {
            System.out.println("Erro ao estabelecer conexão!");
            System.out.println(e.getMessage());
        }
    }
}