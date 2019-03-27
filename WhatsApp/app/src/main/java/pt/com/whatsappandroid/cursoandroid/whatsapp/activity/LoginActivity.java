package pt.com.whatsappandroid.cursoandroid.whatsapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Base64Custom;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Usuario;


public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText senha;
    private Button botaoLogar;
    private Usuario usuario;
    private FirebaseAuth auth;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListener;
    private String identificadorUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        verificarUsuarioLogado();

        email = findViewById( R.id.edit_login_email );
        senha = findViewById( R.id.edit_login_senha );
        botaoLogar = findViewById( R.id.bt_logar );

        botaoLogar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usuario = new Usuario();
                usuario.setEmail( email.getText().toString() );
                usuario.setSenha( senha.getText().toString() );
                validarLogin();

            }
        } );

    }

    private void verificarUsuarioLogado(){
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        if(auth.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }

    private void validarLogin(){
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.signInWithEmailAndPassword( usuario.getEmail(), usuario.getSenha() )
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            identificadorUsuarioLogado = Base64Custom.codificarBase64(usuario.getEmail());

                            firebaseRef = ConfiguracaoFirebase.getFirebase().child( "usuarios" ).child( identificadorUsuarioLogado );

                            valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Usuario usuarioRecuperado = dataSnapshot.getValue( Usuario.class );

                                    Preferencias preferencias = new Preferencias( LoginActivity.this );
                                    preferencias.salvarDados( identificadorUsuarioLogado, usuarioRecuperado.getNome(), usuarioRecuperado.getImageLocation() );

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            firebaseRef.addListenerForSingleValueEvent( valueEventListener );

                            abrirTelaPrincipal();
                            Toast.makeText( LoginActivity.this, "Sucesso ao fazer login!", Toast.LENGTH_LONG ).show();
                        }else{
                            String exceptionError = "";
                            try{
                                throw task.getException();
                            }catch(FirebaseAuthInvalidUserException e){
                                exceptionError = "Esse email não existe no App!";
                            }catch(FirebaseAuthInvalidCredentialsException e){
                                exceptionError = "A password está errada!";
                            } catch (Exception e) {
                                exceptionError = "Ao efetuar o login!";
                                e.printStackTrace();
                            }
                            Toast.makeText( LoginActivity.this, "Erro: " + exceptionError, Toast.LENGTH_LONG ).show();
                        }
                    }
                } );
    }

    private void abrirTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity( intent );
        finish();
    }

    public void abrirCadastroUsuario(View view){
        Intent intent = new Intent( LoginActivity.this, CadastroUsuarioActivity.class );
        startActivity( intent );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent( LoginActivity.this, NotificacaoService.class );
        startService( intent );
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startForegroundService( intent );
        //}
    }
}
