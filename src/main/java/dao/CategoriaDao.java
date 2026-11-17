package dao;

import modelo.Categoria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class CategoriaDao {

    public Categoria inserir(Connection c, String nome, int capacidade, BigDecimal preco) throws SQLException {
        int id = Sql.inserir(c,
                "INSERT INTO Categoria (nomeCategoria, capacidade, preco) VALUES (?, ?, ?)",
                nome, capacidade, preco);
        return new Categoria(id, nome, capacidade, preco);
    }

    public void atualizar(Connection c, Categoria x) throws SQLException {
        Sql.executar(c,
                "UPDATE Categoria SET nomeCategoria = ?, capacidade = ?, preco = ? WHERE idCategoria = ?",
                x.getNome(), x.getCapacidade(), x.getPreco(), x.getIdCategoria());
    }

    public Map<Integer, Categoria> carregar(Connection c) throws SQLException {
        return Sql.mapa(c,
                "SELECT idCategoria, nomeCategoria, capacidade, preco FROM Categoria ORDER BY idCategoria",
                rs -> new Categoria(rs.getInt("idCategoria"), rs.getString("nomeCategoria"),
                        rs.getInt("capacidade"), rs.getBigDecimal("preco")),
                Categoria::getIdCategoria);
    }
}