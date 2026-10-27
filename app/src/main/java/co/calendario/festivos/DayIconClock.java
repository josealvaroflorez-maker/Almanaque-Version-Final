package co.calendario.festivos;
import java.time.*;
public final class DayIconClock {
    public static final ZoneId COLOMBIA=ZoneId.of("America/Bogota");
    public static int day(Instant now){return now.atZone(COLOMBIA).getDayOfMonth();}
    public static long nextUpdate(Instant now){return now.atZone(COLOMBIA).toLocalDate().plusDays(1).atStartOfDay(COLOMBIA).plusMinutes(1).toInstant().toEpochMilli();}
}
