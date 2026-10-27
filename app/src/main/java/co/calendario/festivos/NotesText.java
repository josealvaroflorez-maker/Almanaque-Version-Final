package co.calendario.festivos;
import java.nio.*;
import java.nio.charset.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class NotesText {
    public static byte[] encode(Map<String,String> notes,Instant exportedAt) {
        Locale locale=new Locale("es","CO");
        StringBuilder text=new StringBuilder("CALENDARIO COLOMBIA\nNotas personales\nExportado: ")
            .append(exportedAt.atZone(DayIconClock.COLOMBIA).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",locale)))
            .append(" · hora de Colombia\nTotal: ").append(notes.size()).append(" notas\n\n");
        for(Map.Entry<String,String> entry:new TreeMap<>(notes).entrySet()) {
            LocalDate day=LocalDate.parse(entry.getKey());String note=entry.getValue();
            if(day.getYear()<2000||day.getYear()>2100||note==null||note.trim().isEmpty()||note.length()>4000)throw new IllegalArgumentException("Nota no válida");
            text.append(day.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM 'de' yyyy",locale))).append('\n')
                .append(note).append("\n\n");
            if(text.length()>NoteBackup.MAX_BYTES)throw new IllegalArgumentException("El archivo supera 16 MB");
        }
        try {
            ByteBuffer encoded=StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(text));
            if(encoded.remaining()>NoteBackup.MAX_BYTES)throw new IllegalArgumentException("El archivo supera 16 MB");
            byte[] bytes=new byte[encoded.remaining()];encoded.get(bytes);return bytes;
        }catch(CharacterCodingException ex){throw new IllegalArgumentException("Texto no válido",ex);}
    }
}
