package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Gestao {
    private LocalDateTime dataInicio = LocalDateTime.now();
    private LocalDateTime dataFim;
    private String estado;

    public Gestao(LocalDateTime dataInicio, LocalDateTime dataFim, String estado){
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.estado = estado;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
