package dao;

import modelo.Contracto;
import modelo.MetodoPagamento;
import modelo.Pagamento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class PagamentoDao {

    public Pagamento inserir(Connection c, Contracto contracto, MetodoPagamento metodo, int meses) throws SQLException {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        BigDecimal valor = contracto.getPrecoMensal().multiply(BigDecimal.valueOf(meses));
        int id = Sql.inserir(c,
                "INSERT INTO Pagamento (data, metodo, totalMeses, valorTotal, idContracto, idEstudante) VALUES (?, ?, ?, ?, ?, ?)",
                Datas.ts(agora), metodo.name(), meses, valor,
                contracto.getId(), contracto.getEstudante().getIdEstudante());
        return new Pagamento(id, agora, metodo, meses, contracto);
    }

    public void carregar(Connection c, Map<Integer, Contracto> contractos) throws SQLException {
        Sql.percorrer(c,
                "SELECT idPagamento, data, metodo, totalMeses, valorTotal, idContracto FROM Pagamento ORDER BY idPagamento",
                rs -> {
                    Pagamento.restaurar(rs.getInt("idPagamento"), Datas.dataHora(rs.getTimestamp("data")),
                            MetodoPagamento.valueOf(rs.getString("metodo")), rs.getInt("totalMeses"),
                            rs.getBigDecimal("valorTotal"), contractos.get(rs.getInt("idContracto")));
                    return null;
                });
    }
}