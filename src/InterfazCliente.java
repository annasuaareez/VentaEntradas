import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.Timer;


import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class InterfazCliente {

    private JFrame frame;
    private JTextArea entradasAgregadasTextArea;
    private JButton reservarButton;
    private JButton comprarButton;
    private JButton addButton;
    private JProgressBar progressBar;
    private JLabel compraInfoLabel;
    private Timer timer;
    
    private int tiempoRestante = 15000;
    private boolean temporizadorActivo = false;
    
    private ArrayList<String> entradasReservadas = new ArrayList<>();
    private ArrayList<String> nombresArchivosGenerados = new ArrayList<>();
    private JComboBox<String> tipoEntradaComboBox;
    private JComboBox<String> cantidadEntradaComboBox;
    
    private Socket socket;
    private OutputStream outputStream;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    InterfazCliente window = new InterfazCliente();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public InterfazCliente() {
        conectarServidor();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setResizable(false);
        frame.setBounds(100, 100, 1200, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Gestor Ticket");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Reserva Ticket");
        Font titleFont = loadCustomFont(50, "gelosa_cara.otf");
        titleLabel.setFont(titleFont);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(45, 30, 0, 0));
        titlePanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Reserve hasta 3 entradas. Su reserva se mantendrá durante 2 minutos");
        Font subtitleFont = loadCustomFont(18, "manrope.ttf");
        subtitleLabel.setFont(subtitleFont);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 40, 0));
        titlePanel.add(subtitleLabel);

        mainPanel.add(titlePanel);

        JPanel gridPanel = new JPanel(new GridLayout(1, 2));

        JPanel ticketPanel = new JPanel();
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(60, 200, 0, 0));
        ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));

        JLabel tipoEntradaLabel = new JLabel("Tipo de Entrada:");
        tipoEntradaLabel.setFont(loadCustomFont(18, "manrope.ttf"));
        tipoEntradaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ticketPanel.add(tipoEntradaLabel);

        String[] opciones = {"Entrada General | 60,00 €", "Entrada VIP | 100,00 €", "Abono VIP | 150,00 €"};
        tipoEntradaComboBox = new JComboBox<>(opciones);
        tipoEntradaComboBox.setMaximumSize(new Dimension(250, tipoEntradaComboBox.getPreferredSize().height));
        tipoEntradaComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipoEntradaComboBox.setSelectedIndex(-1);
        ticketPanel.add(tipoEntradaComboBox);

        ticketPanel.add(Box.createVerticalStrut(20));

        JLabel cantidadEntradaLabel = new JLabel("Cantidad de Entrada:");
        cantidadEntradaLabel.setFont(loadCustomFont(18, "manrope.ttf"));
        cantidadEntradaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ticketPanel.add(cantidadEntradaLabel);

        String[] cantidad = {"1", "2", "3"};
        cantidadEntradaComboBox = new JComboBox<>(cantidad);
        cantidadEntradaComboBox.setMaximumSize(new Dimension(250, cantidadEntradaComboBox.getPreferredSize().height));
        cantidadEntradaComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        cantidadEntradaComboBox.setSelectedIndex(-1);
        ticketPanel.add(cantidadEntradaComboBox);

        ticketPanel.add(Box.createVerticalStrut(40));

        addButton = new JButton("Añadir");
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(250, addButton.getPreferredSize().height));
        ticketPanel.add(addButton);

        gridPanel.add(ticketPanel);

        JPanel infoPanel = new JPanel();
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        compraInfoLabel = new JLabel("Información Entradas"); // Modificación: se inicializa aquí
        compraInfoLabel.setFont(loadCustomFont(18, "manrope.ttf"));
        compraInfoLabel.setBorder(BorderFactory.createEmptyBorder(60, 50, 0, 0));
        compraInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(compraInfoLabel);

        entradasAgregadasTextArea = new JTextArea();
        entradasAgregadasTextArea.setEditable(false);
        entradasAgregadasTextArea.setLineWrap(true);
        entradasAgregadasTextArea.setWrapStyleWord(true);
        entradasAgregadasTextArea.setBackground(mainPanel.getBackground());
        entradasAgregadasTextArea.setFont(loadCustomFont(14, "manrope.ttf"));
        entradasAgregadasTextArea.setBorder(null);

        gridPanel.add(entradasAgregadasTextArea);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        reservarButton = new JButton("Reservar");
        reservarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reservarButton.setMaximumSize(new Dimension(150, reservarButton.getPreferredSize().height));
        reservarButton.setEnabled(false);
        buttonsPanel.add(reservarButton);

        buttonsPanel.add(Box.createVerticalStrut(20));

        comprarButton = new JButton("Comprar");
        comprarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        comprarButton.setMaximumSize(new Dimension(150, comprarButton.getPreferredSize().height));
        comprarButton.setVisible(false);
        buttonsPanel.add(comprarButton);

        gridPanel.add(buttonsPanel);

        buttonsPanel.add(Box.createVerticalStrut(20));

        int maxButtonWidth = Math.max(reservarButton.getPreferredSize().width, comprarButton.getPreferredSize().width);

        progressBar = new JProgressBar(0, 15000);
        buttonsPanel.add(progressBar);
        progressBar.setVisible(false);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setMaximumSize(new Dimension(maxButtonWidth, progressBar.getPreferredSize().height));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressBar.addChangeListener(e -> {
            if (progressBar.getValue() == progressBar.getMaximum()) {
                reservarButton.setEnabled(false);
            }
        });

        mainPanel.add(gridPanel);

        frame.getContentPane().add(mainPanel);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarEntradas();
            }
        });

        reservarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reservarEntradas();
            }
        });

        comprarButton.addActionListener(new ActionListener() {
            
			@Override
            public void actionPerformed(ActionEvent e) {
                comprarEntradas();
                detenerTemporizador();
            }
        });
    }

    private void conectarServidor() {
        try {
            socket = new Socket("localhost", 124);
            outputStream = socket.getOutputStream();
            System.out.println("Conexión establecida con el servidor.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void agregarEntradas() {    	
        if (tipoEntradaComboBox.getSelectedIndex() == -1 || cantidadEntradaComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(frame, "Por favor seleccione el tipo y la cantidad de entradas.");
            return;
        }

        String tipoEntrada = (String) tipoEntradaComboBox.getSelectedItem();
        int cantidadSeleccionada = Integer.parseInt((String) cantidadEntradaComboBox.getSelectedItem());
        String cantidadEntrada = (String) cantidadEntradaComboBox.getSelectedItem();

        int cantidadTotal = 0;
        
        for (String entrada : entradasReservadas) {
            String[] parts = entrada.split(" - Cantidad: ");
            cantidadTotal += Integer.parseInt(parts[1]);
        }

        if (cantidadTotal + cantidadSeleccionada > 3) {
            JOptionPane.showMessageDialog(frame, "No puede reservar más de tres entradas en total.");
            tipoEntradaComboBox.setSelectedIndex(-1);
            cantidadEntradaComboBox.setSelectedIndex(-1);
            return;
        }
        
        String entrada = tipoEntrada + " - Cantidad: " + cantidadEntrada; 
        entradasAgregadasTextArea.append(entrada + "\n");
        
        entradasReservadas.add(entrada);
        
        System.out.println("Entrada agregada: " + entrada);

        tipoEntradaComboBox.setSelectedIndex(-1);
        cantidadEntradaComboBox.setSelectedIndex(-1);
        
        reservarButton.setEnabled(true); 
   
    }

    private void reservarEntradas() {
    	System.out.println("El cliente ha reservado");
        if (!validarEntradas())
            return;
        
        progressBar.setVisible(true);
        iniciarTemporizador();
        
        for (String entrada : entradasReservadas) {
            String[] parts = entrada.split(" - Cantidad: ");
            String nombreEntrada = parts[0];
            int cantidad = Integer.parseInt(parts[1]);
            enviarMensaje("reserva:" + nombreEntrada + ":" + cantidad + "\n");
        }
        
        System.out.println("Enviando mensaje al servidor para reservar entradas: " + entradasReservadas);
        System.out.println();
        
       //entradasReservadas.clear();
        
        addButton.setEnabled(false);
        reservarButton.setEnabled(false);
        comprarButton.setVisible(true);
        comprarButton.setEnabled(true);
    }

    private void comprarEntradas() {    	
    	System.out.println("El cliente ha comprado");
    	enviarMensaje("comprar\n");
    	detenerTemporizador();
    	
    	System.out.println("Array " + entradasReservadas.size());
    	
    	generarPDF();
    	
    	mostrarPDF();
    	
    	JOptionPane.showMessageDialog(frame, "Compra realizada.");
    	
    	entradasAgregadasTextArea.setText("");
    	comprarButton.setEnabled(false);
        progressBar.setVisible(false);

    	entradasReservadas.clear();
    }

    private void generarPDF() {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport("JasperSoft/Entrada.jrxml");

            int entryIndex = 1;
            
            for (String entrada : entradasReservadas) {
                String[] parts = entrada.split(" - Cantidad: ");
                String entrada1 = parts[0].split("\\|")[0].trim();
                String tipoEntrada = parts[0];
                int cantidad = Integer.parseInt(parts[1]);
                String precio = "";

                if (tipoEntrada.equals("Entrada General | 60,00 €")) {
                    precio = "60.00";
                } else if (tipoEntrada.equals("Entrada VIP | 100,00 €")) {
                    precio = "100.00";
                } else if (tipoEntrada.equals("Abono VIP | 150,00 €")) {
                    precio = "150.00";
                }

                for (int i = 0; i < cantidad; i++) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("Entrada", entrada1);
                    parameters.put("Dinero", precio);

                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
                    
                    // Exportar el informe JasperPrint a un archivo PDF
                    String nombreArchivo = entrada1 + "_entrada" + entryIndex + ".pdf";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, "pdf/" + nombreArchivo);
                    
                    nombresArchivosGenerados.add(nombreArchivo);
                    
                    entryIndex++;
                }
            }
        } catch (JRException e) {
            e.printStackTrace();
        }
    }
    
    private void mostrarPDF() {
        try {
            for (String nombreArchivo : nombresArchivosGenerados) {
                File pdfFile = new File("pdf/" + nombreArchivo);
                if (pdfFile.exists() && pdfFile.isFile()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private boolean validarEntradas() {
        if (entradasReservadas.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No ha agregado ninguna entrada para reservar.");
            return false;
        }
        return true;
    }
    
    private void iniciarTemporizador() {
        if (!temporizadorActivo) {
            temporizadorActivo = true;
            tiempoRestante = 15000;
            progressBar.setValue(tiempoRestante);
            
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	tiempoRestante -= 1000;
                    progressBar.setValue(tiempoRestante);

                    if (tiempoRestante <= 0) {
                        detenerTemporizador();
                        enviarMensaje("cancelacion\n"); 
                        JOptionPane.showMessageDialog(frame, "Se ha cancelado la reserva por tiempo agotado.");
                        limpiarReserva();
                    }
                }
            });
            timer.start();
        }
    }
    
    private void detenerTemporizador() {
        if (temporizadorActivo) {
            timer.stop();
            temporizadorActivo = false;
        }
    }
    
    private void limpiarReserva() {
        entradasAgregadasTextArea.setText("");
        entradasReservadas.clear();
        reservarButton.setEnabled(false);
        comprarButton.setEnabled(false);
        temporizadorActivo = false;
        addButton.setEnabled(true);
        progressBar.setVisible(false);
    }
    
    private void enviarMensaje(String mensaje) {
        try {
            outputStream.write(mensaje.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Font loadCustomFont(int size, String fontFileName) {
        Font customFont = null;
        try {
            String fontPath = "./font/" + fontFileName;
            File fontFile = new File(fontPath);

            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        return customFont.deriveFont(Font.PLAIN, size);
    }
}