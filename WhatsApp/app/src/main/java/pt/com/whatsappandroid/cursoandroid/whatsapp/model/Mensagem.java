package pt.com.whatsappandroid.cursoandroid.whatsapp.model;

public class Mensagem {

    private String idMensagem;
    private String idUsuario;
    private String nomeUsuario;
    private String mensagem;
    private String date;
    private String time;
    private boolean lida;
    private String imageLocation;

    public Mensagem(){ }

    public String getIdUsuario() { return idUsuario; }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() { return nomeUsuario; }

    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getIdMensagem() { return idMensagem; }

    public void setIdMensagem(String idMensagem) { this.idMensagem = idMensagem; }

    public boolean isLida() { return lida; }

    public void setLida(boolean lida) { this.lida = lida; }

    public String getImageLocation() { return imageLocation; }

    public void setImageLocation(String imageLocation) { this.imageLocation = imageLocation; }
}
