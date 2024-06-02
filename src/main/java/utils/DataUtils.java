package utils;

import java.sql.Date;
import java.time.LocalDate;

public class DataUtils {

    private DataUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate();
    }
}
