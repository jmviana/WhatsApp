package pt.com.whatsappandroid.cursoandroid.whatsapp.model;

import android.graphics.Bitmap;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;

public class Usuario {

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String imageLocation;

    public Usuario(){

    }

    public void salvar(){
        DatabaseReference firebaseReference = ConfiguracaoFirebase.getFirebase();
        firebaseReference.child( "usuarios" ).child( getId() ).setValue( this );
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getImageLocation() { return imageLocation; }

    public void setImageLocation(String imageLocation) { this.imageLocation = imageLocation; }
}
