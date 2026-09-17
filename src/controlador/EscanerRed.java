package controlador;

import modelo.Device;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EscanerRed {

    public static Device escanearIp(String ip, int timeoutMs) {
        long inicio = System.currentTimeMillis();
        boolean alcanzable = false;
        long tiempoRespuesta = -1;

        try {
            // Comando ping adaptado a Windows (-n 1) con timeout (-w)
            ProcessBuilder pb = new ProcessBuilder("ping", "-n", "1", "-w", String.valueOf(timeoutMs), ip);
            Process proceso = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.toLowerCase();
                // Detección de respuesta exitosa en español/inglés
                if (linea.contains("bytes=") || linea.contains("tiempo=") || linea.contains("time=")) {
                    alcanzable = true;
                    tiempoRespuesta = System.currentTimeMillis() - inicio;
                    break;
                }
            }
            proceso.waitFor();
        } catch (Exception e) {
            alcanzable = false;
        }

        if (!alcanzable) {
            return new Device(ip, "Desconocido", false, -1);
        }

        // Obtener hostname usando nslookup del sistema
        String hostname = obtenerHostnameSystem(ip);
        return new Device(ip, hostname, true, tiempoRespuesta);
    }

    private static String obtenerHostnameSystem(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("nslookup", ip);
            Process proceso = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.toLowerCase().contains("name:") || linea.toLowerCase().contains("nombre:")) {
                    String[] partes = linea.split(":");
                    if (partes.length > 1) {
                        return partes[1].trim();
                    }
                }
            }
        } catch (Exception ignored) {
            // Error en nslookup, cae a fallback
        }
        return "No resuelto";
    }
}