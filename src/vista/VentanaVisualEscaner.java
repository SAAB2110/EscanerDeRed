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

        // Panel superior de controles
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

        // Tabla central
        String[] columnas = {"Dirección IP", "Hostname", "Estado", "Tiempo de Respuesta"};
        datosTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(datosTabla);
        add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        // Panel inferior con etiqueta de estado y barra de progreso
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblEstadoTexto = new JLabel("Estado: Listo para escanear.");
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true); // Muestra el porcentaje % en la barra
        barraProgreso.setValue(0);

        panelInferior.add(lblEstadoTexto, BorderLayout.WEST);
        panelInferior.add(barraProgreso, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        // Listeners
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

        String subredIni = ipIni.substring(0, ipIni.lastIndexOf('.'));
        String subredFin = ipFin.substring(0, ipFin.lastIndexOf('.'));

        if (!subredIni.equalsIgnoreCase(subredFin)) {
            JOptionPane.showMessageDialog(this, "Tienen que pertenecer a la misma red.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numIni = Integer.parseInt(ipIni.substring(ipIni.lastIndexOf('.') + 1));
        int numFin = Integer.parseInt(ipFin.substring(ipFin.lastIndexOf('.') + 1));

        if (numIni > numFin) {
            JOptionPane.showMessageDialog(this, "La IP inicial no puede ser mayor a la final.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Configuración inicial del escaneo
        btnEscanear.setEnabled(false);
        datosTabla.setRowCount(0);
        lblEstadoTexto.setText("Estado: Escaneando red...");
        
        int totalIps = (numFin - numIni) + 1;
        barraProgreso.setMinimum(0);
        barraProgreso.setMaximum(totalIps);
        barraProgreso.setValue(0);

        // Hilo de escaneo en segundo plano
        new Thread(() -> {
            int progresoActual = 0;

            for (int i = numIni; i <= numFin; i++) {
                String ipActual = subredIni + "." + i;
                DispositivoEncontrado dev = LogicaEscaner.probarIp(ipActual, timeoutVal);

                progresoActual++;
                final int paso = progresoActual;

                // Actualizar interfaz desde el hilo principal de Swing
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue(paso);
                    if (dev != null && dev.isResponde()) {
                        datosTabla.addRow(new Object[]{
                            dev.getIp(),
                            dev.getHost(),
                            "Alcanzable",
                            dev.getTiempoFormateado()
                        });
                    }
                });
            }

            // Al finalizar el bucle
            SwingUtilities.invokeLater(() -> {
                lblEstadoTexto.setText("Estado: Escaneo completado.");
                btnEscanear.setEnabled(true);
            });
        }).start();
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
                pw.println("Total encontrados: " + datosTabla.getRowCount());
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