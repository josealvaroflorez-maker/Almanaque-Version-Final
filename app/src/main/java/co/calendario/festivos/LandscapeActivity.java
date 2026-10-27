package co.calendario.festivos;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Visor de la colección mensual. No cambia el calendario hasta pulsar Ver este mes. */
public final class LandscapeActivity extends Activity {
    private int month,year;private float scale=1f;private LinearLayout root;private ImageView image;
    private TextView label,description;
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state) {
        setTheme(android.R.style.Theme_Material_NoActionBar);super.onCreate(state);
        month=state==null?getIntent().getIntExtra("month",1):state.getInt("month",1);
        year=state==null?getIntent().getIntExtra("year",java.time.LocalDate.now(DayIconClock.COLOMBIA).getYear()):state.getInt("year",java.time.LocalDate.now(DayIconClock.COLOMBIA).getYear());
        month=Math.max(1,Math.min(12,month));year=Math.max(2000,Math.min(2100,year));
        int size=getSharedPreferences("appearance",MODE_PRIVATE).getInt("reading_size",0);scale=size==1?1.12f:size==2?1.25f:1f;
        getWindow().getDecorView().setSystemUiVisibility(0);getWindow().setStatusBarColor(0xff10211c);getWindow().setNavigationBarColor(0xff10211c);
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff10211c);
        boolean scrollLayout=getResources().getConfiguration().screenHeightDp<500||getResources().getConfiguration().fontScale*scale>1.6f;
        if(scrollLayout){ScrollView viewport=new ScrollView(this);viewport.setFillViewport(true);viewport.addView(root);setContentView(viewport);}else setContentView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{root.setPadding(dp(16),insets.getSystemWindowInsetTop(),dp(16),insets.getSystemWindowInsetBottom());return insets;});root.requestApplyInsets();
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text("Paisajes del campo",21);title.setTypeface(null,Typeface.BOLD);heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        heading.addView(button("Cerrar",()->finish()),new LinearLayout.LayoutParams(-2,-2));root.addView(heading);
        label=text("",19);label.setGravity(Gravity.CENTER);root.addView(label);
        image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);root.addView(image,scrollLayout?new LinearLayout.LayoutParams(-1,dp(220)):new LinearLayout.LayoutParams(-1,0,1));
        description=text("",17);description.setGravity(Gravity.CENTER);root.addView(description);
        TextView note=text("Paisajes por mes · 12 imágenes · Sin conexión",12);note.setGravity(Gravity.CENTER);note.setTextColor(0xff97dfbc);root.addView(note);
        LinearLayout nav=new LinearLayout(this);
        Button previous=button("‹ Anterior",()->{month=month==1?12:month-1;showMonth();});previous.setContentDescription("Ver paisaje del mes anterior");
        Button next=button("Siguiente ›",()->{month=month==12?1:month+1;showMonth();});next.setContentDescription("Ver paisaje del mes siguiente");
        nav.addView(previous,new LinearLayout.LayoutParams(0,-2,1));nav.addView(next,new LinearLayout.LayoutParams(0,-2,1));root.addView(nav);
        Button open=button("Ver este mes en el calendario",()->{
            setResult(RESULT_OK,new Intent().putExtra("month",month).putExtra("year",year));finish();
        });root.addView(open,new LinearLayout.LayoutParams(-1,-2));showMonth();
    }
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size*scale);t.setTextColor(Color.WHITE);t.setPadding(dp(8),dp(8),dp(8),dp(8));return t;}
    private Button button(String value,Runnable action){Button b=new Button(this);b.setText(value);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTextSize(14*scale);b.setMinHeight(dp(48));b.setPadding(dp(10),dp(8),dp(10),dp(8));GradientDrawable bg=new GradientDrawable();bg.setColor(0xff254b3d);bg.setCornerRadius(dp(16));b.setBackground(bg);b.setOnClickListener(v->action.run());return b;}
    private void showMonth(){
        String title=YearMonth.of(year,month).format(DateTimeFormatter.ofPattern("MMMM yyyy",new Locale("es","CO")));
        label.setText(title);description.setText(LandscapeAssets.description(this,month));PersonalLandscapes.show(this,image,month,false);
        image.setContentDescription("Paisaje de "+title+". "+LandscapeAssets.description(this,month));
    }
    @Override public void onSaveInstanceState(Bundle state){state.putInt("month",month);state.putInt("year",year);super.onSaveInstanceState(state);}
}
