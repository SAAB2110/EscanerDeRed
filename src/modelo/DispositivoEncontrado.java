package modelo;

public class DispositivoEncontrado {

    private String ip;
    private String host;
    private boolean responde;
    private long tiempoMs;

    public DispositivoEncontrado(String ip, String host, boolean responde, long tiempoMs) {
        this.ip = ip;
        this.host = host;
        this.responde = responde;
        this.tiempoMs = tiempoMs;
    }

    public String getIp() {
        return ip;
    }

    public String getHost() {
        return host;
    }

    public boolean isResponde() {
        return responde;
    }

    public long getTiempoMs() {
        return tiempoMs;
    }

    public String getTiempoFormateado() {
        return tiempoMs + " ms";
    }
}