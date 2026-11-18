package dao;

import modelo.Administrador;
import modelo.Cama;
import modelo.Contracto;
import modelo.EstadoContracto;
import modelo.Requisicao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class ContractoDao {

    public Contracto inserir(Connection c, Requisicao requisicao, Cama cama, Administrador administrador,
                             LocalDateTime inicial, LocalDateTime fim) throws SQLException {
        LocalDateTime assinatura = LocalDateTime.now().withNano(0);
        int id = Sql.inserir(c,
                "INSERT INTO Contracto (dataAssinatura, dataInicial, dataFinal, estado, idRequisicao, idCama, idEstudante, idAdministrador) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Datas.ts(assinatura), Datas.ts(inicial), Datas.ts(fim), EstadoContracto.PENDENTE.name(),
                requisicao.getIdRequisicao(), cama.getIdCama(), requisicao.getEstudante().getIdEstudante(),
                administrador.getIdAdministrador());
        return new Contracto(id, assinatura, inicial, fim, requisicao, cama, administrador);
    }

    public void atualizar(Connection c, Contracto x) throws SQLException {
        Sql.executar(c, "UPDATE Contracto SET estado = ? WHERE idContracto = ?", x.getEstado().name(), x.getId());
    }

    public Map<Integer, Contracto> carregar(Connection c, Map<Integer, Requisicao> requisicoes,
                                            Map<Integer, Cama> camas,
                                            Map<Integer, Administrador> administradores) throws SQLException {
        return Sql.mapa(c,
                "SELECT idContracto, dataAssinatura, dataInicial, dataFinal, estado, idRequisicao, idCama, idAdministrador "
                        + "FROM Contracto ORDER BY idContracto",
                rs -> {
                    int idAdmin = rs.getInt("idAdministrador");
                    Administrador admin = rs.wasNull() ? null : administradores.get(idAdmin);
                    return new Contracto(rs.getInt("idContracto"),
                            Datas.dataHora(rs.getTimestamp("dataAssinatura")),
                            Datas.dataHora(rs.getTimestamp("dataInicial")),
                            Datas.dataHora(rs.getTimestamp("dataFinal")),
                            EstadoContracto.valueOf(rs.getString("estado")),
                            requisicoes.get(rs.getInt("idRequisicao")),
                            camas.get(rs.getInt("idCama")),
                            admin);
                },
                Contracto::getId);
    }
}

