package dao;

import modelo.Administrador;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class AdministradorDao {

    public Administrador inserir(Connection c, String nome, String telefone, String email,
                                 LocalDateTime dataInicio, String grau) throws SQLException {
        int id = Sql.inserir(c,
                "INSERT INTO Administrador (nome, telefone, email, dataInicio, grau) VALUES (?, ?, ?, ?, ?)",
                nome, telefone, email, Datas.ts(dataInicio), grau);
        return new Administrador(id, nome, telefone, email, dataInicio, grau);
    }

    public void atualizar(Connection c, Administrador x) throws SQLException {
        Sql.executar(c, "UPDATE Administrador SET telefone = ?, email = ?, grau = ? WHERE idAdministrador = ?",
                x.getTelefone(), x.getEmail(), x.getGrau(), x.getIdAdministrador());
    }

    public Map<Integer, Administrador> carregar(Connection c) throws SQLException {
        return Sql.mapa(c,
                "SELECT idAdministrador, nome, telefone, email, dataInicio, grau FROM Administrador ORDER BY idAdministrador",
                rs -> new Administrador(rs.getInt("idAdministrador"), rs.getString("nome"),
                        rs.getString("telefone"), rs.getString("email"),
                        Datas.dataHora(rs.getTimestamp("dataInicio")), rs.getString("grau")),
                Administrador::getIdAdministrador);
    }
}

