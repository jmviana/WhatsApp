package pt.com.whatsappandroid.cursoandroid.whatsapp.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Contacto;

public class ContactoAdapter extends ArrayAdapter<Contacto> {

    private ArrayList<Contacto> contactos;
    private Context context;

    public ContactoAdapter(Context c, ArrayList<Contacto> objects) {
        super( c, 0, objects );
        this.contactos = objects;
        this.context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        //Verifica se a lista está vazia
        if(contactos != null){

            //Inicializar objeto para montagem da view
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( context.LAYOUT_INFLATER_SERVICE );

            //Montar view a partir do xml
            view = inflater.inflate( R.layout.lista_contacto, parent, false );

            //Recuperar elemento para exibição
            TextView nomeContacto = view.findViewById( R.id.tv_nome );
            TextView emailContacto = view.findViewById( R.id.tv_email );
            ImageView imagem = view.findViewById( R.id.usuarioFoto );

            Contacto contacto = contactos.get( position );
            nomeContacto.setText( contacto.getNome() );
            emailContacto.setText( contacto.getEmail() );

            //Picasso.with( context ).load(imageUri).fit().into( imagem );
            String image_name = contacto.getImageLocation();
            imagem.setImageBitmap( getImageFromInternalStorage( image_name ) );

        }

        return view;
    }

    public Bitmap getImageFromInternalStorage(String imageName){

        Bitmap image = null;
        try {
            FileInputStream fis = context.openFileInput( imageName );
            image = BitmapFactory.decodeStream( fis );

        }catch (Exception e) {
            Log.e("getInInternalStorage()", e.getMessage());
        }

        return image;
    }

}
