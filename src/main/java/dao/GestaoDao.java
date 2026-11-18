package dao;

import modelo.Administrador;
import modelo.EstadoGestao;
import modelo.Gestao;
import modelo.Residencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class GestaoDao {

    public Gestao inserir(Connection c, Administrador administrador, Residencia residencia,
                          LocalDateTime dataInicio) throws SQLException {
        Sql.executar(c,
                "INSERT INTO Gestao (idResidencia, idAdministrador, dataInicio, dataFim, estado) VALUES (?, ?, ?, NULL, ?)",
                residencia.getIdResidencia(), administrador.getIdAdministrador(),
                Datas.ts(dataInicio), EstadoGestao.ATIVA.name());
        return administrador.gerir(residencia, dataInicio);
    }

    public void atualizar(Connection c, Gestao g) throws SQLException {
        Sql.executar(c,
                "UPDATE Gestao SET dataFim = ?, estado = ? WHERE idResidencia = ? AND idAdministrador = ? AND dataInicio = ?",
                Datas.ts(g.getDataFim()), g.getEstado().name(),
                g.getResidencia().getIdResidencia(), g.getAdministrador().getIdAdministrador(),
                Datas.ts(g.getDataInicio()));
    }

    public void carregar(Connection c, Map<Integer, Administrador> administradores,
                         Map<Integer, Residencia> residencias) throws SQLException {
        Sql.percorrer(c,
                "SELECT idResidencia, idAdministrador, dataInicio, dataFim, estado FROM Gestao ORDER BY dataInicio",
                rs -> {
                    new Gestao(administradores.get(rs.getInt("idAdministrador")),
                            residencias.get(rs.getInt("idResidencia")),
                            Datas.dataHora(rs.getTimestamp("dataInicio")),
                            Datas.dataHora(rs.getTimestamp("dataFim")),
                            EstadoGestao.valueOf(rs.getString("estado")));
                    return null;
                });
    }
}

