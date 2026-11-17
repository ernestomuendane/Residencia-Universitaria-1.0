package dao;

import java.sql.Connection;
import java.sql.SQLException;

public final class Transacao {

    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection ligacao) throws SQLException;
    }

    private Transacao() {
    }

    public static <T> T executar(Operacao<T> operacao) {
        try (Connection c = Conexao.obter()) {
            c.setAutoCommit(false);
            try {
                T resultado = operacao.executar(c);
                c.commit();
                return resultado;
            } catch (SQLException e) {
                c.rollback();
                throw new DaoException("Erro na base de dados: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Não foi possível usar a base de dados: " + e.getMessage(), e);
        }
    }
}
