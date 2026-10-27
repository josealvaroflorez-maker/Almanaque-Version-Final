package co.calendario.festivos;
public final class LandscapeAssets {
    private static final int[] PHOTOS={R.drawable.field_01,R.drawable.field_02,R.drawable.field_03,R.drawable.field_04,R.drawable.field_05,R.drawable.field_06,R.drawable.field_07,R.drawable.field_08,R.drawable.field_09,R.drawable.field_10,R.drawable.field_11,R.drawable.field_12};
    private static final int[] THUMBS={R.drawable.field_01_thumb,R.drawable.field_02_thumb,R.drawable.field_03_thumb,R.drawable.field_04_thumb,R.drawable.field_05_thumb,R.drawable.field_06_thumb,R.drawable.field_07_thumb,R.drawable.field_08_thumb,R.drawable.field_09_thumb,R.drawable.field_10_thumb,R.drawable.field_11_thumb,R.drawable.field_12_thumb};
    private static final String[] DESCRIPTIONS={"Cafetales al amanecer","Praderas entre colinas","Cultivos de los Andes","El valle después de la lluvia","Palmas entre la niebla","Arrozales y montañas","Girasoles del campo","Laderas de caña panelera","Finca tropical","Río y llanuras","Caminos de montaña","Atardecer en la finca"};
    private static int index(int month){if(month<1||month>12)throw new IllegalArgumentException("Mes inválido");return month-1;}
    public static int photo(int month){return PHOTOS[index(month)];}
    public static int thumb(int month){return THUMBS[index(month)];}
    public static String description(android.content.Context context,int month){return PersonalLandscapes.has(context,month)?"Tu imagen del mes":description(month);}
    public static String description(int month){return DESCRIPTIONS[index(month)];}
}
