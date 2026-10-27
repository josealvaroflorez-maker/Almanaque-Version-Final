package co.calendario.festivos;
import android.content.*;
public final class DayIconReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){DayIconUpdater.refresh(context);}
}
