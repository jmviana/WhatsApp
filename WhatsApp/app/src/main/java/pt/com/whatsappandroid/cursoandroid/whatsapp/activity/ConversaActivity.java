package pt.com.whatsappandroid.cursoandroid.whatsapp.activity;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.adapter.MensagemAdapter;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Base64Custom;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.InternalStorage;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Conversa;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Mensagem;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Usuario;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btMensagem;
    private DatabaseReference firebaseRef;
    private ListView listView;
    private ArrayList<Pair<Mensagem, String>> mensagens;
    private ArrayAdapter<Pair<Mensagem, String>> adapter;
    private ValueEventListener valueEventListenerMensagem;
    private boolean aberto;

    //dados do destinatário
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;

    //dados do remetente
    private String idUsuarioRemetente;
    private String nomeUsuarioRemetente;
    private String imageLocationDest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_conversa );

        toolbar = findViewById( R.id.tb_conversa );
        editMensagem = findViewById( R.id.edit_mensagem );
        btMensagem = findViewById( R.id.bt_enviar );
        listView = findViewById( R.id.lv_conversas );

        //dados do usuário logado
        Preferencias preferencias = new Preferencias( ConversaActivity.this );
        idUsuarioRemetente = preferencias.getIdentificador();
        nomeUsuarioRemetente = preferencias.getNome();

        Bundle extra = getIntent().getExtras();

        if(extra != null){
            nomeUsuarioDestinatario = extra.getString( "nome" );
            String emailDestinatario = extra.getString( "email" );
            imageLocationDest = extra.getString( "imagemDest" );
            idUsuarioDestinatario = Base64Custom.codificarBase64( emailDestinatario );
        }

        //Configurar toolbar
        toolbar.setTitle( nomeUsuarioDestinatario );
        toolbar.setNavigationIcon( R.drawable.ic_action_arrow_left );
        setSupportActionBar( toolbar );

        //Montar listview e adapter
        mensagens = new ArrayList<>();
        //adapter = new ArrayAdapter( ConversaActivity.this, android.R.layout.simple_list_item_1, mensagens );
        adapter = new MensagemAdapter( ConversaActivity.this, mensagens );
        listView.setAdapter( adapter );

        //Recuperar mensagens do firebase
        firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" ).child( idUsuarioRemetente ).child( idUsuarioDestinatario );

        listView.setLongClickable( true );
        listView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removerMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagens.get( position ).first);
                if(position == mensagens.size()-1){
                    String msg = "";
                    if (position != 0){
                        msg = mensagens.get( position - 1 ).first.getMensagem();

                    }
                        //salvar conversa para o remetente
                        Conversa conversa = new Conversa();
                        conversa.setIdUsuario( idUsuarioDestinatario );
                        conversa.setNome( nomeUsuarioDestinatario );
                        conversa.setMensagem( msg );
                        boolean retornoConversaRemetente = salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, conversa );
                        if (!retornoConversaRemetente) {
                            Toast.makeText( ConversaActivity.this, "Problema ao salvar conversa, tente novamente!", Toast.LENGTH_LONG ).show();
                        }

                }
                return true;
            }
        } );

        //Criar listener para mensagens
        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Limpar mensagens
                mensagens.clear();

                //Recupera mensagens
                for (DataSnapshot dados : dataSnapshot.getChildren()){
                    Mensagem mensagem = dados.getValue( Mensagem.class );

                    if( !idUsuarioRemetente.equals(mensagem.getIdUsuario()) && aberto ){
                        if(!mensagem.isLida()) {
                            mensagem.setLida( true );
                            firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" ).child( idUsuarioRemetente ).child( idUsuarioDestinatario ).child( mensagem.getIdMensagem() );
                            firebaseRef.setValue( mensagem );
                            firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" ).child( idUsuarioDestinatario ).child( idUsuarioRemetente ).child( mensagem.getIdMensagem() );
                            firebaseRef.setValue( mensagem );
                        }
                    }

                    if (mensagem.getIdUsuario().equals( idUsuarioRemetente )){
                        //mensagens_imagens.add( new Preferencias( ConversaActivity.this ).getImagem() );
                        Pair<Mensagem, String> pair = new Pair<>(mensagem, new Preferencias( ConversaActivity.this ).getImagem());
                        mensagens.add( pair );
                    }else{
                        //mensagens_imagens.add( imageLocationDest );
                        Pair<Mensagem, String> pair = new Pair<>(mensagem, imageLocationDest);
                        mensagens.add( pair );
                    }
                    //mensagens.add( mensagem );


                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        };
        firebaseRef.addValueEventListener( valueEventListenerMensagem );



        //Enviar mensagem
        btMensagem.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoMensagem = editMensagem.getText().toString();

                if(textoMensagem.isEmpty() || textoMensagem.replaceAll( "\n|\r|\t| ", "" ).isEmpty()){
                    Toast.makeText( ConversaActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_LONG ).show();
                }else{

                    if (textoMensagem.startsWith("\r") || textoMensagem.startsWith("\n") || textoMensagem.startsWith("\t") || textoMensagem.startsWith(" ")) {
                        while (textoMensagem.startsWith( "\r" ) || textoMensagem.startsWith( "\n" ) || textoMensagem.startsWith( "\t" ) || textoMensagem.startsWith( " " ))
                            textoMensagem = textoMensagem.substring( 1, textoMensagem.length() );
                    }
                    if (textoMensagem.endsWith("\r") || textoMensagem.endsWith("\n") || textoMensagem.endsWith("\t") || textoMensagem.endsWith(" ")) {
                        while (textoMensagem.endsWith( "\r" ) || textoMensagem.endsWith( "\n" ) || textoMensagem.endsWith( "\t" ) || textoMensagem.endsWith( " " ))
                            textoMensagem = textoMensagem.substring( 0, textoMensagem.length() - 1 );
                    }

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioRemetente );
                    mensagem.setMensagem( textoMensagem );

                    DateFormat df_date = new SimpleDateFormat("EEE, dd MMM yyyy");
                    String date = df_date.format( Calendar.getInstance().getTime() );
                    mensagem.setDate( date );

                    DateFormat df_time = new SimpleDateFormat( "HH:mm" );
                    String time = df_time.format( Calendar.getInstance().getTime() );
                    mensagem.setTime( time );

                    //salva mensagem para o remetente
                    String retornoMensagemRemetente = salvarMensagem( idUsuarioRemetente, idUsuarioDestinatario, mensagem, null );
                    if(retornoMensagemRemetente == null){
                        Toast.makeText( ConversaActivity.this, "Problema ao salvar mensagem, tente novamente!", Toast.LENGTH_LONG ).show();
                    }else{

                        //salva mensagem para o destinatário
                        String retornoMensagemDestinatario = salvarMensagem( idUsuarioDestinatario, idUsuarioRemetente, mensagem, retornoMensagemRemetente );
                        if(retornoMensagemDestinatario == null){
                            Toast.makeText( ConversaActivity.this, "Problema ao enviar mensagem para o destinatário, tente novamente!", Toast.LENGTH_LONG ).show();
                        }

                    }

                    //salvar conversa para o remetente
                    Conversa conversa = new Conversa();
                    conversa.setIdUsuario( idUsuarioDestinatario );
                    conversa.setNome( nomeUsuarioDestinatario );
                    conversa.setMensagem( textoMensagem );
                    conversa.setImageLocation( imageLocationDest );
                    boolean retornoConversaRemetente = salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, conversa );
                    if(!retornoConversaRemetente){
                        Toast.makeText( ConversaActivity.this, "Problema ao salvar conversa, tente novamente!", Toast.LENGTH_LONG ).show();
                    }else {

                        //salvar conversa para o destinatario
                        conversa = new Conversa();
                        conversa.setIdUsuario( idUsuarioRemetente );
                        conversa.setNome( nomeUsuarioRemetente );
                        conversa.setMensagem( textoMensagem );
                        conversa.setImageLocation( new Preferencias( ConversaActivity.this ).getImagem() );
                        boolean retornoConversaDestinatario = salvarConversa( idUsuarioDestinatario, idUsuarioRemetente, conversa );
                        if(!retornoConversaDestinatario){
                            Toast.makeText( ConversaActivity.this, "Problema ao salvar conversa no destinatário, tente novamente!", Toast.LENGTH_LONG ).show();
                        }

                    }

                    editMensagem.setText( "" );

                }

            }
        } );

    }



    public Bitmap getImageFromInternalStorage(String imageName){

        Bitmap image = null;
        Log.i("useername", "image: "+ image);
        try {
            FileInputStream fis = openFileInput( imageName );
            image = BitmapFactory.decodeStream( fis );

        }catch (Exception e) {
            Log.e("getInInternalStorage()", e.getMessage());
        }

        return image;
    }

    private String salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem, String idMsg){
        try{
            String idMensagem="";
            if(idMsg==null) {
                firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" ).child( idRemetente ).child( idDestinatario ).push();
                idMensagem = firebaseRef.getKey();
                mensagem.setIdMensagem( idMensagem );
                mensagem.setNomeUsuario( nomeUsuarioRemetente );
                firebaseRef.setValue( mensagem );
                //Toast.makeText( ConversaActivity.this, idMensagem, Toast.LENGTH_LONG ).show();
            }
            else{
                firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" ).child( idRemetente ).child( idDestinatario ).child( idMsg );
                idMensagem = idMsg;
                mensagem.setIdMensagem( idMensagem );
                mensagem.setNomeUsuario( nomeUsuarioRemetente );
                firebaseRef.setValue( mensagem );
            }

            return idMensagem;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private boolean salvarConversa(String idRemetente, String idDestinatario, Conversa conversa){
        try{
            firebaseRef = ConfiguracaoFirebase.getFirebase().child( "conversas" );
            firebaseRef.child( idRemetente ).child( idDestinatario ).setValue( conversa );

            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void removerMensagem(final String idUsuarioRemetente, final String idUsuarioDestinatario, final Mensagem mensagem){
        AlertDialog.Builder dialog = new AlertDialog.Builder( ConversaActivity.this );
        dialog.setTitle("Quer remover a mensagem?");
        dialog.setMessage("Esta ação apenas irá remover a mensagem do seu aplicativo, permanecendo visível para a outra pessoa. Deseja continuar?");
        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ConversaActivity.this, "Remoção cancelada!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" )
                        .child( idUsuarioRemetente ).child( idUsuarioDestinatario ).child( mensagem.getIdMensagem() );
                firebaseRef.removeValue();

                Toast.makeText(ConversaActivity.this, "Mensagem removida com sucesso!", Toast.LENGTH_LONG).show();
            }
        });

        dialog.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        aberto = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //firebaseRef.removeEventListener( valueEventListenerMensagem );
        aberto = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent( ConversaActivity.this, NotificacaoService.class );
        intent.putExtra( "email", Base64Custom.decodificarBase64(idUsuarioDestinatario) );
        startService( intent );
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startForegroundService( intent );
        //}
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.common_google_play_services_notification_channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
