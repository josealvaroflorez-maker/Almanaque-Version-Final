package co.calendario.festivos;
import java.time.*;
public class Version11Check {
    private static void check(String now,int day,String next) {
        Instant instant=Instant.parse(now);
        if(DayIconClock.day(instant)!=day)throw new AssertionError("Día de Colombia: "+now);
        if(!Instant.ofEpochMilli(DayIconClock.nextUpdate(instant)).equals(Instant.parse(next)))throw new AssertionError("Próxima actualización: "+now);
    }
    public static void main(String[] args) {
        check("2026-10-27T04:59:59Z",26,"2026-10-27T05:01:00Z");
        check("2026-10-27T05:00:00Z",27,"2026-10-28T05:01:00Z");
        check("2026-12-31T18:00:00Z",31,"2027-01-01T05:01:00Z");
        check("2024-02-28T18:00:00Z",28,"2024-02-29T05:01:00Z");
        check("2024-02-29T18:00:00Z",29,"2024-03-01T05:01:00Z");
        check("2025-02-28T18:00:00Z",28,"2025-03-01T05:01:00Z");
        for(int y=2024;y<=2027;y++)for(int m=1;m<=12;m++)for(int d=1;d<=YearMonth.of(y,m).lengthOfMonth();d++) {
            Instant now=LocalDate.of(y,m,d).atTime(23,59).atZone(DayIconClock.COLOMBIA).toInstant();
            if(DayIconClock.day(now)!=d||DayIconClock.nextUpdate(now)<=now.toEpochMilli())throw new AssertionError("Límite diario");
        }
        System.out.println("OK: número de día colombiano, medianoche, fin de mes/año y bisiestos.");
    }
}
