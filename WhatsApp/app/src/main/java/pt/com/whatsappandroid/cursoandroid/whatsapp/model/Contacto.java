package pt.com.whatsappandroid.cursoandroid.whatsapp.model;

public class Contacto {

    private String identificadorUsuario;
    private String nome;
    private String email;
    private String imageLocation;

    public Contacto(){ }

    public String getIdentificadorUsuario() { return identificadorUsuario; }

    public void setIdentificadorUsuario(String identificadorUsuario) { this.identificadorUsuario = identificadorUsuario; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getImageLocation() { return imageLocation; }

    public void setImageLocation(String imageLocation) { this.imageLocation = imageLocation; }
}
