package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

final class Sql {

    @FunctionalInterface
    interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private Sql() {
    }

    private static PreparedStatement preparar(Connection c, String sql, boolean chaves, Object... parametros)
            throws SQLException {
        PreparedStatement ps = chaves
                ? c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : c.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
        return ps;
    }

    static int inserir(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(c, sql, true, parametros)) {
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    static int executar(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(c, sql, false, parametros)) {
            return ps.executeUpdate();
        }
    }

    static <T> Map<Integer, T> mapa(Connection c, String sql, Mapeador<T> mapeador, ToIntFunction<T> chave,
                                    Object... parametros) throws SQLException {
        Map<Integer, T> resultado = new LinkedHashMap<>();
        try (PreparedStatement ps = preparar(c, sql, false, parametros);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                T objeto = mapeador.mapear(rs);
                resultado.put(chave.applyAsInt(objeto), objeto);
            }
        }
        return resultado;
    }

    static void percorrer(Connection c, String sql, Mapeador<Void> consumidor, Object... parametros)
            throws SQLException {
        try (PreparedStatement ps = preparar(c, sql, false, parametros);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                consumidor.mapear(rs);
            }
        }
    }
}
