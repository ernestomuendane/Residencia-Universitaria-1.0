package dao;

import modelo.Residencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class ResidenciaDao {

    public Residencia inserir(Connection c, String nome, String tipo) throws SQLException {
        int id = Sql.inserir(c, "INSERT INTO Residencia (nome, tipo) VALUES (?, ?)", nome, tipo);
        return new Residencia(id, nome, tipo);
    }

    public void atualizar(Connection c, Residencia x) throws SQLException {
        Sql.executar(c, "UPDATE Residencia SET nome = ?, tipo = ? WHERE idResidencia = ?",
                x.getNome(), x.getTipo(), x.getIdResidencia());
    }

    public Map<Integer, Residencia> carregar(Connection c) throws SQLException {
        return Sql.mapa(c, "SELECT idResidencia, nome, tipo FROM Residencia ORDER BY idResidencia",
                rs -> new Residencia(rs.getInt("idResidencia"), rs.getString("nome"), rs.getString("tipo")),
                Residencia::getIdResidencia);
    }
}

