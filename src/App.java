import controlador.EscanerRed;
import modelo.Device;
import utilidades.IpValidador;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        
        System.out.print("Ingrese una IP para escanear (ej. 127.0.0.1 o 8.8.8.8): ");
        String ipIngresada = scanner.nextLine();

        // 1. Validar formato de IP
        if (!IpValidador.esValida(ipIngresada)) {
            System.out.println("❌ Error: La dirección IP ingresada no es válida.");
            scanner.close();
            return;
        }

        System.out.println("Escaneando la dirección " + ipIngresada + "...");

        // 2. Ejecutar escaneo con timeout de 1000ms
        Device resultado = EscanerRed.escanearIp(ipIngresada, 1000);

        // 3. Imprimir el resultado
        System.out.println("----------------------------------------");
        System.out.println("IP: " + resultado.getIp());
        System.out.println("Estado: " + (resultado.isActive() ? "✅ Activo" : "❌ No responde"));
        System.out.println("Nombre de equipo: " + resultado.getHostname());
        System.out.println("Tiempo de respuesta: " + (resultado.getResponseTime() >= 0 ? resultado.getResponseTime() + " ms" : "N/A"));
        System.out.println("----------------------------------------");

        scanner.close();
    }
}