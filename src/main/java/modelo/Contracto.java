package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class Contracto {
    private int idContracto;
    private LocalDateTime  dataAssinatura = LocalDateTime.now();
    private Date dataInicial;
    private Date dataFinal;
    private String estado;

    public Contracto(int idContracto, LocalDateTime dataAssinatura, Date dataInicial, Date dataFinal) {
        this.idContracto = idContracto;
        this.dataAssinatura = dataAssinatura;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.estado = estado;
    }



}
