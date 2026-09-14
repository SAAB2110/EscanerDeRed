package utilidades;

public class IpValidador {

    public static boolean esValida(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        // Separamos la cadena por los puntos
        String[] partes = ip.trim().split("\\.");

        // Debe tener exactamente 4 octetos
        if (partes.length != 4) {
            return false;
        }

        // Comprobamos que cada bloque sea un número de 0 a 255
        for (String parte : partes) {
            try {
                int numero = Integer.parseInt(parte);
                if (numero < 0 || numero > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false; // Si contiene letras o símbolos
            }
        }

        return true;
    }
}