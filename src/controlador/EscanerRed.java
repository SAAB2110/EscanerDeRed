package controlador;

import modelo.Device;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class EscanerRed {

    public static Device escanearIp(String ip, int timeoutMs) {
        boolean activo = false;
        long tiempoRespuesta = -1;
        String nombreEquipo = "Desconocido";

        try {
            long inicio = System.currentTimeMillis();
            InetAddress address = InetAddress.getByName(ip);
            
            activo = address.isReachable(timeoutMs);
            long fin = System.currentTimeMillis();

            if (activo) {
                tiempoRespuesta = fin - inicio;
                
                nombreEquipo = address.getCanonicalHostName();
                if (nombreEquipo.equals(ip)) {
                    nombreEquipo = resolverNslookup(ip);
                }
            }
        } catch (Exception e) {
            activo = false;
        }

        return new Device(ip, nombreEquipo, activo, tiempoRespuesta);
    }

    private static String resolverNslookup(String ip) {
        try {
            Process process = Runtime.getRuntime().exec("nslookup " + ip);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.contains("Name:") || linea.contains("Nombre:")) {
                    return linea.split(":")[1].trim();
                }
            }
        } catch (Exception ignored) {}
        return "Desconocido";
    }
}