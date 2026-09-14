package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EscanerGui extends JFrame {

    private JTextField txtIp;
    private JButton btnEscanear;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstado;

    public EscanerGui() {
        setTitle("Escáner de Red - MVP");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel de Entrada
        JPanel panelEntrada = new JPanel(new FlowLayout());
        panelEntrada.add(new JLabel("IP Target:"));
        txtIp = new JTextField("127.0.0.1", 15);
        panelEntrada.add(txtIp);
        btnEscanear = new JButton("Escanear");
        panelEntrada.add(btnEscanear);
        add(panelEntrada, BorderLayout.NORTH);

        // 2. Tabla de Resultados
        String[] columnas = {"Dirección IP", "Hostname", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(modeloTabla);
        add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        // 3. Barra de Estado
        lblEstado = new JLabel(" Estado: Listo");
        add(lblEstado, BorderLayout.SOUTH);

        btnEscanear.addActionListener(e -> ejecutarEscaneo());
    }

    private void ejecutarEscaneo() {
    String ipTexto = txtIp.getText().trim();

    btnEscanear.setEnabled(false);
    modeloTabla.setRowCount(0); // Limpiar tabla
    lblEstado.setText(" Estado: Escaneando subred en paralelo...");

    new Thread(() -> {
        // Extraer los primeros 3 octetos de la IP
        String baseIp = ipTexto;
        if (ipTexto.contains(".")) {
            int ultimoPunto = ipTexto.lastIndexOf('.');
            baseIp = ipTexto.substring(0, ultimoPunto);
        }

        // Crear un pool de 50 hilos en paralelo
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 1; i <= 254; i++) {
            final String ipAProbar = baseIp + "." + i;
            
            executor.submit(() -> {
                // Escaneo individual con timeout de 200ms
                modelo.Device dev = controlador.EscanerRed.escanearIp(ipAProbar, 200);

                if (dev != null && dev.isActive()) {
                    final modelo.Device dispositivoEncontrado = dev;
                    SwingUtilities.invokeLater(() -> {
                        modeloTabla.addRow(new Object[]{
                            dispositivoEncontrado.getIp(),
                            dispositivoEncontrado.getHostname(),
                            "Alcanzable"
                        });
                    });
                }
            });
        }

        // Apagar el executor y esperar a que terminen las tareas
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(" Estado: Escaneo de subred finalizado.");
            btnEscanear.setEnabled(true);
        });
    }).start();
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EscanerGui().setVisible(true);
        });
    }
}