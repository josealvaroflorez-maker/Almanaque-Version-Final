package co.calendario.festivos;
import java.time.*;
import java.util.*;
public class Version10Check {
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    public static void main(String[] args) {
        Map<String,String> notes=new HashMap<>();
        notes.put("2026-10-18","Reunión con mamá");notes.put("2026-11-02","Viaje");notes.put("2025-10-18","Viaje anterior");
        notes.put("incorrecta","Ignorar");notes.put("2026-10-19","");notes.put("1999-01-01","Fuera de rango");
        YearMonth period=YearMonth.of(2026,10);
        require(NoteList.filter(notes,period,0,"").size()==3,"Todas las notas válidas");
        require(NoteList.filter(notes,period,1,"").size()==2,"Filtro de año");
        require(NoteList.filter(notes,period,2,"").equals(List.of(LocalDate.of(2026,10,18))),"Filtro de mes");
        require(NoteList.filter(notes,period,0,"REUNION").size()==1,"Sin tildes ni mayúsculas");
        require(NoteList.filter(notes,period,1,"viaje").equals(List.of(LocalDate.of(2026,11,2))),"Texto combinado con año");
        require(NoteList.filter(notes,period,2,"viaje").isEmpty(),"Sin coincidencias en mes");
        require(NoteList.filter(notes,period,0,"18/10/2025").size()==1,"Fecha numérica");
        require(NoteList.filter(notes,period,0,"18 octubre 2026").size()==1,"Fecha escrita");
        require(CalendarNavigation.restore("2027-04",LocalDate.of(2026,10,18)).equals(YearMonth.of(2027,4)),"Restaurar período válido");
        for(String saved:new String[]{null,"","dato incorrecto","1999-12","2101-01","2026-13"})
            require(CalendarNavigation.restore(saved,LocalDate.of(2026,10,18)).equals(period),"Recuperar preferencia inválida");
        require(CalendarNavigation.restore(null,LocalDate.of(2101,2,1)).equals(YearMonth.of(2100,2)),"Respetar límite superior al restaurar");
        System.out.println("OK: filtros de notas, búsqueda combinada y restauración de período.");
    }
}
