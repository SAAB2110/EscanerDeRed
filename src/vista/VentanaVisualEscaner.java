package vista;

import modelo.DispositivoEncontrado;
import controlador.LogicaEscaner;
import utilidades.ValidarIpValida;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VentanaVisualEscaner extends JFrame {

    private JTextField txtIpInicio;
    private JTextField txtIpFin;
    private JTextField txtTimeout;
    private JButton btnEscanear;
    private JButton btnGuardarTxt;
    private JTable tablaResultados;
    private DefaultTableModel datosTabla;
    private JLabel lblEstadoTexto;
    private JProgressBar barraProgreso;

    public VentanaVisualEscaner() {
        setTitle("Escáner de Red Local");
        setSize(780, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel panelControles = new JPanel(new FlowLayout());

        panelControles.add(new JLabel("IP Inicio:"));
        txtIpInicio = new JTextField("192.168.0.1", 10);
        panelControles.add(txtIpInicio);

        panelControles.add(new JLabel("IP Fin:"));
        txtIpFin = new JTextField("192.168.0.15", 10);
        panelControles.add(txtIpFin);

        panelControles.add(new JLabel("Timeout (ms):"));
        txtTimeout = new JTextField("1000", 4);
        panelControles.add(txtTimeout);

        btnEscanear = new JButton("Escanear Rango");
        btnGuardarTxt = new JButton("Guardar TXT");

        panelControles.add(btnEscanear);
        panelControles.add(btnGuardarTxt);

        add(panelControles, BorderLayout.NORTH);

        String[] columnas = {"Dirección IP", "Hostname", "Estado", "Tiempo de Respuesta"};
        datosTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(datosTabla);
        add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblEstadoTexto = new JLabel("Estado: Listo para escanear.");
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setValue(0);

        panelInferior.add(lblEstadoTexto, BorderLayout.WEST);
        panelInferior.add(barraProgreso, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        btnEscanear.addActionListener(e -> empezarEscaneo());
        btnGuardarTxt.addActionListener(e -> guardarEnDocumentos());
    }

    private void empezarEscaneo() {
        String ipIni = txtIpInicio.getText().trim();
        String ipFin = txtIpFin.getText().trim();
        String timeoutTxt = txtTimeout.getText().trim();

        if (!ValidarIpValida.verificarFormato(ipIni) || !ValidarIpValida.verificarFormato(ipFin)) {
            JOptionPane.showMessageDialog(this, "Las IPs no son válidas.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int timeoutVal;
        try {
            timeoutVal = Integer.parseInt(timeoutTxt);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El timeout debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long lIpIni = ipToLong(ipIni);
        long lIpFin = ipToLong(ipFin);

        if (lIpIni > lIpFin) {
            JOptionPane.showMessageDialog(this, "La IP inicial no puede ser mayor a la final.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnEscanear.setEnabled(false);
        datosTabla.setRowCount(0);
        lblEstadoTexto.setText("Estado: Escaneando red...");
        
        long totalIps = (lIpFin - lIpIni) + 1;
        barraProgreso.setMinimum(0);
        barraProgreso.setMaximum((int) totalIps);
        barraProgreso.setValue(0);

        new Thread(() -> {
            int progresoActual = 0;

            for (long i = lIpIni; i <= lIpFin; i++) {
                String ipActual = longToIp(i);
                DispositivoEncontrado dev = LogicaEscaner.probarIp(ipActual, timeoutVal);

                progresoActual++;
                final int paso = progresoActual;

                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue(paso);
                    if (dev != null) {
                        String estadoStr = dev.isResponde() ? "Alcanzable" : "No alcanzable";
                        String latenciaStr = dev.isResponde() ? dev.getTiempoFormateado() : "-";
                        
                        datosTabla.addRow(new Object[]{
                            dev.getIp(),
                            dev.getHost(),
                            estadoStr,
                            latenciaStr
                        });
                    }
                });
            }

            SwingUtilities.invokeLater(() -> {
                lblEstadoTexto.setText("Estado: Escaneo completado.");
                btnEscanear.setEnabled(true);
            });
        }).start();
    }

    private long ipToLong(String ipAddress) {
        long result = 0;
        String[] atoms = ipAddress.split("\\.");
        for (int i = 0; i < 4; i++) {
            result = (result << 8) + Integer.parseInt(atoms[i]);
        }
        return result;
    }

    private String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }

    private void guardarEnDocumentos() {
        if (datosTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay nada en la tabla para guardar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String homeUsuario = System.getProperty("user.home");
            File carpetaDocs = new File(homeUsuario, "Documents");

            if (!carpetaDocs.exists()) {
                carpetaDocs = new File(homeUsuario, "Documentos");
                if (!carpetaDocs.exists()) {
                    carpetaDocs = new File(homeUsuario);
                }
            }

            File archivoSalida = new File(carpetaDocs, "reporte_escaneo.txt");

            try (PrintWriter pw = new PrintWriter(new FileWriter(archivoSalida))) {
                String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

                pw.println("      ESCANEO DE RED     ");
                pw.println(" Fecha: " + fechaActual);
                pw.println(" Rango: " + txtIpInicio.getText() + " - " + txtIpFin.getText());
                pw.println(" Timeout: " + txtTimeout.getText() + " ms");
                pw.println();
                pw.printf("%-18s %-25s %-12s %-10s%n", "DIRECCIÓN IP", "HOSTNAME", "ESTADO", "LATENCIA");
                pw.println("------------------------------------------------------------------");

                for (int i = 0; i < datosTabla.getRowCount(); i++) {
                    String ip = datosTabla.getValueAt(i, 0).toString();
                    String host = datosTabla.getValueAt(i, 1).toString();
                    String estado = datosTabla.getValueAt(i, 2).toString();
                    String latencia = datosTabla.getValueAt(i, 3).toString();

                    pw.printf("%-18s %-25s %-12s %-10s%n", ip, host, estado, latencia);
                }

                pw.println("------------------------------------------------------------------");
                pw.println("Total evaluadas: " + datosTabla.getRowCount());
            }

            JOptionPane.showMessageDialog(this,
                    "Reporte guardado en:\n" + archivoSalida.getAbsolutePath(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}