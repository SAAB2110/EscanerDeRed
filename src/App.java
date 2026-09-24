import vista.VentanaVisualEscaner;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaVisualEscaner ventana = new VentanaVisualEscaner();
            ventana.setVisible(true);
        });
    }
}