package modelo;

public class Device {
    private String ip;
    private String hostname;
    private boolean active;
    private long responseTimeMs;

    public Device(String ip, String hostname, boolean active, long responseTimeMs) {
        this.ip = ip;
        this.hostname = hostname;
        this.active = active;
        this.responseTimeMs = responseTimeMs;
    }

    public String getIp() { 
        return ip; 
    }

    public String getHostname() { 
        return hostname; 
    }

    public boolean isActive() { 
        return active; 
    }

    public long getResponseTime() { 
        return responseTimeMs; 
    }

    public long getResponseTimeMs() { 
        return responseTimeMs; 
    }

    public String getResponseTimeFormatted() {
        return active ? responseTimeMs + " ms" : "N/A";
    }
}