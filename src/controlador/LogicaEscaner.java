package controlador;

import modelo.DispositivoEncontrado;
import java.net.InetAddress;

public class LogicaEscaner {

    public static DispositivoEncontrado probarIp(String ipAProbar, int tiempoMaximo) {
        try {
            InetAddress inet = InetAddress.getByName(ipAProbar);

            long inicio = System.currentTimeMillis();
            boolean contesto = inet.isReachable(tiempoMaximo);
            long fin = System.currentTimeMillis();

            long demora = fin - inicio;

            if (contesto && demora <= tiempoMaximo) {
                String nombreEquipo = inet.getCanonicalHostName();
                if (nombreEquipo.equalsIgnoreCase(ipAProbar)) {
                    nombreEquipo = "No resuelto";
                }

                return new DispositivoEncontrado(ipAProbar, nombreEquipo, true, demora);
            }

        } catch (Exception e) {
        }

        return null;
    }
}