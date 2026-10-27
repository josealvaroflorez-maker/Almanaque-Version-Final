package co.calendario.festivos;
import java.time.YearMonth;

public final class CalendarNavigation {
    public static YearMonth restore(String saved,java.time.LocalDate today) {
        if(saved!=null) {
            try {
                YearMonth value=YearMonth.parse(saved);
                if(value.getYear()>=2000&&value.getYear()<=2100)return value;
            }catch(java.time.DateTimeException ignored) { }
        }
        return YearMonth.of(Math.max(2000,Math.min(2100,today.getYear())),today.getMonthValue());
    }
    public static YearMonth move(YearMonth current,boolean annual,int direction) {
        if(direction!=1&&direction!=-1)return current;
        YearMonth next=annual?current.plusYears(direction):current.plusMonths(direction);
        return next.getYear()<2000||next.getYear()>2100?current:next;
    }
    public static int swipeDirection(float dx,float dy,long elapsed,float density) {
        if(density<=0||elapsed<0||elapsed>800||Math.abs(dx)<72*density||Math.abs(dx)<Math.abs(dy)*1.5f)return 0;
        return dx<0?1:-1;
    }
}
