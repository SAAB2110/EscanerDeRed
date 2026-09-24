package utilidades;

import java.util.regex.Pattern;

public class ValidarIpValida {

    private static final String REGEX_IP =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern PATRON = Pattern.compile(REGEX_IP);

    public static boolean verificarFormato(String textoIp) {
        if (textoIp == null || textoIp.trim().isEmpty()) {
            return false;
        }
        return PATRON.matcher(textoIp.trim()).matches();
    }
}