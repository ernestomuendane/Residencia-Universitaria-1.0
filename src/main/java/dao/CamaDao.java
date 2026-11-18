package dao;

import modelo.Cama;
import modelo.EstadoCama;
import modelo.Quarto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class CamaDao {

    public Cama inserir(Connection c, Quarto quarto) throws SQLException {
        int id = Sql.inserir(c, "INSERT INTO Cama (estado, idQuarto) VALUES (?, ?)",
                EstadoCama.DISPONIVEL.name(), quarto.getIdQuarto());
        return quarto.adicionarCama(id);
    }

    public void atualizar(Connection c, Cama x) throws SQLException {
        Sql.executar(c, "UPDATE Cama SET estado = ? WHERE idCama = ?", x.getEstado().name(), x.getIdCama());
    }

    public Map<Integer, Cama> carregar(Connection c, Map<Integer, Quarto> quartos) throws SQLException {
        return Sql.mapa(c, "SELECT idCama, estado, idQuarto FROM Cama ORDER BY idCama",
                rs -> new Cama(rs.getInt("idCama"), EstadoCama.valueOf(rs.getString("estado")),
                        quartos.get(rs.getInt("idQuarto"))),
                Cama::getIdCama);
    }
}

