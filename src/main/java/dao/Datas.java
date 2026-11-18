package dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class Datas {

    private Datas() {
    }

    static Timestamp ts(LocalDate d) {
        return d == null ? null : Timestamp.valueOf(d.atStartOfDay());
    }

    static Timestamp ts(LocalDateTime d) {
        return d == null ? null : Timestamp.valueOf(d);
    }

    static LocalDate data(Timestamp t) {
        return t == null ? null : t.toLocalDateTime().toLocalDate();
    }

    static LocalDateTime dataHora(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}
