package co.calendario.festivos;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.util.Log;
import java.time.Instant;
import java.util.Locale;

public final class DayIconUpdater {
    private static ComponentName component(Context c,int day){return new ComponentName(c.getPackageName(),c.getPackageName()+String.format(Locale.ROOT,".DayIcon%02d",day));}
    private static boolean enabled(PackageManager pm,ComponentName component,int day) {
        int state=pm.getComponentEnabledSetting(component);
        return state==PackageManager.COMPONENT_ENABLED_STATE_ENABLED||(state==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT&&day==1);
    }
    public static synchronized boolean refresh(Context source) {
        Context c=source.getApplicationContext();boolean success=false;
        try {
            PackageManager pm=c.getPackageManager();int today=DayIconClock.day(Instant.now());ComponentName target=component(c,today);
            // Primero se habilita el nuevo acceso; nunca se deshabilita la Activity real.
            if(!enabled(pm,target,today))pm.setComponentEnabledSetting(target,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
            if(enabled(pm,target,today)) {
                for(int day=1;day<=31;day++)if(day!=today) {
                    ComponentName old=component(c,day);
                    if(enabled(pm,old,day))pm.setComponentEnabledSetting(old,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                }
                success=true;
            }
        } catch(RuntimeException error){Log.w("DayIcon","No se pudo cambiar el icono",error);}
        long scheduled=0;
        try {scheduled=schedule(c);}catch(RuntimeException error){Log.w("DayIcon","No se pudo programar la actualización",error);}
        c.getSharedPreferences("icon_update_status",Context.MODE_PRIVATE).edit().putLong("last_request",System.currentTimeMillis())
            .putBoolean("last_success",success).putLong("scheduled_for",scheduled).apply();
        return success;
    }    public static String status(Context source) {
        Context c=source.getApplicationContext();String selected="No se pudo comprobar";
        try {
            int count=0,dayEnabled=0;PackageManager pm=c.getPackageManager();
            for(int day=1;day<=31;day++)if(enabled(pm,component(c,day),day)){count++;dayEnabled=day;}
            selected=count==1?String.valueOf(dayEnabled):"No confirmado. Solicita una actualización.";
        }catch(RuntimeException error){Log.w("DayIcon","No se pudo leer el estado",error);}
        android.content.SharedPreferences saved=c.getSharedPreferences("icon_update_status",Context.MODE_PRIVATE);
        long last=saved.getLong("last_request",0),next=saved.getLong("scheduled_for",0);
        java.time.format.DateTimeFormatter format=java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",new Locale("es","CO")).withZone(DayIconClock.COLOMBIA);
        return "Día actual de Colombia: "+DayIconClock.day(Instant.now())+"\nIcono configurado: "+selected+
            "\nÚltima solicitud: "+(last==0?"Sin registro":format.format(Instant.ofEpochMilli(last)))+
            "\nResultado de la solicitud: "+(last==0?"Sin registro":saved.getBoolean("last_success",false)?"Configurado":"No se pudo completar")+
            "\nPróximo intento programado: "+(next==0?"Sin programación confirmada":format.format(Instant.ofEpochMilli(next)))+
            "\n\nHoras de Colombia. La programación puede retrasarse. Este estado comprueba la configuración de la app; el lanzador puede tardar en mostrar el cambio.";
    }

    private static long schedule(Context c) {
        Intent intent=new Intent(c,DayIconReceiver.class).setAction("co.calendario.festivos.UPDATE_DAY_ICON");
        PendingIntent pending=PendingIntent.getBroadcast(c,1101,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarms=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(alarms==null)throw new IllegalStateException("AlarmManager no disponible");
        long next=DayIconClock.nextUpdate(Instant.now());
        alarms.setAndAllowWhileIdle(AlarmManager.RTC,next,pending);return next;
    }
}
