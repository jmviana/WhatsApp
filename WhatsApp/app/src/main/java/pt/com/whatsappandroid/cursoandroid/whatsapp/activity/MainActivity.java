package pt.com.whatsappandroid.cursoandroid.whatsapp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.support.v7.widget.*;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.adapter.TabAdapter;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Base64Custom;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.SlidingTabLayout;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Contacto;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Usuario;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FirebaseAuth userAuth;

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private String identificadorContacto;
    private DatabaseReference firebaseRef;

    private StorageReference storageRef;
    private Uri imageUri;
    private String imageLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        userAuth = ConfiguracaoFirebase.getFirebaseAuth();

        toolbar = findViewById( R.id.toolbar );
        toolbar.setTitle( "WhatsApp" );
        setSupportActionBar( toolbar );

        slidingTabLayout = findViewById( R.id.stl_tabs );
        viewPager = findViewById( R.id.vp_pagina );

        //Configurar sliding tabs
        slidingTabLayout.setDistributeEvenly( true );
        slidingTabLayout.setSelectedIndicatorColors( ContextCompat.getColor( this, R.color.colorAccent ) );

        //Configurar página
        TabAdapter tabAdapter = new TabAdapter( getSupportFragmentManager() );
        viewPager.setAdapter( tabAdapter );

        slidingTabLayout.setViewPager( viewPager );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch( item.getItemId() ){
            case R.id.item_sair:
                deslogarUsuario();
                return true;
            case R.id.item_configuracoes:
                return true;
            case R.id.item_adicionar:
                abrirCadastroContacto();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }

    }

    private void abrirCadastroContacto(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder( MainActivity.this );

        //Configurações do Dialog
        alertDialog.setTitle( "Novo contacto" );
        alertDialog.setMessage( "E-mail do usuário" );
        alertDialog.setCancelable( false );

        final EditText editText = new EditText( MainActivity.this );
        alertDialog.setView( editText );

        //Configura botões
        alertDialog.setPositiveButton( "Cadastrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String emailContacto = editText.getText().toString();

                //Valida se o email foi digitado
                if(emailContacto.isEmpty()){
                    Toast.makeText( MainActivity.this, "Preencha o email", Toast.LENGTH_LONG ).show();
                }else{

                    //Verificar se o usuário já está cadastrado no app
                    identificadorContacto = Base64Custom.codificarBase64( emailContacto );

                    //Recuperar instância do Firebase
                    firebaseRef = ConfiguracaoFirebase.getFirebase().child( "usuarios" ).child( identificadorContacto );

                    firebaseRef.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue() != null){

                                //Recuperar dados do contacto a ser adicionado
                                Usuario usuarioContacto = dataSnapshot.getValue(Usuario.class);

                                //Recuperar identificador usuario logado (base64)
                                Preferencias preferencias = new Preferencias( MainActivity.this );
                                String identificadorUsuarioLogado = preferencias.getIdentificador();

                                firebaseRef = ConfiguracaoFirebase.getFirebase();
                                firebaseRef = firebaseRef.child("contactos").child( identificadorUsuarioLogado ).child( identificadorContacto );

                                final Contacto contacto = new Contacto();
                                contacto.setIdentificadorUsuario( identificadorContacto );
                                contacto.setEmail( usuarioContacto.getEmail() );
                                contacto.setNome( usuarioContacto.getNome() );
                                contacto.setImageLocation( usuarioContacto.getImageLocation() );

                                storageRef = FirebaseStorage.getInstance().getReference()
                                        .child( "usuarios" )
                                        .child( contacto.getImageLocation() );
                                //.child( "link_arrow.png" );

                                imageLocation = contacto.getImageLocation();

                                File file = new File(getFilesDir(), imageLocation);
                                try {
                                    file.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                storageRef.getFile(file).addOnCompleteListener( new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {

                                    }
                                }).addOnFailureListener( new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                } );

                                firebaseRef.setValue( contacto );

                            }else{
                                Toast.makeText( MainActivity.this, "Usuário não possui cadastro", Toast.LENGTH_LONG ).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );

                }

            }
        } );

        alertDialog.setNegativeButton( "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        } );

        alertDialog.create();
        alertDialog.show();

    }

    private void deslogarUsuario(){
        userAuth.signOut();
        Intent intent = new Intent( MainActivity.this, LoginActivity.class );
        startActivity( intent );
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
