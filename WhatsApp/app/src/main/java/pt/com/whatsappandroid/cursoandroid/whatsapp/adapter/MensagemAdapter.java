package pt.com.whatsappandroid.cursoandroid.whatsapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.InternalStorage;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Mensagem;

import static android.graphics.Bitmap.createScaledBitmap;

public class MensagemAdapter extends ArrayAdapter<Pair<Mensagem, String>> {

    private Context context;
    private ArrayList<Pair<Mensagem, String>> mensagens;
    private DatabaseReference firebaseRef;

    public MensagemAdapter(Context c, ArrayList<Pair<Mensagem, String>> objects) {
        super( c, 0, objects );
        this.context = c;
        this.mensagens = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        //Verifica se a lista está preenchida
        if (mensagens != null){

            //Recupera dados do usuário remetente
            Preferencias preferencias = new Preferencias( context );
            String idUsuarioRemetente = preferencias.getIdentificador();

            //Inicializa objeto para montagem do layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( context.LAYOUT_INFLATER_SERVICE );

            //Recupera mensagem e imagemNome
            Mensagem mensagem = mensagens.get( position ).first;
            String imagemNome = mensagens.get( position ).second;

            //Monta view a partir do xml
            if (idUsuarioRemetente.equals( mensagem.getIdUsuario() )){
                view = inflater.inflate( R.layout.item_mensagem_direita, parent, false );
                TextView textoEstadoMensagem = view.findViewById( R.id.tv_mensagem_estado );
                String estadoMensagem = mensagem.isLida() ? "Visualizada" : "Entregue";
                textoEstadoMensagem.setText( estadoMensagem );
            }else {
                view = inflater.inflate( R.layout.item_mensagem_esquerda, parent, false );
            }

            //Recupera elementos para exibição
            TextView textoMensagem = view.findViewById( R.id.tv_mensagem );
            TextView textoHora = view.findViewById( R.id.tv_mensagem_hora );
            TextView textoData = view.findViewById( R.id.tv_date );
            textoMensagem.setText( mensagem.getMensagem() );
            textoHora.setText( mensagem.getTime() );

            if (position == mensagens.size()-1) {
                ImageView imageView = view.findViewById( R.id.user_image );
                Bitmap imagem = new InternalStorage( context ).getImageFromInternalStorage( imagemNome );
                Bitmap bitmap = createScaledBitmap( imagem, 100, 80, true );
                imageView.setImageBitmap( bitmap );
            }
            else{
                if (!mensagem.getIdUsuario().equals( mensagens.get( position+1 ).first.getIdUsuario() )){
                    ImageView imageView = view.findViewById( R.id.user_image );
                    Log.i("imagemNome", "context - "+context.toString());
                    Bitmap imagem = new InternalStorage( context ).getImageFromInternalStorage( imagemNome );
                    Bitmap bitmap = createScaledBitmap( imagem, 100, 80, true );
                    imageView.setImageBitmap( bitmap );
                }
            }

            String data = mensagem.getDate().toLowerCase();
            if (data.equals(new SimpleDateFormat("EEE, dd MMM yyyy").format( Calendar.getInstance().getTime() ).toLowerCase()))
                data = "Hoje";

            if(position == 0){
                textoData.setText( data );
            }else{
                if(!mensagem.getDate().toLowerCase().equals(mensagens.get( position-1 ).first.getDate().toLowerCase())){
                    textoData.setText( data );
                }
            }

        }

        return view;
    }

}
