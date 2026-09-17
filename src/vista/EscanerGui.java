package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class EscanerGui extends JFrame {

    private JTextField txtIpInicio;
    private JTextField txtIpFin;
    private JTextField txtTimeout;
    private JButton btnEscanear;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JProgressBar progressBar;
    private JLabel lblEstado;

    public EscanerGui() {
        setTitle("Escáner de Red Local - Rango y Latencia");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Valores 192.168.1.x y Timeout
        JPanel panelEntrada = new JPanel(new FlowLayout());
        
        panelEntrada.add(new JLabel("IP Inicio:"));
        txtIpInicio = new JTextField("192.168.1.1", 10);
        panelEntrada.add(txtIpInicio);

        panelEntrada.add(new JLabel("IP Fin:"));
        txtIpFin = new JTextField("192.168.1.30", 10);
        panelEntrada.add(txtIpFin);

        panelEntrada.add(new JLabel("Timeout (ms):"));
        txtTimeout = new JTextField("500", 4);
        panelEntrada.add(txtTimeout);

        btnEscanear = new JButton("Escanear Rango");
        panelEntrada.add(btnEscanear);

        add(panelEntrada, BorderLayout.NORTH);

        // Tabla con 4 Columnas (incluye Tiempo de Respuesta)
        String[] columnas = {"Dirección IP", "Hostname", "Estado", "Tiempo (ms)"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(modeloTabla);
        add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        // Panel Inferior: Progreso y Estado
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        lblEstado = new JLabel(" Estado: Listo para escanear.");

        panelInferior.add(progressBar, BorderLayout.NORTH);
        panelInferior.add(lblEstado, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);

        btnEscanear.addActionListener(e -> ejecutarEscaneo());
    }

    private void ejecutarEscaneo() {
        String ipInicioStr = txtIpInicio.getText().trim();
        String ipFinStr = txtIpFin.getText().trim();
        String timeoutStr = txtTimeout.getText().trim();

        if (!utilidades.IpValidador.esValida(ipInicioStr) || !utilidades.IpValidador.esValida(ipFinStr)) {
            JOptionPane.showMessageDialog(this, "Formato de IP inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int timeout;
        try {
            timeout = Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El timeout debe ser un entero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String baseInicio = ipInicioStr.substring(0, ipInicioStr.lastIndexOf('.'));
        String baseFin = ipFinStr.substring(0, ipFinStr.lastIndexOf('.'));

        if (!baseInicio.equals(baseFin)) {
            JOptionPane.showMessageDialog(this, "Las IPs deben pertenecer a la misma subred.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int inicio = Integer.parseInt(ipInicioStr.substring(ipInicioStr.lastIndexOf('.') + 1));
        int fin = Integer.parseInt(ipFinStr.substring(ipFinStr.lastIndexOf('.') + 1));

        if (inicio > fin) {
            JOptionPane.showMessageDialog(this, "La IP inicial debe ser menor o igual a la final.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnEscanear.setEnabled(false);
        modeloTabla.setRowCount(0);

        int totalIps = (fin - inicio) + 1;
        progressBar.setMaximum(totalIps);
        progressBar.setValue(0);

        AtomicInteger completadas = new AtomicInteger(0);
        lblEstado.setText(" Estado: Escaneando...");

        new Thread(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(25);

            for (int i = inicio; i <= fin; i++) {
                final String ipAProbar = baseInicio + "." + i;

                executor.submit(() -> {
                    modelo.Device dev = controlador.EscanerRed.escanearIp(ipAProbar, timeout);

                    if (dev != null && dev.isActive()) {
                        final modelo.Device devEncontrado = dev;
                        SwingUtilities.invokeLater(() -> {
                            modeloTabla.addRow(new Object[]{
                                devEncontrado.getIp(),
                                devEncontrado.getHostname(),
                                "Alcanzable",
                                devEncontrado.getResponseTimeFormatted()
                            });
                        });
                    }

                    int progreso = completadas.incrementAndGet();
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progreso));
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }

            SwingUtilities.invokeLater(() -> {
                lblEstado.setText(" Estado: Escaneo completado.");
                btnEscanear.setEnabled(true);
            });
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EscanerGui().setVisible(true));
    }
}