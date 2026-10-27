package co.calendario.festivos;
import java.time.LocalDate;
public final class Version16Check {
 public static void main(String[] args) {
  String privateNote="Mi cita privada ñ";
  String basic=DayShare.text(LocalDate.of(2026,1,12),privateNote,false);
  require(basic.contains("Reyes Magos")&&basic.contains("2026-01-06"),"Festivo trasladado");
  require(!basic.contains(privateNote)&&!basic.contains("Mi nota"),"Excluir nota por defecto");
  require(DayShare.text(LocalDate.of(2026,1,12),privateNote,true).contains(privateNote),"Incluir nota elegida");
  String sunday=DayShare.text(LocalDate.of(2026,1,11),null,true);
  require(sunday.contains("Domingo.")&&sunday.contains("No es festivo nacional"),"Domingo sin festivo");
  String collision=DayShare.text(LocalDate.of(2025,6,30),"",true);
  require(collision.contains("San Pedro")&&collision.contains("Sagrado Corazón"),"Festivos coincidentes");
  require(!collision.contains("Mi nota"),"Nota vacía");
  System.out.println("Version16Check OK");
 }
 private static void require(boolean value,String message) {if(!value)throw new AssertionError(message);}
}
