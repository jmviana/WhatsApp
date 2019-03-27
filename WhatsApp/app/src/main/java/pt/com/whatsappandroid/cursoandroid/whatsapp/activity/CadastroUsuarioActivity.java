package pt.com.whatsappandroid.cursoandroid.whatsapp.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Base64Custom;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Usuario;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private ImageView imagem_adicionar_usuario;
    private EditText nome;
    private EditText email;
    private EditText senha;
    private Button botaoCadastrar;
    private Button botaoAdicionarFoto;
    private ImageView imagem_plus;
    private ImageView imagem_cross;
    private StorageReference imagePath;
    private String imageLocation;
    private Uri imageUri;
    private Usuario usuario;
    private StorageReference storageRef;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_cadastro_usuario );

        nome                        = findViewById( R.id.edit_cadastro_nome );
        email                       = findViewById( R.id.edit_cadastro_email );
        senha                       = findViewById( R.id.edit_cadastro_senha );
        botaoCadastrar              = findViewById( R.id.bt_cadastrar );
        botaoAdicionarFoto          = findViewById( R.id.bt_adicionar_foto );
        imagem_adicionar_usuario    = findViewById( R.id.imageView_adicionar_usuario );
        imagem_plus                 = findViewById( R.id.imageView_plus );
        imagem_cross                = findViewById( R.id.imageView_cross );

        imagem_cross.setVisibility( View.GONE );

        botaoCadastrar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuario = new Usuario();
                usuario.setNome( nome.getText().toString() );
                usuario.setEmail( email.getText().toString() );
                usuario.setSenha( senha.getText().toString() );

                if (imageUri == null){
                    imagePath = FirebaseStorage.getInstance().getReference().child( "usuarios" ).child( "user_icon.png" );
                    imageLocation = "user_icon.png";
                }else{
                    imagePath = FirebaseStorage.getInstance().getReference().child( "usuarios" ).child( imageUri.getLastPathSegment() + ".png" );
                    imageLocation = imageUri.getLastPathSegment() + ".png";
                    imagePath.putFile( imageUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText( CadastroUsuarioActivity.this, "Salvo com sucesso...", Toast.LENGTH_LONG ).show();
                        }
                    } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText( CadastroUsuarioActivity.this, "Falha ao salvar...", Toast.LENGTH_LONG ).show();
                        }
                    } );
                }

                storageRef = FirebaseStorage.getInstance().getReference()
                        .child( "usuarios" )
                        .child( imageLocation );

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


                usuario.setImageLocation( imageLocation );
                cadastrarUsuario();
            }
        } );

        botaoAdicionarFoto.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 0);
            }
        } );

        imagem_plus.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 0);
            }
        } );

        imagem_cross.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagem_adicionar_usuario.setImageResource( R.drawable.usuario );
                imagem_cross.setVisibility( View.GONE );
                imageUri = null;
            }
        } );

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imagem_adicionar_usuario.setImageBitmap(selectedImage);
                imagem_cross.setVisibility( View.VISIBLE );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(CadastroUsuarioActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(CadastroUsuarioActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void cadastrarUsuario(){

        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.createUserWithEmailAndPassword( usuario.getEmail(), usuario.getSenha() )
                .addOnCompleteListener( CadastroUsuarioActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText( CadastroUsuarioActivity.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_LONG ).show();
                            String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                            usuario.setId(identificadorUsuario);
                            usuario.salvar();

                            Preferencias preferencias = new Preferencias( CadastroUsuarioActivity.this );
                            preferencias.salvarDados( identificadorUsuario, usuario.getNome(), imageLocation );

                            abrirLoginUsuario();

                        }else{

                            String exceptionError = "";

                            try{
                                throw task.getException();
                            }catch(FirebaseAuthWeakPasswordException e) {
                                exceptionError = "Digite uma password mais forte, com mais caracteres e com letras e números!";
                            }catch (FirebaseAuthInvalidCredentialsException e) {
                                exceptionError = "O e-mail digitado é inválido, digite um novo e-mail!";
                            }catch (FirebaseAuthUserCollisionException e){
                                exceptionError = "Esse e-mail já está em uso no App!";
                            } catch (Exception e) {
                                exceptionError = "Ao efetuar o cadastro!";
                                e.printStackTrace();
                            }

                            Toast.makeText( CadastroUsuarioActivity.this, "Erro: " + exceptionError, Toast.LENGTH_LONG ).show();
                        }
                    }
                } );

    }

    public void abrirLoginUsuario(){
        Intent intent = new Intent(CadastroUsuarioActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
