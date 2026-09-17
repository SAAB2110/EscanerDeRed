package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EscanerGui extends JFrame {

    private JTextField txtIpInicio;
    private JTextField txtIpFin;
    private JButton btnEscanear;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstado;

    public EscanerGui() {
        setTitle("Escáner de Red - Entrega Final");
        setSize(650, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel de Entrada con IP Inicio e IP Fin
        JPanel panelEntrada = new JPanel(new FlowLayout());

        panelEntrada.add(new JLabel("IP Inicio:"));
        txtIpInicio = new JTextField("192.168.1.1", 11);
        panelEntrada.add(txtIpInicio);

        panelEntrada.add(new JLabel("IP Fin:"));
        txtIpFin = new JTextField("192.168.1.50", 11);
        panelEntrada.add(txtIpFin);

        btnEscanear = new JButton("Escanear Rango");
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

        // Evento del botón
        btnEscanear.addActionListener(e -> ejecutarEscaneo());
    }

    private void ejecutarEscaneo() {
        String ipInicioStr = txtIpInicio.getText().trim();
        String ipFinStr = txtIpFin.getText().trim();

        // Validar formato de ambas IPs usando tu clase de utilidades
        if (!utilidades.IpValidador.esValida(ipInicioStr) || !utilidades.IpValidador.esValida(ipFinStr)) {
            JOptionPane.showMessageDialog(this, 
                "Verificá que ambas direcciones IP tengan un formato válido (ej. 192.168.1.1).", 
                "Error de entrada", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extraer la base (primeros 3 octetos) y los rangos del último octeto
        String baseInicio = ipInicioStr.substring(0, ipInicioStr.lastIndexOf('.'));
        String baseFin = ipFinStr.substring(0, ipFinStr.lastIndexOf('.'));

        if (!baseInicio.equals(baseFin)) {
            JOptionPane.showMessageDialog(this, 
                "Ambas IPs deben pertenecer a la misma subred (los primeros 3 octetos deben ser iguales).", 
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int inicio = Integer.parseInt(ipInicioStr.substring(ipInicioStr.lastIndexOf('.') + 1));
        int fin = Integer.parseInt(ipFinStr.substring(ipFinStr.lastIndexOf('.') + 1));

        if (inicio > fin) {
            JOptionPane.showMessageDialog(this, 
                "La IP de inicio no puede ser mayor que la IP de fin.", 
                "Error de rango", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Preparar la UI para el escaneo
        btnEscanear.setEnabled(false);
        modeloTabla.setRowCount(0); // Limpiar tabla previa
        lblEstado.setText(" Estado: Escaneando rango " + ipInicioStr + " - " + ipFinStr + "...");

        // Escaneo multihilo en segundo plano
        new Thread(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(30);

            for (int i = inicio; i <= fin; i++) {
                final String ipAProbar = baseInicio + "." + i;

                executor.submit(() -> {
                    modelo.Device dev = controlador.EscanerRed.escanearIp(ipAProbar, 500);

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

            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            SwingUtilities.invokeLater(() -> {
                lblEstado.setText(" Estado: Escaneo por rango finalizado.");
                btnEscanear.setEnabled(true);
            });
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EscanerGui().setVisible(true));
    }
}