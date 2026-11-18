package dao;

import modelo.Categoria;
import modelo.Quarto;
import modelo.Residencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class QuartoDao {

    public Quarto inserir(Connection c, Residencia residencia, int numero, Categoria categoria) throws SQLException {
        int id = Sql.inserir(c,
                "INSERT INTO Quarto (numero, idResidencia, idCategoria) VALUES (?, ?, ?)",
                numero, residencia.getIdResidencia(), categoria.getIdCategoria());
        return residencia.adicionarQuarto(id, numero, categoria);
    }

    public void atualizar(Connection c, Quarto x) throws SQLException {
        Sql.executar(c, "UPDATE Quarto SET idCategoria = ? WHERE idQuarto = ?",
                x.getCategoria().getIdCategoria(), x.getIdQuarto());
    }

    public Map<Integer, Quarto> carregar(Connection c, Map<Integer, Residencia> residencias,
                                         Map<Integer, Categoria> categorias) throws SQLException {
        return Sql.mapa(c,
                "SELECT idQuarto, numero, idResidencia, idCategoria FROM Quarto ORDER BY idQuarto",
                rs -> new Quarto(rs.getInt("idQuarto"), rs.getInt("numero"),
                        categorias.get(rs.getInt("idCategoria")), residencias.get(rs.getInt("idResidencia"))),
                Quarto::getIdQuarto);
    }
}
