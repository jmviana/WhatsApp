package pt.com.whatsappandroid.cursoandroid.whatsapp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

public class Preferencias {

    private Context context;
    private SharedPreferences preferences;
    private static final String NOME_ARQUIVO = "whatsapp.preferencias";
    private int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String CHAVE_IDENTIFICADOR = "identificadorUsuarioLogado";
    private final String CHAVE_NOME = "nomeUsuarioLogado";
    private final String CHAVE_IMAGEM = "imagemUsuarioLogado";

    public Preferencias(Context contextParameter){
        context = contextParameter;
        preferences = context.getSharedPreferences( NOME_ARQUIVO, MODE );
        editor = preferences.edit();
    }

    public void salvarDados(String identificadorUsuario, String nomeUsuario, String imagemUsuario){
        //MediaPlayer player = MediaPlayer.create(context,Settings.System.DEFAULT_RINGTONE_URI);
        //player.setLooping( true );
        //player.start();
        //Log.i("username", "antes salvar: "+identificadorUsuario );
        editor.putString( CHAVE_IDENTIFICADOR, identificadorUsuario ).toString();
        editor.putString( CHAVE_NOME, nomeUsuario );
        editor.putString( CHAVE_IMAGEM, imagemUsuario );
        editor.commit();
        //Log.i("username", "depois salvar: "+identificadorUsuario + "  " + CHAVE_IDENTIFICADOR );
    }

    public String getIdentificador(){ return preferences.getString( CHAVE_IDENTIFICADOR, null ); }

    public String getNome(){ return preferences.getString( CHAVE_NOME, null ); }

    public String getImagem() { return preferences.getString( CHAVE_IMAGEM, null ); }

}
