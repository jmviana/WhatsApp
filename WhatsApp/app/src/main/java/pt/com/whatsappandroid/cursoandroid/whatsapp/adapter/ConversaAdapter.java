package pt.com.whatsappandroid.cursoandroid.whatsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.*;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.InternalStorage;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Contacto;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Conversa;

public class ConversaAdapter extends ArrayAdapter<Conversa> {

    private ArrayList<Conversa> conversas;
    private Context context;

    public ConversaAdapter(Context c, ArrayList<Conversa> objects) {
        super( c, 0, objects );
        this.conversas = objects;
        this.context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        //Verifica se a lista está vazia
        if(conversas != null){

            //Inicializar objeto para montagem da view
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( context.LAYOUT_INFLATER_SERVICE );

            //Montar view a partir do xml
            view = inflater.inflate( R.layout.lista_conversas, parent, false );

            //Recuperar elemento para exibição
            TextView nome = view.findViewById( R.id.tv_titulo );
            TextView ultimaMensagem = view.findViewById( R.id.tv_subtitulo );
            ImageView imagem = view.findViewById( R.id.usuarioFoto );

            Conversa conversa = conversas.get( position );
            nome.setText( conversa.getNome() );

            String image_name = conversa.getImageLocation();
            imagem.setImageBitmap( new InternalStorage( context ).getImageFromInternalStorage( image_name ) );

            String mensagem = conversa.getMensagem();
            if (mensagem.length() > 30) {
                mensagem = mensagem.substring( 0, 27 );
                mensagem += "...";
            }
            if (mensagem.contains( "\n" )){
                String[] result = mensagem.split( "\n", 2 );
                mensagem = result[0] + "...";
            }
            if (mensagem.contains( "\r" )){
                String[] result = mensagem.split( "\r", 2 );
                mensagem = result[0] + "...";
            }
            ultimaMensagem.setText( mensagem );

        }

        return view;
    }

}
