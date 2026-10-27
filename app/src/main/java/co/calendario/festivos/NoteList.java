package co.calendario.festivos;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class NoteList {
    private static String normalized(String text) {
        return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).trim().replaceAll("\\s+"," ");
    }
    /** Scope 0: todas; 1: año mostrado; 2: mes mostrado. */
    public static List<LocalDate> filter(Map<String,String> notes,YearMonth period,int scope,String query) {
        if(scope<0||scope>2)throw new IllegalArgumentException("Filtro desconocido");
        List<LocalDate> result=new ArrayList<>();String q=normalized(query);
        DateTimeFormatter formats=DateTimeFormatter.ofPattern("dd/MM/yyyy d/M/yyyy d MMMM yyyy",new Locale("es","CO"));
        for(Map.Entry<String,String> e:notes.entrySet()) {
            LocalDate date;
            try{date=LocalDate.parse(e.getKey());}catch(RuntimeException ignored){continue;}
            if(date.getYear()<2000||date.getYear()>2100||e.getValue()==null||e.getValue().trim().isEmpty())continue;
            if(scope==1&&date.getYear()!=period.getYear())continue;
            if(scope==2&&!YearMonth.from(date).equals(period))continue;
            if(!q.isEmpty()&&!normalized(e.getValue()+" "+date+" "+date.format(formats)).contains(q))continue;
            result.add(date);
        }
        Collections.sort(result);return result;
    }
}
