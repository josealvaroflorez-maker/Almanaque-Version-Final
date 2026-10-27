package co.calendario.festivos;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Texto compartible: las notas solo se incluyen por elección explícita. */
public final class DayShare {
    private DayShare() { }
    public static String text(LocalDate day, String note, boolean includeNote) {
        StringBuilder out=new StringBuilder("Calendario Colombia\n");
        out.append(day.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' uuuu",new Locale("es","CO"))));
        boolean holiday=false;
        for(Holidays.Holiday h:Holidays.year(day.getYear())) if(h.date.equals(day)) {
            holiday=true;out.append("\nFestivo: ").append(h.name);
            if(!h.date.equals(h.original)) out.append(" (trasladado desde ").append(h.original).append(")");
        }
        if(!holiday) out.append("\nNo es festivo nacional.");
        if(day.getDayOfWeek()==DayOfWeek.SUNDAY) out.append("\nDomingo.");
        if(includeNote&&note!=null&&!note.trim().isEmpty()) out.append("\n\nMi nota:\n").append(note.trim());
        return out.toString();
    }
}
