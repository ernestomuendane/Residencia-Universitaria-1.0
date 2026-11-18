package dao;

import modelo.Estudante;
import modelo.Sexo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EstudanteDao {

    public Estudante inserir(Connection c, String nome, String telefone, String email,
                             Sexo sexo, String curso) throws SQLException {
        int id = Sql.inserir(c,
                "INSERT INTO Estudante (nome, telefone, email, sexo, curso) VALUES (?, ?, ?, ?, ?)",
                nome, telefone, email, sexo.name(), curso);
        return new Estudante(id, nome, telefone, email, sexo, curso);
    }

    public void atualizar(Connection c, Estudante x) throws SQLException {
        Sql.executar(c, "UPDATE Estudante SET telefone = ?, email = ?, curso = ? WHERE idEstudante = ?",
                x.getTelefone(), x.getEmail(), x.getCurso(), x.getIdEstudante());
    }

    public Map<Integer, Estudante> carregar(Connection c) throws SQLException {
        return Sql.mapa(c,
                "SELECT idEstudante, nome, telefone, email, sexo, curso FROM Estudante ORDER BY idEstudante",
                rs -> new Estudante(rs.getInt("idEstudante"), rs.getString("nome"), rs.getString("telefone"),
                        rs.getString("email"), Sexo.valueOf(rs.getString("sexo")), rs.getString("curso")),
                Estudante::getIdEstudante);
    }
}

