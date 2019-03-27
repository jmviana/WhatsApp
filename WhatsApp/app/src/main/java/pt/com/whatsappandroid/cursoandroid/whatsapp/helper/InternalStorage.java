package pt.com.whatsappandroid.cursoandroid.whatsapp.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileInputStream;

public class InternalStorage {

    private Context context;
    private String imageName;

    public InternalStorage(Context contextParameter){
        context = contextParameter;
    }

    public Bitmap getImageFromInternalStorage(String imageName){

        Bitmap image = null;
        Log.i("useername", "image: "+ image);
        try {
            FileInputStream fis = context.openFileInput( imageName );
            image = BitmapFactory.decodeStream( fis );

        }catch (Exception e) {
            Log.e("getInInternalStorage()", e.getMessage());
        }

        return image;
    }

}
