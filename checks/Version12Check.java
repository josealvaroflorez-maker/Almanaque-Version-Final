package co.calendario.festivos;
import java.time.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class Version12Check {
    private static void require(boolean c,String message){if(!c)throw new AssertionError(message);}
    public static void main(String[] args) {
        Map<String,String> notes=new HashMap<>();
        notes.put("2026-11-02","Viaje con mamá 🙂\nSegunda línea\tcon tabulación");
        notes.put("2026-10-27","Reunión de trabajo");
        Map<String,String> original=new HashMap<>(notes);
        String text=new String(NotesText.encode(notes,Instant.parse("2026-10-28T03:30:00Z")),StandardCharsets.UTF_8);
        require(text.contains("CALENDARIO COLOMBIA")&&text.contains("Total: 2 notas"),"Encabezado y total");
        require(text.contains("27/10/2026 22:30"),"Fecha de exportación en Colombia");
        require(text.contains("Viaje con mamá 🙂\nSegunda línea\tcon tabulación"),"Unicode y formato de notas");
        require(text.indexOf("Reunión")<text.indexOf("Viaje"),"Orden cronológico");
        require(notes.equals(original),"No modifica notas originales");
        require(new String(NotesText.encode(Map.of(),Instant.EPOCH),StandardCharsets.UTF_8).contains("Total: 0 notas"),"Colección vacía");
        for(Map<String,String> invalid:List.of(Map.of("2026-02-30","Fecha inválida"),Map.of("1999-01-01","Fuera de rango"),Map.of("2026-01-01"," "),Map.of("2026-01-01","x".repeat(4001)),Map.of("2026-01-01","\uD800"))) {
            try{NotesText.encode(invalid,Instant.EPOCH);throw new AssertionError("Se aceptó una nota no válida");}
            catch(IllegalArgumentException|java.time.DateTimeException expected) { }
        }
        System.out.println("OK: exportación legible, fecha colombiana, Unicode, orden, validación y datos intactos.");
    }
}
