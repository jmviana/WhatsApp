package pt.com.whatsappandroid.cursoandroid.whatsapp.activity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.leolin.shortcutbadger.ShortcutBadger;
import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Base64Custom;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Mensagem;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Usuario;

import static android.content.Intent.getIntent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificacaoService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "pt.com.whatsappandroid.cursoandroid.whatsapp.activity.action.FOO";
    private static final String ACTION_BAZ = "pt.com.whatsappandroid.cursoandroid.whatsapp.activity.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "pt.com.whatsappandroid.cursoandroid.whatsapp.activity.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "pt.com.whatsappandroid.cursoandroid.whatsapp.activity.extra.PARAM2";

    private DatabaseReference firebaseRefForDestUser;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListenerMensagem;
    private String idUsuarioRemetente;
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;
    private ArrayList<Mensagem> mensagemLista;
    //private MediaPlayer player;


    public NotificacaoService() {
        super( "NotificacaoService" );
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //player = MediaPlayer.create(this,Settings.System.DEFAULT_RINGTONE_URI);
        //player.setLooping(true);
        //player.start();

        //bindService(intent, m_serviceConnection, BIND_AUTO_CREATE);

        Preferencias preferencias = new Preferencias( NotificacaoService.this );
        idUsuarioRemetente = preferencias.getIdentificador();

        firebaseRef = ConfiguracaoFirebase.getFirebase().child( "mensagens" ).child( idUsuarioRemetente );
        mensagemLista = new ArrayList<>();

        //createNotificationChannel();

        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mensagemLista.clear();
                    collectMessages( (Map<String, Map<String, Mensagem>>) dataSnapshot.getValue() );
                    Log.i( "username", "collectMessages " + mensagemLista );

                    int mensagemNovas = 0;
                    String ultimaMensagem = "";
                    Log.i( "username", String.valueOf( mensagemLista.size() ) );

                    for (Mensagem m : mensagemLista) {
                        if (!m.isLida()) {
                            mensagemNovas++;
                            ultimaMensagem = m.getMensagem();
                            idUsuarioDestinatario = m.getIdUsuario();
                            nomeUsuarioDestinatario = m.getNomeUsuario();

                            Log.i( "username", "out " + nomeUsuarioDestinatario );
                        }
                    }

                    if (mensagemNovas > 0 && nomeUsuarioDestinatario != null) {
                        showNotification( mensagemNovas, ultimaMensagem );
                    }
                    else{
                        ShortcutBadger.removeCount(NotificacaoService.this); //for 1.1.4+
                        //ShortcutBadger.with(getApplicationContext()).remove();  //for 1.1.3
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        firebaseRef.addValueEventListener( valueEventListenerMensagem );



    }

    @Override
    public void onDestroy() {
        //player.stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void showNotification(int novasMensagens, String ultimaMensagem){

        if (novasMensagens == 1) {
            Intent intent = new Intent( this, ConversaActivity.class );
            intent.putExtra( "nome", nomeUsuarioDestinatario );
            intent.putExtra( "email", Base64Custom.decodificarBase64( idUsuarioDestinatario ) );
            Log.i( "username", "one "+nomeUsuarioDestinatario );
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( NotificacaoService.this, null )
                    .setSmallIcon( R.drawable.ic_logo )
                    .setContentTitle( nomeUsuarioDestinatario )
                    .setContentText( ultimaMensagem )
                    .setDefaults( ~Notification.DEFAULT_SOUND )
                    .setAutoCancel( true )
                    .setSound( Uri.parse( ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +getApplicationContext().getPackageName()+ "/" + R.raw.notification) )
                    .setPriority( NotificationCompat.PRIORITY_DEFAULT )
                    .setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from( NotificacaoService.this );
            notificationManager.notify( 0, mBuilder.build() );

        }else{
            Intent intent = new Intent( this, ConversaActivity.class );
            intent.putExtra( "nome", nomeUsuarioDestinatario );
            intent.putExtra( "email", Base64Custom.decodificarBase64( idUsuarioDestinatario ) );
            Log.i( "username", "many "+nomeUsuarioDestinatario );
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( NotificacaoService.this, null )
                    .setSmallIcon( R.drawable.ic_logo )
                    .setContentTitle( nomeUsuarioDestinatario )
                    .setContentText( ultimaMensagem )
                    .setNumber( novasMensagens )
                    .setDefaults( ~Notification.DEFAULT_SOUND )
                    .setAutoCancel( true )
                    .setSound( Uri.parse( ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +getApplicationContext().getPackageName()+ "/" + R.raw.notification) )
                    .setPriority( NotificationCompat.PRIORITY_DEFAULT )
                    .setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from( NotificacaoService.this );
            notificationManager.notify( 0, mBuilder.build() );
        }

        int badgeCount = novasMensagens;
        ShortcutBadger.applyCount(NotificacaoService.this, badgeCount); //for 1.1.4+
        //ShortcutBadger.with(getApplicationContext()).count(badgeCount); //for 1.1.3

    }

    private void collectMessages(Map<String, Map<String, Mensagem>> mensagens){
        try {
            for (Map.Entry<String, Map<String, Mensagem>> entry : mensagens.entrySet()) {
                Map<String, Mensagem> mensagensUnicoUser = (Map) entry.getValue();
                for (Map.Entry<String, Mensagem> e : mensagensUnicoUser.entrySet()) {
                    Map unicaMensagem = (Map) e.getValue();

                    if (!unicaMensagem.get( "idUsuario" ).toString().equals(idUsuarioRemetente)) {
                        Mensagem m = new Mensagem();
                        m.setIdMensagem( unicaMensagem.get( "idMensagem" ).toString() );
                        m.setIdUsuario( unicaMensagem.get( "idUsuario" ).toString() );
                        m.setNomeUsuario( unicaMensagem.get( "nomeUsuario" ).toString() );
                        m.setLida( (Boolean) unicaMensagem.get( "lida" ) );
                        m.setMensagem( unicaMensagem.get( "mensagem" ).toString() );
                        m.setTime( unicaMensagem.get( "time" ).toString() );
                        m.setDate( unicaMensagem.get( "date" ).toString() );
                        mensagemLista.add( m );
                    }
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public class MyBinder extends Binder {
        public NotificacaoService getService() {
            return NotificacaoService.this;
        }
    }

    /*private ServiceConnection m_serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            m_service = ((NotificacaoService.MyBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            m_service = null;
        }
    };*/

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent( context, NotificacaoService.class );
        intent.setAction( ACTION_FOO );
        intent.putExtra( EXTRA_PARAM1, param1 );
        intent.putExtra( EXTRA_PARAM2, param2 );
        context.startService( intent );
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent( context, NotificacaoService.class );
        intent.setAction( ACTION_BAZ );
        intent.putExtra( EXTRA_PARAM1, param1 );
        intent.putExtra( EXTRA_PARAM2, param2 );
        context.startService( intent );
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
