package modelo;

public class Device {
    private String ip;
    private String hostname;
    private boolean active;
    private long responseTime; // en ms

    public Device(String ip, String hostname, boolean active, long responseTime) {
        this.ip = ip;
        this.hostname = hostname;
        this.active = active;
        this.responseTime = responseTime;
    }

    // Getters y Setters
    public String getIp() { return ip; }
    public String getHostname() { return hostname; }
    public boolean isActive() { return active; }
    public long getResponseTime() { return responseTime; }

    public void setHostname(String hostname) { this.hostname = hostname; }
    public void setActive(boolean active) { this.active = active; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
}