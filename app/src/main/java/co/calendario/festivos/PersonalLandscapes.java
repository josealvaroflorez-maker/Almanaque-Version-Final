package co.calendario.festivos;
import android.content.Context;
import android.net.Uri;
import android.graphics.*;
import android.media.ExifInterface;
import android.widget.ImageView;
import java.io.*;
import java.nio.file.*;
import java.util.Locale;

public final class PersonalLandscapes {
    private static final int MAX_BYTES=20*1024*1024;
    private static File file(Context context,int month) {
        if(month<1||month>12)throw new IllegalArgumentException("Mes inválido");
        return new File(context.getFilesDir(),String.format(Locale.ROOT,"landscape_month_%02d.jpg",month));
    }
    public static boolean has(Context context,int month){File photo=file(context,month);return photo.isFile()&&photo.length()>0;}
    public static boolean show(Context context,ImageView image,int month,boolean compact) {
        File custom=file(context,month);
        if(custom.isFile()) {
            BitmapFactory.Options info=new BitmapFactory.Options();info.inJustDecodeBounds=true;BitmapFactory.decodeFile(custom.getPath(),info);
            int sample=1;while(Math.max(info.outWidth,info.outHeight)/sample>(compact?400:1600))sample*=2;
            BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=sample;
            Bitmap bitmap=BitmapFactory.decodeFile(custom.getPath(),options);
            if(bitmap!=null){image.setImageBitmap(bitmap);return true;}
        }
        image.setImageResource(compact?LandscapeAssets.thumb(month):LandscapeAssets.photo(month));return false;
    }
    public static boolean restore(Context context,int month){File photo=file(context,month);return !photo.exists()||photo.delete();}
    public static void importPhoto(Context context,Uri uri,int month) throws IOException {
        File destination=file(context,month);
        File source=File.createTempFile("landscape_",".source",context.getCacheDir());
        File output=File.createTempFile("landscape_",".jpg",context.getFilesDir());Bitmap bitmap=null;
        try {
            try(InputStream in=context.getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(source)) {
                if(in==null)throw new IOException("No se pudo abrir la foto");
                byte[] buffer=new byte[8192];int n,total=0;
                while((n=in.read(buffer))!=-1){total+=n;if(total>MAX_BYTES)throw new IOException("La foto supera 20 MB");out.write(buffer,0,n);}
            }
            BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(source.getPath(),bounds);
            if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Formato de imagen no admitido");
            BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;
            while(Math.max(bounds.outWidth,bounds.outHeight)/options.inSampleSize>1600)options.inSampleSize*=2;
            bitmap=BitmapFactory.decodeFile(source.getPath(),options);if(bitmap==null)throw new IOException("No se pudo leer la imagen");
            Matrix transform=new Matrix();
            try {
                int orientation=new ExifInterface(source.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                switch(orientation) {
                    case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:transform.setScale(-1,1);break;
                    case ExifInterface.ORIENTATION_ROTATE_180:transform.setRotate(180);break;
                    case ExifInterface.ORIENTATION_FLIP_VERTICAL:transform.setScale(1,-1);break;
                    case ExifInterface.ORIENTATION_TRANSPOSE:transform.setRotate(90);transform.postScale(-1,1);break;
                    case ExifInterface.ORIENTATION_ROTATE_90:transform.setRotate(90);break;
                    case ExifInterface.ORIENTATION_TRANSVERSE:transform.setRotate(270);transform.postScale(-1,1);break;
                    case ExifInterface.ORIENTATION_ROTATE_270:transform.setRotate(270);break;
                    default:break;
                }
            }catch(IOException ignored){/* Algunas imágenes no contienen EXIF. */}
            if(!transform.isIdentity()){
                Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),transform,true);
                if(rotated!=bitmap){bitmap.recycle();bitmap=rotated;}
            }
            try(OutputStream out=new FileOutputStream(output)){if(!bitmap.compress(Bitmap.CompressFormat.JPEG,88,out))throw new IOException("No se pudo guardar la foto");}
            try {Files.move(output.toPath(),destination.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){Files.move(output.toPath(),destination.toPath(),StandardCopyOption.REPLACE_EXISTING);}
        } finally {
            if(bitmap!=null)bitmap.recycle();source.delete();output.delete();
        }
    }
}
