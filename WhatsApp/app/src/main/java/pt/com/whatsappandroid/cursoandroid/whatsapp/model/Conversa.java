package pt.com.whatsappandroid.cursoandroid.whatsapp.model;

public class Conversa {

    private String idUsuario;
    private String nome;
    private String mensagem;
    private String imageLocation;

    public Conversa() { }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getImageLocation() { return imageLocation; }

    public void setImageLocation(String imageLocation) { this.imageLocation = imageLocation; }
}
