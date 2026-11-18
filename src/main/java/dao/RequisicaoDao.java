package dao;

import modelo.Administrador;
import modelo.Estudante;
import modelo.Requisicao;
import modelo.Residencia;
import modelo.ResultadoRequisicao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class RequisicaoDao {

    public Requisicao inserir(Connection c, Estudante estudante, Residencia residencia) throws SQLException {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        int id = Sql.inserir(c,
                "INSERT INTO Requisicao (data, resultado, idEstudante, idResidencia, idAdministrador) VALUES (?, ?, ?, ?, NULL)",
                Datas.ts(agora), ResultadoRequisicao.PENDENTE.name(),
                estudante.getIdEstudante(), residencia.getIdResidencia());
        return new Requisicao(id, agora, estudante, residencia);
    }

    public void atualizar(Connection c, Requisicao r) throws SQLException {
        Integer admin = r.getAdministrador() == null ? null : r.getAdministrador().getIdAdministrador();
        Sql.executar(c, "UPDATE Requisicao SET resultado = ?, idAdministrador = ? WHERE idRequisicao = ?",
                r.getResultado().name(), admin, r.getIdRequisicao());
    }

    public Map<Integer, Requisicao> carregar(Connection c, Map<Integer, Estudante> estudantes,
                                             Map<Integer, Residencia> residencias,
                                             Map<Integer, Administrador> administradores) throws SQLException {
        return Sql.mapa(c,
                "SELECT idRequisicao, data, resultado, idEstudante, idResidencia, idAdministrador FROM Requisicao ORDER BY idRequisicao",
                rs -> {
                    int idAdmin = rs.getInt("idAdministrador");
                    Administrador admin = rs.wasNull() ? null : administradores.get(idAdmin);
                    return new Requisicao(rs.getInt("idRequisicao"), Datas.dataHora(rs.getTimestamp("data")),
                            ResultadoRequisicao.valueOf(rs.getString("resultado")),
                            estudantes.get(rs.getInt("idEstudante")),
                            residencias.get(rs.getInt("idResidencia")), admin);
                },
                Requisicao::getIdRequisicao);
    }
}

