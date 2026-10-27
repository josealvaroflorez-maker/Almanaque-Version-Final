package co.calendario.festivos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ColorDrawable;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Gravity;
import android.graphics.Paint;
import android.text.InputType;
import android.text.InputFilter;
import android.content.SharedPreferences;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainActivity extends Activity {
    private int INK, GREEN, RED, HOLIDAY_BG, SURFACE, PAGE;
    private static final int EXPORT_NOTES=701, IMPORT_NOTES=702, EXPORT_TEXT=703, VIEW_LANDSCAPE=704, PICK_MONTH_PHOTO=705;
    private final ExecutorService fileWorker=Executors.newSingleThreadExecutor();
    private AlertDialog workingDialog;
    private LocalDate selectedDate;
    private int pendingPhotoMonth;
    private float readingScale=1f;
    private boolean landscapes=true;
    private boolean dark;
    private EditText noteEditor;
    private LocalDate editingDate;
    private final Locale locale=new Locale("es", "CO");
    private YearMonth month;
    private LocalDate today;
    private boolean annual;
    private LinearLayout root, body;

    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private String date(LocalDate d) { return d.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM",locale)); }
    private TextView text(String s,int size,int color) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size*readingScale); t.setTextColor(color);
        t.setPadding(dp(8),dp(7),dp(8),dp(7)); return t;
    }
    private void bold(TextView t) { t.setTypeface(null,Typeface.BOLD); }
    private GradientDrawable bg(int color) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(20)); return d;
    }
    private Button button(String s,Runnable action) {
        Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(INK);
        b.setBackground(buttonBackground(SURFACE)); b.setTextSize(15*readingScale); b.setPadding(dp(12),dp(10),dp(12),dp(10));
        b.setStateListAnimator(null);
        b.setMinHeight(dp(48)); b.setOnClickListener(v->action.run()); return b;
    }
    @Override public void onCreate(Bundle state) {
        dark=getSharedPreferences("appearance",MODE_PRIVATE).getBoolean("dark",false);
        setTheme(dark?android.R.style.Theme_Material_NoActionBar:android.R.style.Theme_Material_Light_NoActionBar);
        super.onCreate(state);
        int readingSize=getSharedPreferences("appearance",MODE_PRIVATE).getInt("reading_size",0);
        readingScale=readingSize==1?1.12f:readingSize==2?1.25f:1f;
        INK=dark?0xffedf5ef:0xff183c32;
        GREEN=dark?0xff97dfbc:0xff16654d;
        RED=dark?0xffff8e9b:0xffb42332;
        HOLIDAY_BG=dark?0xff47232d:0xffffe8eb;
        SURFACE=dark?0xff1b3029:0xffffffff;
        PAGE=dark?0xff10211c:0xfff1f5ef;
        getWindow().setStatusBarColor(PAGE);
        getWindow().setNavigationBarColor(PAGE);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        today=LocalDate.now(ZoneId.of("America/Bogota"));
        String savedPeriod=state!=null?state.getString("month"):
            getPreferences(MODE_PRIVATE).getBoolean("remember_period",true)?getPreferences(MODE_PRIVATE).getString("last_period",null):null;
        month=CalendarNavigation.restore(savedPeriod,today);
        annual=state==null?getPreferences(MODE_PRIVATE).getBoolean("annual",false):state.getBoolean("annual");
        if(state!=null&&state.containsKey("selectedDate")) selectedDate=LocalDate.parse(state.getString("selectedDate"));
        pendingPhotoMonth=state==null?0:state.getInt("pendingPhotoMonth",0);
        render();
        if(state!=null&&state.containsKey("editingDate")) {
            openNote(LocalDate.parse(state.getString("editingDate")),state.getString("noteDraft",""));
        }
    }
    @Override public void onResume() {
        super.onResume();
        DayIconUpdater.refresh(this);
        LocalDate now=LocalDate.now(ZoneId.of("America/Bogota"));
        if(!now.equals(today)) { today=now; render(); }
    }
    @Override public void onSaveInstanceState(Bundle state) {
        state.putString("month",month.toString()); state.putBoolean("annual",annual);
        if(editingDate!=null&&noteEditor!=null) {
            state.putString("editingDate",editingDate.toString()); state.putString("noteDraft",noteEditor.getText().toString());
        }
        if(selectedDate!=null) state.putString("selectedDate",selectedDate.toString());
        state.putInt("pendingPhotoMonth",pendingPhotoMonth);
        super.onSaveInstanceState(state);
    }
    @Override public void onDestroy() {
        if(workingDialog!=null) workingDialog.dismiss();
        fileWorker.shutdown(); super.onDestroy();
    }
    private void setAnnual(boolean value) {
        annual=value; getPreferences(MODE_PRIVATE).edit().putBoolean("annual",value).apply(); render();
    }
    private void step(int direction) {
        YearMonth next=CalendarNavigation.move(month,annual,direction);
        if(!next.equals(month)) { month=next; render(); }
    }
    private void render() {
        landscapes=getSharedPreferences("appearance",MODE_PRIVATE).getBoolean("show_landscapes",true);
        if(getPreferences(MODE_PRIVATE).getBoolean("remember_period",true))getPreferences(MODE_PRIVATE).edit().putString("last_period",month.toString()).apply();
        root=new LinearLayout(this); root.setOrientation(1); root.setBackgroundColor(PAGE);
        setContentView(root);
        root.setOnApplyWindowInsetsListener((v,insets)-> {
            root.setPadding(dp(12),insets.getSystemWindowInsetTop(),dp(12),insets.getSystemWindowInsetBottom());
            return insets;
        }); root.requestApplyInsets();
        LinearLayout heading=new LinearLayout(this); heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text(getString(co.calendario.festivos.R.string.app_name),22,INK); bold(title);
        heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button menu=button("☰",()->showTools()); menu.setTextSize(23); menu.setContentDescription("Abrir menú de herramientas");
        heading.addView(menu,new LinearLayout.LayoutParams(dp(52),dp(52))); root.addView(heading);
        LinearLayout modes=new LinearLayout(this);
        for(boolean value:new boolean[]{false,true}) {
            Button b=button(value?"Anual":"Mensual",()->setAnnual(value));
            b.setSelected(annual==value);
            b.setContentDescription((value?"Vista anual":"Vista mensual")+(annual==value?", seleccionada":""));
            b.setBackground(buttonBackground(annual==value?GREEN:SURFACE));
            b.setTextColor(annual==value?(dark?PAGE:Color.WHITE):INK);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);
            lp.setMargins(dp(3),dp(3),dp(3),dp(5)); modes.addView(b,lp);
        } root.addView(modes);
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        Button prev=button("‹",()->step(-1)); prev.setContentDescription(annual?"Año anterior":"Mes anterior");
        prev.setEnabled(annual?month.getYear()>2000:month.isAfter(YearMonth.of(2000,1)));
        prev.setAlpha(prev.isEnabled()?1f:0.35f); nav.addView(prev,new LinearLayout.LayoutParams(dp(52),dp(48)));
        TextView label=text(annual?String.valueOf(month.getYear()):month.format(DateTimeFormatter.ofPattern("MMMM yyyy",locale)),20,INK);
        bold(label); label.setGravity(Gravity.CENTER); label.setMinHeight(dp(48));
        label.setContentDescription(label.getText()+". Toca para seleccionar año"); label.setOnClickListener(v->chooseYear());
        nav.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        Button next=button("›",()->step(1)); next.setContentDescription(annual?"Año siguiente":"Mes siguiente");
        next.setEnabled(annual?month.getYear()<2100:month.isBefore(YearMonth.of(2100,12)));
        next.setAlpha(next.isEnabled()?1f:0.35f); nav.addView(next,new LinearLayout.LayoutParams(dp(52),dp(48))); root.addView(nav);
        Button todayButton=button("Hoy · "+today.format(DateTimeFormatter.ofPattern("dd MMM yyyy",locale)),()->{
            today=LocalDate.now(ZoneId.of("America/Bogota")); selectedDate=today; month=YearMonth.from(today); render();
        });
        todayButton.setContentDescription("Volver a la fecha actual de Colombia"); root.addView(todayButton);
        ScrollView scroll=new CalendarScroll(); body=new LinearLayout(this); body.setOrientation(1);
        scroll.addView(body); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body.addView(text(annual?"Desliza para cambiar de año · Toca un mes para abrirlo":"Desliza para cambiar de mes · Toca una fecha para ver sus notas",12,GREEN));
        List<Holidays.Holiday> holidays=Holidays.year(month.getYear());
        if(annual) renderYear(holidays); else {
            if(landscapes)body.addView(monthLandscape(month,false));
            LinearLayout calendarCard=new LinearLayout(this);calendarCard.setOrientation(1);calendarCard.setPadding(dp(8),dp(8),dp(8),dp(10));
            calendarCard.setBackground(bg(SURFACE));calendarCard.addView(calendar(month,holidays,false));
            LinearLayout.LayoutParams calendarSpace=new LinearLayout.LayoutParams(-1,-2);calendarSpace.setMargins(0,dp(10),0,dp(8));body.addView(calendarCard,calendarSpace);
            renderHolidayList(holidays,false);
        }
        TextView legend=text("Domingos y festivos en rojo · Hoy con borde verde\nFechas subrayadas: tienen una nota · Borde dorado: fecha elegida",12,RED); body.addView(legend);
        if(!annual) body.addView(button("Ver calendario anual",()->setAnnual(true)));
        body.addView(text("Toca el encabezado para elegir año · 2000–2100\nTu vista elegida se guarda automáticamente.\nFestivos nacionales según Ley 51 de 1983. No incluye celebraciones locales ni futuros cambios normativos.\nCalendario Colombia · Versión 16.0 · Sin conexión",12,GREEN));
    }
    private FrameLayout monthLandscape(YearMonth period,boolean compact) {
        int index=period.getMonthValue()-1;
        FrameLayout frame=new FrameLayout(this);frame.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(compact?96:180)));
        frame.setBackground(bg(SURFACE));frame.setClipToOutline(true);
        ImageView photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);PersonalLandscapes.show(this,photo,index+1,compact);
        photo.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        frame.addView(photo,new FrameLayout.LayoutParams(-1,-1));
        android.view.View shade=new android.view.View(this);
        GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x00000000,0x15000000,0xcc10251b});
        shade.setBackground(gradient);frame.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        String label=compact?period.format(DateTimeFormatter.ofPattern("MMMM",locale)):LandscapeAssets.description(this,index+1);
        TextView caption=text(label,compact?16:20,Color.WHITE);bold(caption);caption.setPadding(dp(14),dp(14),dp(14),dp(12));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);frame.addView(caption,cp);
        frame.setContentDescription((compact?"Abrir ":"Paisaje de ")+period.format(DateTimeFormatter.ofPattern("MMMM yyyy",locale))+". "+LandscapeAssets.description(this,index+1));
        frame.setForeground(buttonBackground(0x00000000));
        if(compact)frame.setOnClickListener(v->{month=period;setAnnual(false);});
        else {
            frame.setContentDescription("Ampliar paisaje de "+period.format(DateTimeFormatter.ofPattern("MMMM yyyy",locale))+". "+LandscapeAssets.description(this,index+1));
            frame.setOnClickListener(v->openLandscapes(period));
        }
        return frame;
    }

    private LinearLayout calendar(YearMonth ym,List<Holidays.Holiday> holidays,boolean compact) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(1);
        LinearLayout week=new LinearLayout(this);
        for(String s:new String[]{"L","M","M","J","V","S","D"}) {
            TextView t=text(s,compact?11:13,s.equals("D")?RED:GREEN); t.setPadding(0,dp(4),0,dp(4)); t.setGravity(Gravity.CENTER);
            week.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        } box.addView(week);
        int offset=ym.atDay(1).getDayOfWeek().getValue()-1;
        int rows=compact?6:(offset+ym.lengthOfMonth()+6)/7;
        for(int row=0;row<rows;row++) {
            LinearLayout line=new LinearLayout(this);
            for(int col=0;col<7;col++) {
                int day=row*7+col-offset+1;
                TextView cell=text("",compact?13:18,INK); cell.setPadding(0,0,0,0); cell.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(Math.round((compact?28:48)*readingScale)),1);
                cp.setMargins(dp(1),dp(2),dp(1),dp(2)); line.addView(cell,cp);
                if(day<1||day>ym.lengthOfMonth()) continue;
                LocalDate d=ym.atDay(day); List<Holidays.Holiday> matches=new ArrayList<>();
                for(Holidays.Holiday h:holidays) if(h.date.equals(d)) matches.add(h);
                boolean holiday=!matches.isEmpty();
                boolean sunday=d.getDayOfWeek()==DayOfWeek.SUNDAY;
                cell.setText(String.valueOf(day));
                if(sunday) cell.setTextColor(RED);
                GradientDrawable shape=bg(holiday?HOLIDAY_BG:Color.TRANSPARENT);
                if(holiday) { cell.setTextColor(RED); bold(cell); }
                if(d.equals(today)) { shape.setStroke(dp(2),GREEN); if(!holiday&&!sunday) cell.setTextColor(GREEN); bold(cell); }
                if(d.equals(selectedDate)&&!d.equals(today)) shape.setStroke(dp(2),dark?0xffffcf66:0xff946000);
                cell.setBackground(shape);
                boolean hasNote=!notes().getString(d.toString(),"").isEmpty();
                if(hasNote) cell.setPaintFlags(cell.getPaintFlags()|Paint.UNDERLINE_TEXT_FLAG);
                StringBuilder detail=new StringBuilder();
                for(Holidays.Holiday h:matches) {
                    detail.append(h.name);
                    if(!h.date.equals(h.original)) detail.append("\nFecha original: ").append(date(h.original)).append(". Descanso trasladado al lunes.");
                    detail.append("\n\n");
                }
                cell.setContentDescription(date(d)+(holiday?", festivo: "+detail:"")+(d.equals(today)?", hoy":"")+(hasNote?", tiene una nota":"")+(d.equals(selectedDate)?", fecha elegida":""));
                // Miniaturas sin objetivos táctiles pequeños: su botón abre el mes completo.
                if(!compact) cell.setOnClickListener(v->openNote(d,null));
            } box.addView(line);
        } return box;
    }
    private void renderYear(List<Holidays.Holiday> holidays) {
        int columns=getResources().getConfiguration().screenWidthDp>=360
            &&getResources().getConfiguration().fontScale*readingScale<=1.3f?2:1;
        for(int start=1;start<=12;start+=columns) {
            LinearLayout row=new LinearLayout(this);
            for(int c=0;c<columns&&start+c<=12;c++) {
                YearMonth ym=YearMonth.of(month.getYear(),start+c);
                LinearLayout card=new LinearLayout(this); card.setOrientation(1); card.setPadding(dp(4),dp(4),dp(4),dp(4));
                card.setBackground(bg(SURFACE));
                if(landscapes)card.addView(monthLandscape(ym,true));
                else card.addView(button(ym.format(DateTimeFormatter.ofPattern("MMMM",locale)),()->{month=ym;setAnnual(false);}));
                card.addView(calendar(ym,holidays,true));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1); lp.setMargins(dp(3),dp(5),dp(3),dp(5));
                row.addView(card,lp);
            } body.addView(row);
        }
        renderHolidayList(holidays,true);
    }
    private void renderHolidayList(List<Holidays.Holiday> holidays,boolean year) {
        TextView heading=text(year?"Festivos de "+month.getYear():"Festivos del mes",21,INK); bold(heading); body.addView(heading);
        int count=0,lastMonth=0;
        for(Holidays.Holiday h:holidays) if(year||YearMonth.from(h.date).equals(month)) {
            count++;
            if(year&&lastMonth!=h.date.getMonthValue()) {
                lastMonth=h.date.getMonthValue();
                TextView group=text(h.date.format(DateTimeFormatter.ofPattern("MMMM",locale)),17,GREEN);bold(group);body.addView(group);
            }
            TextView t=text(h.date.format(DateTimeFormatter.ofPattern("dd MMM",locale))+" · "+h.name+"\n"+date(h.date)
                +(h.date.equals(h.original)?"":" · Trasladado"),15,RED);
            t.setBackground(bg(SURFACE));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(4),0,dp(4)); body.addView(t,lp);
        }
        if(count==0) body.addView(text("Un mes sin festivos nacionales.",15,GREEN));
    }
    private SharedPreferences notes() { return getSharedPreferences("date_notes",MODE_PRIVATE); }
    private void openNote(LocalDate day,String draft) {
        String saved=notes().getString(day.toString(),"");
        LinearLayout content=new LinearLayout(this); content.setOrientation(1); content.setPadding(dp(16),0,dp(16),0);
        StringBuilder detail=new StringBuilder();
        for(Holidays.Holiday h:Holidays.year(day.getYear())) if(h.date.equals(day)) {
            detail.append(h.name);
            if(!h.date.equals(h.original)) detail.append("\nFecha original: ").append(date(h.original)).append(". Descanso trasladado al lunes.");
            detail.append("\n\n");
        }
        content.addView(text(detail.length()>0?detail.toString().trim():"No es festivo nacional.",16,detail.length()>0?RED:INK));
        EditText input=new EditText(this); input.setHint("Escribe tu nota para este día…");
        input.setTextColor(INK); input.setHintTextColor(GREEN); input.setMinLines(4); input.setMaxLines(8);
        input.setTextSize(17*readingScale); input.setPadding(dp(8),dp(12),dp(8),dp(12)); input.setLineSpacing(dp(3),1f);
        input.setGravity(Gravity.TOP); input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4000)}); input.setText(draft==null?saved:draft);
        content.addView(input,new LinearLayout.LayoutParams(-1,-2));
        Button share=new Button(this); share.setText("Compartir esta fecha");
        share.setOnClickListener(v->chooseDayShare(day,input.getText().toString()));
        content.addView(share,new LinearLayout.LayoutParams(-1,-2));
        TextView noteCount=text("0 / 4000 caracteres · Solo en este teléfono",12,GREEN);
        content.addView(noteCount);
        android.text.TextWatcher watcher=new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence v,int start,int count,int after) { }
            public void onTextChanged(CharSequence v,int start,int before,int count) {noteCount.setText(v.length()+" / 4000 caracteres · Solo en este teléfono");}
            public void afterTextChanged(android.text.Editable value) { }
        };
        input.addTextChangedListener(watcher); noteCount.setText(input.length()+" / 4000 caracteres · Solo en este teléfono");
        ScrollView scroll=new ScrollView(this); scroll.addView(content);
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle(date(day)+" de "+day.getYear()).setView(scroll)
            .setPositiveButton("Guardar",(dialog,which)->{
                String value=input.getText().toString().trim();
                if(value.isEmpty()) deleteNote(day);
                else notes().edit().putString(day.toString(),value).apply();
                render(); Toast.makeText(this,value.isEmpty()?"Sin nota guardada":"Nota guardada",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancelar",null);
        if(!saved.isEmpty()) builder.setNeutralButton("Eliminar",(dialog,which)->new AlertDialog.Builder(this)
            .setTitle("¿Eliminar la nota?").setMessage(date(day))
            .setPositiveButton("Eliminar",(d,w)->{deleteNote(day);render();Toast.makeText(this,"Nota eliminada. Puedes recuperarla desde Herramientas.",Toast.LENGTH_LONG).show();})
            .setNegativeButton("Cancelar",null).show());
        AlertDialog dialog=builder.create(); editingDate=day; noteEditor=input;
        dialog.setOnDismissListener(d->{if(noteEditor==input){editingDate=null;noteEditor=null;}});
        dialog.show();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    private void showNotes() {
        final YearMonth period=month;
        Map<String,String> saved=snapshotNotes();
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);panel.setPadding(dp(16),0,dp(16),0);
        Spinner scope=new Spinner(this);
        List<String> scopes=Arrays.asList("Todas las fechas","Año "+period.getYear(),period.format(DateTimeFormatter.ofPattern("MMMM yyyy",locale)));
        ArrayAdapter<String> scopesAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,scopes) {
            @Override public android.view.View getView(int p,android.view.View v,android.view.ViewGroup parent) {
                TextView text=(TextView)super.getView(p,v,parent);text.setTextColor(INK);text.setTextSize(16*readingScale);return text;
            }
        };
        scopesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);scope.setAdapter(scopesAdapter);
        scope.setContentDescription("Filtrar notas por período");panel.addView(scope,new LinearLayout.LayoutParams(-1,dp(48)));
        EditText query=new EditText(this);query.setSingleLine(true);query.setTextColor(INK);query.setHintTextColor(GREEN);
        query.setTextSize(17*readingScale);query.setHint("Buscar en mis notas");query.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});panel.addView(query);
        TextView status=text("Buscando notas…",13,GREEN);panel.addView(status);
        List<String> labels=new ArrayList<>();List<LocalDate> days=new ArrayList<>();
        ArrayAdapter<String> adapter=resultAdapter(labels);ListView list=new ListView(this);list.setAdapter(adapter);
        list.setDivider(new ColorDrawable(PAGE));list.setDividerHeight(dp(6));
        panel.addView(list,new LinearLayout.LayoutParams(-1,dp(Math.max(140,Math.min(320,getResources().getConfiguration().screenHeightDp-350)))));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Mis notas").setView(panel)
            .setPositiveButton("Nueva nota",(d,w)->chooseDate()).setNegativeButton("Cerrar",null).create();
        int[] generation={0};android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
        Runnable update=()->{
            if(isDestroyed()||isFinishing()||!dialog.isShowing())return;
            int request=++generation[0];int selected=scope.getSelectedItemPosition();String value=query.getText().toString();
            days.clear();labels.clear();adapter.notifyDataSetChanged();status.setText("Buscando notas…");
            fileWorker.execute(()->{
                List<LocalDate> found=NoteList.filter(saved,period,Math.max(0,selected),value);
                runOnUiThread(()->{
                    if(isDestroyed()||!dialog.isShowing()||request!=generation[0])return;
                    for(int i=0;i<Math.min(200,found.size());i++) {
                        LocalDate day=found.get(i);String note=saved.get(day.toString()).replace('\n',' ');
                        days.add(day);labels.add(day.format(DateTimeFormatter.ofPattern("EEEE dd MMM yyyy",locale))+"\n"+(note.length()>180?note.substring(0,180)+"…":note));
                    }
                    adapter.notifyDataSetChanged();
                    status.setText(found.isEmpty()?saved.isEmpty()?"No tienes notas. Pulsa Nueva nota para crear una.":"No hay notas que coincidan con el filtro.":found.size()>200?"Mostrando 200 de "+found.size()+" notas. Afina la búsqueda.":found.size()+" notas · Toca una para abrirla.");
                });
            });
        };
        Runnable request=()->{generation[0]++;days.clear();labels.clear();adapter.notifyDataSetChanged();handler.removeCallbacks(update);handler.postDelayed(update,180);};
        query.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            public void onTextChanged(CharSequence s,int start,int before,int count){request.run();}
            public void afterTextChanged(android.text.Editable value) { }
        });
        scope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent,android.view.View v,int position,long id){request.run();}
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        list.setOnItemClickListener((parent,v,index,id)->{LocalDate day=days.get(index);dialog.dismiss();goToDate(day);});
        dialog.setOnDismissListener(d->{generation[0]++;handler.removeCallbacks(update);});
        dialog.show();dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        request.run();
    }

    private RippleDrawable buttonBackground(int color) {
        return new RippleDrawable(ColorStateList.valueOf(dark?0x30ffffff:0x30176b56),bg(color),null);
    }
    private void showTools() {
        String[] labels={"Buscar festivos y notas","Ir a una fecha","Mis notas","Recuperar última nota eliminada","Respaldo y exportación",dark?"Activar modo claro":"Activar modo oscuro","Tamaño de texto",getPreferences(MODE_PRIVATE).getBoolean("remember_period",true)?"Recordar último período: sí":"Recordar último período: no","Estado del icono diario","Actualizar icono del día","Galería de paisajes",landscapes?"Ocultar fotos del calendario":"Mostrar fotos del calendario","Personalizar imagen de un mes","Guía de uso","Acerca de Calendario Colombia"};
        new AlertDialog.Builder(this).setTitle("Herramientas").setItems(labels,(dialog,index)->{
            switch(index) {
                case 0:showSearch();break;
                case 1:chooseDate();break;
                case 2:showNotes();break;
                case 3:undoLastDeletion();break;
                case 4:backupMenu();break;
                case 5:getSharedPreferences("appearance",MODE_PRIVATE).edit().putBoolean("dark",!dark).apply();recreate();break;
                case 6:chooseReadingSize();break;
                case 7:
                    boolean remember=!getPreferences(MODE_PRIVATE).getBoolean("remember_period",true);
                    getPreferences(MODE_PRIVATE).edit().putBoolean("remember_period",remember).apply();render();
                    Toast.makeText(this,remember?"Se recordará el último período consultado":"Al abrir se mostrará el período actual",Toast.LENGTH_SHORT).show();break;
                case 8:message("Estado del icono diario",DayIconUpdater.status(this));break;
                case 9:
                    boolean refreshed=DayIconUpdater.refresh(this);
                    message("Icono del día",refreshed?"Se solicitó mostrar el día "+DayIconClock.day(java.time.Instant.now())+" de Colombia. Android puede tardar en refrescar el icono.":"No se pudo actualizar el icono. Inténtalo otra vez.");break;
                case 10:openLandscapes(month);break;
                case 11:getSharedPreferences("appearance",MODE_PRIVATE).edit().putBoolean("show_landscapes",!landscapes).apply();render();break;
                case 12:choosePhotoMonth();break;
                case 13:showUserGuide();break;
                case 14:message("Calendario Colombia · 16.0 · Edición final","Calendario de festivos nacionales, notas y paisajes del campo. Paisajes incluidos generados para esta aplicación; también puedes usar tus propias fotos.\n\nEl icono muestra el día de Colombia. Consulta su estado o solicita la actualización desde Herramientas.\n\nPuedes recuperar la última nota eliminada y exportar tus notas como texto legible.");break;
                default:break;
            }
        }).setNegativeButton("Cerrar",null).show();
    }
    private void chooseDayShare(LocalDate day,String draft) {
        if(draft.trim().isEmpty()) { shareDay(day,"",false);return; }
        new AlertDialog.Builder(this).setTitle("Compartir fecha")
            .setMessage("Puedes compartir solo la fecha y sus festivos, o incluir el texto actual de tu nota. Compartir no guarda los cambios de la nota.")
            .setPositiveButton("Solo fecha",(d,w)->shareDay(day,"",false))
            .setNeutralButton("Incluir mi nota",(d,w)->shareDay(day,draft,true))
            .setNegativeButton("Cancelar",null).show();
    }
    private void shareDay(LocalDate day,String note,boolean include) {
        Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT,DayShare.text(day,note,include));
        try { startActivity(Intent.createChooser(send,"Compartir fecha")); }
        catch(android.content.ActivityNotFoundException ex) { message("Compartir","No hay una aplicación disponible para compartir texto."); }
    }
    private void showUserGuide() {
        String[] topics={"Consultar el calendario","Notas y compartir","Fotos de cada mes","Respaldo y actualización","Icono del día"};
        String[] help={
            "Cambia entre vista mensual y anual con los controles del calendario. En la vista anual, abre el mes para consultar sus días. Usa Hoy para volver a la fecha actual, o Ir a una fecha y Buscar en Herramientas.\n\nDomingos y festivos aparecen en rojo. El borde verde marca hoy; el subrayado indica una nota. No se muestra una cuenta regresiva al próximo festivo.",
            "Toca un día en la vista mensual para escribir una nota. Guardar conserva el texto; Cancelar descarta la edición. Puedes filtrar tus notas desde Mis notas y recuperar la última eliminada.\n\nCompartir esta fecha abre el selector de Android. Si hay texto, eliges Solo fecha o Incluir mi nota. Compartir no guarda la edición ni envía nada automáticamente.",
            "Abre Personalizar imagen de un mes, elige el mes y selecciona una foto. La app guarda una copia; el original no se modifica. La misma imagen aparece para ese mes en todos los años. Puedes restaurar el paisaje incluido, ocultar las fotos o abrir la galería.",
            "Exporta un respaldo .fco antes de cambiar de APK. Ese archivo permite restaurar notas; el TXT sirve para leerlas. Ninguno incluye fotos ni ajustes. Conserva tus fotos originales.\n\nUna actualización conserva los datos si la firma coincide. Si Android exige desinstalar, respalda primero: desinstalar borra los datos locales. Las claves debug de compilaciones distintas pueden variar.",
            "El icono utiliza el día actual de Colombia. Abre la app después de instalar. Android puede retrasar el cambio o conservar el icono anterior en caché. Consulta Estado del icono diario o Actualizar icono del día en Herramientas."
        };
        new AlertDialog.Builder(this).setTitle("Guía de Calendario Colombia")
            .setItems(topics,(d,i)->message(topics[i],help[i])).setNegativeButton("Cerrar",null).show();
    }
    private void choosePhotoMonth() {
        String[] months=new String[12];
        for(int i=0;i<12;i++)months[i]=YearMonth.of(month.getYear(),i+1).format(DateTimeFormatter.ofPattern("MMMM",locale))+(PersonalLandscapes.has(this,i+1)?" · Foto propia":"");
        new AlertDialog.Builder(this).setTitle("Imagen de cada mes").setItems(months,(dialog,index)->{
            int selected=index+1;
            new AlertDialog.Builder(this).setTitle(months[index]).setItems(new String[]{"Elegir una foto del teléfono","Usar paisaje original"},(d,choice)->{
                if(choice==0) {
                    pendingPhotoMonth=selected;
                    try {startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*"),PICK_MONTH_PHOTO);}
                    catch(ActivityNotFoundException error){pendingPhotoMonth=0;message("No se pudo abrir Fotos","Tu teléfono no tiene un selector de imágenes disponible.");}
                } else if(PersonalLandscapes.has(this,selected)) {
                    new AlertDialog.Builder(this).setTitle("¿Volver al paisaje original?").setMessage("Se quitará la copia de la foto personalizada de este mes. La foto de tu galería no se modifica.")
                        .setPositiveButton("Restaurar",(a,b)->{if(PersonalLandscapes.restore(this,selected)){render();Toast.makeText(this,"Paisaje original restaurado",Toast.LENGTH_SHORT).show();}else message("No se pudo restaurar","Inténtalo otra vez.");})
                        .setNegativeButton("Cancelar",null).show();
                }else message("Paisaje original","Este mes ya usa la imagen original.");
            }).setNegativeButton("Cerrar",null).show();
        }).setNegativeButton("Cerrar",null).show();
    }
    private void importMonthPhoto(Uri uri,int target) {
        startWorking("Preparando imagen del mes…");
        fileWorker.execute(()->{
            String error=null;
            try{PersonalLandscapes.importPhoto(this,uri,target);}
            catch(IOException|RuntimeException failure){error="No se pudo guardar la imagen. Elige una foto compatible de hasta 20 MB. Se conserva el paisaje anterior.";}
            catch(OutOfMemoryError failure){error="No hay memoria suficiente para esa foto. Elige una de menor tamaño. Se conserva el paisaje anterior.";}
            String failure=error;
            runOnUiThread(()->{if(isDestroyed())return;stopWorking();render();message(failure==null?"Foto guardada":"No se pudo cambiar la foto",failure==null?"La imagen se mostrará en "+YearMonth.of(month.getYear(),target).format(DateTimeFormatter.ofPattern("MMMM",locale))+" de todos los años. También está disponible en la galería de paisajes.":failure);});
        });
    }

    private void openLandscapes(YearMonth period) {
        Intent intent=new Intent(this,LandscapeActivity.class).putExtra("month",period.getMonthValue()).putExtra("year",period.getYear());
        startActivityForResult(intent,VIEW_LANDSCAPE);
    }
    private void deleteNote(LocalDate day) {
        String previous=notes().getString(day.toString(),"");
        if(!previous.isEmpty())getSharedPreferences("note_undo",MODE_PRIVATE).edit().putString("date",day.toString()).putString("text",previous).apply();
        notes().edit().remove(day.toString()).apply();
    }
    private void undoLastDeletion() {
        SharedPreferences undo=getSharedPreferences("note_undo",MODE_PRIVATE);
        String key=undo.getString("date",null),value=undo.getString("text","");
        if(key==null||value.isEmpty()){message("Recuperar nota","No hay una nota eliminada para recuperar.");return;}
        LocalDate day;
        try{day=LocalDate.parse(key);}catch(java.time.DateTimeException error){message("No se pudo recuperar","La fecha de recuperación no es válida.");return;}
        String current=notes().getString(key,"");
        if(current.equals(value)){undo.edit().clear().apply();message("Nota disponible","La nota ya está guardada en esa fecha.");return;}
        String preview=value.length()>300?value.substring(0,300)+"…":value;
        new AlertDialog.Builder(this).setTitle("Recuperar · "+date(day)+" de "+day.getYear()).setMessage(preview+(current.isEmpty()?"":"\n\nYa hay una nota distinta en esta fecha. Reemplazarla requiere tu confirmación."))
            .setPositiveButton(current.isEmpty()?"Recuperar":"Reemplazar",(d,w)->{
                notes().edit().putString(key,value).apply();undo.edit().clear().apply();
                selectedDate=day;month=YearMonth.from(day);setAnnual(false);
                Toast.makeText(this,"Nota recuperada",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancelar",null).show();
    }

    private void chooseReadingSize() {
        int current=getSharedPreferences("appearance",MODE_PRIVATE).getInt("reading_size",0);
        new AlertDialog.Builder(this).setTitle("Tamaño de texto").setSingleChoiceItems(new String[]{"Estándar","Grande","Extra grande"},current,(dialog,choice)->{
            getSharedPreferences("appearance",MODE_PRIVATE).edit().putInt("reading_size",choice).apply();
            dialog.dismiss(); recreate();
        }).setNegativeButton("Cancelar",null).show();
    }
    private ArrayAdapter<String> resultAdapter(List<String> labels) {
        return new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2,android.R.id.text1,labels) {
            @Override public android.view.View getView(int position,android.view.View convert,android.view.ViewGroup parent) {
                android.view.View row=super.getView(position,convert,parent);
                TextView dateText=row.findViewById(android.R.id.text1), preview=row.findViewById(android.R.id.text2);
                String value=getItem(position); if(value==null)value="";
                int split=value.indexOf('\n');
                dateText.setText(split>=0?value.substring(0,split):value);
                preview.setText(split>=0?value.substring(split+1):"");
                dateText.setTextSize(16*readingScale); dateText.setTextColor(GREEN); dateText.setTypeface(null,Typeface.BOLD);
                dateText.setSingleLine(false); dateText.setMaxLines(2);
                preview.setTextSize(16*readingScale); preview.setTextColor(INK); preview.setSingleLine(false);
                preview.setMaxLines(3); preview.setEllipsize(TextUtils.TruncateAt.END); preview.setLineSpacing(dp(2),1f);
                row.setPadding(dp(12),dp(10),dp(12),dp(10)); row.setMinimumHeight(dp(76));
                row.setBackgroundColor(SURFACE); return row;
            }
        };
    }
    private final class CalendarScroll extends ScrollView {
        private float startX,startY; private long startTime;
        private boolean captured,vertical,multiTouch;
        CalendarScroll() {super(MainActivity.this);}
        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            int action=event.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN) {
                startX=event.getX();startY=event.getY();startTime=event.getEventTime();captured=false;vertical=false;multiTouch=false;
            } else if(event.getPointerCount()>1||action==MotionEvent.ACTION_POINTER_DOWN) multiTouch=true;
            float dx=event.getX()-startX,dy=event.getY()-startY;
            if(action==MotionEvent.ACTION_MOVE&&!captured&&!multiTouch) {
                if(Math.abs(dy)>dp(12)&&Math.abs(dy)>Math.abs(dx))vertical=true;
                if(!vertical&&CalendarNavigation.swipeDirection(dx,dy,event.getEventTime()-startTime,getResources().getDisplayMetrics().density)!=0) {
                    MotionEvent cancel=MotionEvent.obtain(event);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();captured=true;
                }
            }
            if(captured) {
                if(action==MotionEvent.ACTION_UP) {
                    int direction=multiTouch?0:CalendarNavigation.swipeDirection(dx,dy,event.getEventTime()-startTime,getResources().getDisplayMetrics().density);
                    captured=false;if(direction!=0)step(direction);
                } else if(action==MotionEvent.ACTION_CANCEL)captured=false;
                return true;
            }
            return super.dispatchTouchEvent(event);
        }
    }

    private Map<String,String> snapshotNotes() {
        Map<String,String> result=new TreeMap<>();
        for(Map.Entry<String,?> entry:notes().getAll().entrySet())
            if(entry.getValue() instanceof String&&!((String)entry.getValue()).trim().isEmpty()) result.put(entry.getKey(),(String)entry.getValue());
        return result;
    }
    private void message(String title,String value) {
        if(!isFinishing()&&!isDestroyed()) new AlertDialog.Builder(this).setTitle(title).setMessage(value).setPositiveButton("Aceptar",null).show();
    }
    private void goToDate(LocalDate day) {
        selectedDate=day; month=YearMonth.from(day); setAnnual(false); openNote(day,null);
    }
    private void chooseDate() {
        LocalDate start=selectedDate!=null&&YearMonth.from(selectedDate).equals(month)?selectedDate:
            YearMonth.from(today).equals(month)?today:month.atDay(1);
        DatePickerDialog dialog=new DatePickerDialog(this,(picker,y,m,d)->goToDate(LocalDate.of(y,m+1,d)),start.getYear(),start.getMonthValue()-1,start.getDayOfMonth());
        dialog.getDatePicker().setMinDate(LocalDate.of(2000,1,1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        dialog.getDatePicker().setMaxDate(LocalDate.of(2100,12,31).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        dialog.setTitle("Ir a una fecha"); dialog.show();
    }
    private void showSearch() {
        final int[] searchYear={month.getYear()};
        Map<String,String> savedNotes=snapshotNotes();
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(1); panel.setPadding(dp(16),0,dp(16),0);
        panel.addView(text("Elige el año de los festivos. Se incluyen todas tus notas. Admite fechas dd/MM/aaaa.",13,GREEN));
        Button yearButton=button("Año de festivos: "+searchYear[0],()->{}); panel.addView(yearButton);
        EditText query=new EditText(this); query.setSingleLine(true); query.setHint("Nombre, palabra o fecha"); query.setTextSize(17*readingScale);
        query.setTextColor(INK); query.setHintTextColor(GREEN); query.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
        query.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH); panel.addView(query);
        TextView status=text("Escribe y pulsa Buscar.",13,GREEN); panel.addView(status);
        List<CalendarSearch.Result> matches=new ArrayList<>(); List<String> labels=new ArrayList<>();
        ArrayAdapter<String> adapter=resultAdapter(labels);
        ListView list=new ListView(this); list.setAdapter(adapter); list.setDivider(new ColorDrawable(PAGE)); list.setDividerHeight(dp(6));
        Button search=button("Buscar",()->{}); panel.addView(search);
        panel.addView(list,new LinearLayout.LayoutParams(-1,dp(Math.max(140,Math.min(300,getResources().getConfiguration().screenHeightDp-430)))));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Buscar").setView(panel).setNegativeButton("Cerrar",null).create();
        int[] generation={0};
        Runnable runSearch=()->{
            String value=query.getText().toString(); int request=++generation[0]; int year=searchYear[0];
            if(value.trim().isEmpty()) { matches.clear(); labels.clear(); adapter.notifyDataSetChanged(); status.setText("Escribe una palabra o una fecha."); return; }
            matches.clear(); labels.clear(); adapter.notifyDataSetChanged();
            status.setText("Buscando…");
            android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if(keyboard!=null) keyboard.hideSoftInputFromWindow(query.getWindowToken(),0);
            fileWorker.execute(()->{
                List<CalendarSearch.Result> found=CalendarSearch.find(value,year,savedNotes);
                runOnUiThread(()->{
                    if(isDestroyed()||!dialog.isShowing()||request!=generation[0]) return;
                    matches.clear(); labels.clear();
                    for(int i=0;i<Math.min(200,found.size());i++) { matches.add(found.get(i)); labels.add(found.get(i).label); }
                    adapter.notifyDataSetChanged();
                    status.setText(found.isEmpty()?"Sin coincidencias.":found.size()>200?"Mostrando 200 de "+found.size()+" resultados. Afina la búsqueda.":found.size()+" resultados. Toca uno para abrir la fecha.");
                });
            });
        };
        yearButton.setOnClickListener(v->{
            NumberPicker picker=new NumberPicker(this); picker.setMinValue(2000); picker.setMaxValue(2100);picker.setValue(searchYear[0]);
            new AlertDialog.Builder(this).setTitle("Año de búsqueda").setView(picker).setPositiveButton("Elegir",(d,w)->{
                searchYear[0]=picker.getValue(); yearButton.setText("Año de festivos: "+searchYear[0]);
                runSearch.run();
            }).setNegativeButton("Cancelar",null).show();
        });
        search.setOnClickListener(v->runSearch.run());
        query.setOnEditorActionListener((v,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH){runSearch.run();return true;}return false;});
        list.setOnItemClickListener((parent,v,index,id)->{LocalDate day=matches.get(index).date;dialog.dismiss();goToDate(day);});
        dialog.show(); dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    private void backupMenu() {
        new AlertDialog.Builder(this).setTitle("Respaldo de notas").setItems(new String[]{"Exportar respaldo .fco","Importar un respaldo","Exportar notas como texto .txt"},(dialog,which)->{
            try {
                if(which==0) {
                    if(snapshotNotes().isEmpty()){message("Sin notas","Todavía no tienes notas para respaldar.");return;}
                    Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/octet-stream");
                    intent.putExtra(Intent.EXTRA_TITLE,"Calendario-Colombia-notas-"+today+".fco"); startActivityForResult(intent,EXPORT_NOTES);
                } else if(which==2) {
                    if(snapshotNotes().isEmpty()){message("Sin notas","Todavía no tienes notas para exportar.");return;}
                    Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TITLE,"Calendario-Colombia-notas-"+today+".txt");startActivityForResult(intent,EXPORT_TEXT);
                } else {
                    Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
                    startActivityForResult(intent,IMPORT_NOTES);
                }
            } catch(ActivityNotFoundException ex) { message("No se pudo abrir Archivos","Tu teléfono no tiene un selector de documentos disponible."); }
        }).setNegativeButton("Cerrar",null).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==PICK_MONTH_PHOTO) {
            int target=pendingPhotoMonth;pendingPhotoMonth=0;
            if(result==RESULT_OK&&data!=null&&data.getData()!=null&&target>=1&&target<=12)importMonthPhoto(data.getData(),target);
            return;
        }
        if(request==VIEW_LANDSCAPE) {
            if(result==RESULT_OK&&data!=null) {
                int y=data.getIntExtra("year",month.getYear()),m=data.getIntExtra("month",month.getMonthValue());
                if(y>=2000&&y<=2100&&m>=1&&m<=12){month=YearMonth.of(y,m);selectedDate=null;setAnnual(false);}
            }
            return;
        }
        if((request!=EXPORT_NOTES&&request!=IMPORT_NOTES&&request!=EXPORT_TEXT)||result!=RESULT_OK||data==null||data.getData()==null) return;
        Uri uri=data.getData();
        if(request==EXPORT_NOTES||request==EXPORT_TEXT) exportNotes(uri,request==EXPORT_TEXT); else importNotes(uri);
    }
    private void startWorking(String text) {
        workingDialog=new AlertDialog.Builder(this).setMessage(text).setCancelable(false).create(); workingDialog.show();
    }
    private void stopWorking() { if(workingDialog!=null){workingDialog.dismiss();workingDialog=null;} }
    private void exportNotes(Uri uri,boolean plainText) {
        Map<String,String> saved=snapshotNotes(); startWorking(plainText?"Exportando texto…":"Guardando respaldo…");
        fileWorker.execute(()->{
            String error=null;
            try {
                byte[] bytes=plainText?NotesText.encode(saved,java.time.Instant.now()):NoteBackup.encode(saved);
                try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")) {
                    if(out==null) throw new IOException("No se pudo abrir el archivo."); out.write(bytes); out.flush();
                }
            } catch(Exception ex) { error="No se pudo guardar el respaldo. Comprueba el espacio y el acceso al archivo e inténtalo otra vez."; }
            String failure=error;
            runOnUiThread(()->{if(isDestroyed())return;stopWorking();message(failure==null?plainText?"Texto exportado":"Respaldo guardado":"Error al exportar",failure==null?saved.size()+" notas guardadas."+(plainText?" El TXT sirve para leer; usa .fco para restaurar notas.":""):failure);});
        });
    }
    private void importNotes(Uri uri) {
        startWorking("Leyendo respaldo…");
        fileWorker.execute(()->{
            Map<String,String> incoming=null; String error=null;
            try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                if(in==null) throw new IOException("No se pudo abrir el archivo.");
                byte[] buffer=new byte[8192]; int n;
                while((n=in.read(buffer))!=-1) { if(out.size()+n>NoteBackup.MAX_BYTES) throw new IllegalArgumentException("El archivo supera 16 MB.");out.write(buffer,0,n); }
                incoming=NoteBackup.decode(out.toByteArray());
            } catch(IllegalArgumentException ex) { error="El archivo no es un respaldo válido de Calendario Colombia o está dañado. No se modificaron tus notas."; }
            catch(Exception ex) { error="No se pudo leer el archivo. Comprueba el acceso e inténtalo otra vez."; }
            Map<String,String> loaded=incoming; String failure=error;
            runOnUiThread(()->{if(isDestroyed())return;stopWorking();if(failure!=null)message("No se pudo importar",failure);else confirmImport(loaded);});
        });
    }
    private void confirmImport(Map<String,String> incoming) {
        if(incoming.isEmpty()){message("Respaldo vacío","El archivo no contiene notas. No se modificó ninguna fecha.");return;}
        Map<String,String> existing=snapshotNotes(); int fresh=0, conflicts=0, identical=0;
        for(Map.Entry<String,String> entry:incoming.entrySet()) {
            if(!existing.containsKey(entry.getKey()))fresh++;
            else if(existing.get(entry.getKey()).equals(entry.getValue()))identical++;
            else conflicts++;
        }
        if(fresh==0&&conflicts==0){message("Notas ya guardadas","Todas las notas de este respaldo ya están en el teléfono.");return;}
        String summary=incoming.size()+" notas en el respaldo.\n"+fresh+" nuevas · "+conflicts+" con texto diferente · "+identical+" iguales.\n\nLas notas de otras fechas se conservan.";
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("Importar notas").setMessage(summary)
            .setNegativeButton("Cancelar",null);
        if(conflicts==0) builder.setPositiveButton("Importar",(d,w)->applyImport(incoming,false));
        else {
            builder.setPositiveButton("Conservar actuales",(d,w)->applyImport(incoming,false));
            builder.setNeutralButton("Usar respaldo",(d,w)->new AlertDialog.Builder(this).setTitle("¿Reemplazar coincidencias?")
                .setMessage("Se reemplazará el texto de las fechas con notas distintas usando el del respaldo. Las demás notas se conservan.")
                .setPositiveButton("Reemplazar",(a,b)->applyImport(incoming,true)).setNegativeButton("Cancelar",null).show());
        }
        builder.show();
    }
    private void applyImport(Map<String,String> incoming,boolean replaceConflicts) {
        Map<String,String> existing=snapshotNotes();
        Map<String,String> merged=NoteBackup.merge(existing,incoming,replaceConflicts);
        startWorking("Importando notas…");
        fileWorker.execute(()->{
            SharedPreferences.Editor editor=notes().edit();
            int changed=0;
            for(Map.Entry<String,String> entry:merged.entrySet()) if(!Objects.equals(existing.get(entry.getKey()),entry.getValue())) {editor.putString(entry.getKey(),entry.getValue());changed++;}
            boolean success=editor.commit(); int count=changed;
            runOnUiThread(()->{if(isDestroyed())return;stopWorking();render();message(success?"Importación terminada":"No se pudo confirmar el guardado",success?count+" notas añadidas o actualizadas.":"El teléfono no confirmó el guardado. Conserva el respaldo e inténtalo otra vez.");});
        });
    }

    private void chooseYear() {
        NumberPicker picker=new NumberPicker(this); picker.setMinValue(2000); picker.setMaxValue(2100); picker.setValue(month.getYear());
        new AlertDialog.Builder(this).setTitle("Seleccionar año").setView(picker)
            .setPositiveButton("Ver",(d,w)->{month=YearMonth.of(picker.getValue(),month.getMonthValue());render();})
            .setNegativeButton("Cancelar",null).show();
    }
}
