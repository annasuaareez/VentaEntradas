

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class InterfazServidor {

    private JFrame frame;
    private JPanel tipoTicketsContainerPanel;
	private JPanel containerPanel;
	private JButton generarInformeButton;

	private int entradasTotales = 170;

    private int entradasGenerales = 100;
    private int entradasVIP = 50;
    private int abonosVIP = 20;

    private int precioEntradaGeneral = 60;
    private int precioEntradaVIP = 100;
    private int precioAbonoVIP = 150;
    
    private int precioTotal = 0;
    
    private Map<String, Integer> reservasCliente = new HashMap<>();
    private Map<String, Integer> entradasCompradas = new HashMap<>();
    
    private final ReentrantLock lock = new ReentrantLock();
                
    public int getEntradasDisponibles() {
        return entradasTotales;
    }

    public int getEntradasGeneralesDisponibles() {
        return entradasGenerales;
    }

    public int getEntradasVIPDisponibles() {
        return entradasVIP;
    }

    public int getAbonosVIPDisponibles() {
        return abonosVIP;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    InterfazServidor window = new InterfazServidor();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public InterfazServidor() {
        initialize();

        new Thread(()->{
			startServer();
		}).start();
    }

    private void initialize() {
    	frame = new JFrame();
        frame.setResizable(false);
        frame.setBounds(100, 100, 700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Gestor Ticket");
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Gestor Ticket");
        Font titleFont = loadCustomFont(50, "gelosa_cara.otf");
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(45, 0, 40, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
                
        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        containerPanel.setBackground(Color.WHITE);
        
        addContainer(containerPanel, "Total Capacidad", 1000, "Photos\\users.png");
        addContainer(containerPanel, "Total Tickets Venta", entradasTotales, "Photos\\ticket.png");
        addContainer(containerPanel, "Ingresos Totales", precioTotal, "Photos\\credit-card.png");

        mainPanel.add(containerPanel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel tipoTicketsTitleLabel = new JLabel("Tipo Tickets");
        Font tipoTicketsTitleFont = loadCustomFont(35, "gelosa_cara.otf");
        tipoTicketsTitleLabel.setFont(tipoTicketsTitleFont);
        tipoTicketsTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(tipoTicketsTitleLabel);
        
        tipoTicketsContainerPanel = new JPanel();
        tipoTicketsContainerPanel.setLayout(new BoxLayout(tipoTicketsContainerPanel, BoxLayout.X_AXIS));
        tipoTicketsContainerPanel.setBackground(Color.WHITE);
        
        addContainer(tipoTicketsContainerPanel, "Entrada General", entradasGenerales, "Photos\\user.png");
        addContainer(tipoTicketsContainerPanel, "Entrada VIP", entradasVIP, "Photos\\star.png");
        addContainer(tipoTicketsContainerPanel, "Abono VIP", abonosVIP, "Photos\\pie-chart.png");
        
        mainPanel.add(tipoTicketsContainerPanel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        generarInformeButton = new JButton("Generar Informe"); 
        generarInformeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        generarInformeButton.addActionListener(e -> generarInforme()); 
        mainPanel.add(generarInformeButton);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private void addContainer(JPanel parentPanel, String title, int number, String iconFileName) {
        JPanel containerPanel = new JPanel();
        containerPanel.setBackground(Color.WHITE);

        Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        containerPanel.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));

        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        Font titleFont = loadCustomFont(13, "manrope.ttf");
        titleLabel.setFont(titleFont);
        containerPanel.add(titleLabel);

        JLabel numberLabel = new JLabel(String.valueOf(number));
        Font numberFont = loadCustomFont(24, "manrope.ttf");
        numberLabel.setFont(numberFont);
        containerPanel.add(numberLabel);
        
        Border numberLabelBorder = BorderFactory.createEmptyBorder(5, 0, 0, 0);
        numberLabel.setBorder(numberLabelBorder);

        ImageIcon originalIcon = new ImageIcon(iconFileName);
        Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        JLabel iconLabel = new JLabel(scaledIcon);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        containerPanel.add(iconLabel);

        parentPanel.add(containerPanel);
        containerPanel.setMaximumSize(new Dimension(250, 125));
        parentPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        if (title.equals("Ingresos Totales")) {
            numberLabel.setText(String.valueOf(precioTotal));
        }
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(124)) {
            System.out.println("Servidor TCP iniciado. Esperando conexiones...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado.");
                System.out.println();

                Thread clientThread = new Thread(new ClientThread(socket));
                clientThread.start();
                
            }
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
    
    private class ClientThread extends Thread {
        private final Socket socket;
        
        private Map<String, Integer> reservasClienteLocal = new HashMap<>();
        
        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String mensajeUsuario;

                while ((mensajeUsuario = bufferedReader.readLine()) != null) {
                    System.out.println("Mensaje: " + mensajeUsuario);
                    System.out.println();

                    if (mensajeUsuario.startsWith("reserva:")) {
                    	String[] parts = mensajeUsuario.split(":");
                        String nombreEntrada = parts[1];
                        int cantidad = Integer.parseInt(parts[2]);
                            
                        String[] nombreParts = nombreEntrada.split(" \\| ");
                        String nombre = nombreParts[0];
                          
                        System.out.println();
                        System.out.println("El cliente ha reservado " + cantidad + " entradas de tipo " + nombre);
                        System.out.println();
                            
                        synchronized (InterfazServidor.this) {
                            int cantidadReservada = reservasCliente.getOrDefault(nombre, 0);
                            reservasCliente.put(nombre, cantidadReservada + cantidad);

                            int cantidadReservadaLocal = reservasClienteLocal.getOrDefault(nombre, 0);
                            reservasClienteLocal.put(nombre, cantidadReservadaLocal + cantidad);
                        }
                           
                        System.out.println("Reservas de este cliente:");
                        System.out.println(reservasClienteLocal);
                        
                        actualizarInterfaz();
                    } else if (mensajeUsuario.equals("cancelacion")) {
                    	System.out.println("Operacion cancelada");
                    	
                    	synchronized (InterfazServidor.this) {
                            for (Map.Entry<String, Integer> entry : reservasClienteLocal.entrySet()) {
                                String tipoEntrada = entry.getKey();
                                int cantidadReservada = entry.getValue();
                                int cantidadTotal = reservasCliente.get(tipoEntrada);

                                reservasCliente.put(tipoEntrada, cantidadTotal - cantidadReservada);
                            }
                        }

                        reservasClienteLocal.clear();

                        System.out.println("Reservas actualizadas:");
                        System.out.println(reservasCliente);

                        actualizarInterfaz();
                    } else if (mensajeUsuario.equals("comprar")) {
                    	synchronized (InterfazServidor.this) {
                    		calcularPrecioTotal(reservasClienteLocal);
                            
                            for (Map.Entry<String, Integer> entry : reservasClienteLocal.entrySet()) {
                                String tipoEntrada = entry.getKey();
                                int cantidadComprada = entry.getValue();

                                int cantidadActual = entradasCompradas.getOrDefault(tipoEntrada, 0);
                                entradasCompradas.put(tipoEntrada, cantidadActual + cantidadComprada);
                            }
                            
                            reservasClienteLocal.clear();
                        }
                    	
                        actualizarIngresosTotales();
                    }
                }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
		        try {
		            // Cerrar el socket una vez que el cliente ha terminado
		            socket.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
        }
    } 
    
    private synchronized void calcularPrecioTotal(Map<String, Integer> reservasClienteLocal) {
        int precioEntradasGeneralesReservadas = precioEntradaGeneral * (reservasClienteLocal.getOrDefault("Entrada General", 0));
        int precioEntradasVIPReservadas = precioEntradaVIP * (reservasClienteLocal.getOrDefault("Entrada VIP", 0));
        int precioAbonosVIPReservados = precioAbonoVIP * (reservasClienteLocal.getOrDefault("Abono VIP", 0));

        int precioEntradasGeneralesCompradas = precioEntradaGeneral * (entradasCompradas.getOrDefault("Entrada General", 0));
        int precioEntradasVIPCompradas = precioEntradaVIP * (entradasCompradas.getOrDefault("Entrada VIP", 0));
        int precioAbonosVIPComprados = precioAbonoVIP * (entradasCompradas.getOrDefault("Abono VIP", 0));

        precioTotal = precioEntradasGeneralesReservadas + precioEntradasVIPReservadas + precioAbonosVIPReservados +
                precioEntradasGeneralesCompradas + precioEntradasVIPCompradas + precioAbonosVIPComprados;

        System.out.println("Precio Total = " + precioTotal);
    }
        
    private synchronized void actualizarInterfaz() {
        // Actualizar la interfaz del servidor con las nuevas cantidades de entradas disponibles
        lock.lock();
        try {
        	tipoTicketsContainerPanel.removeAll();

            addContainer(tipoTicketsContainerPanel, "Entrada General", (entradasGenerales - reservasCliente.getOrDefault("Entrada General", 0)), "Photos\\user.png");
            addContainer(tipoTicketsContainerPanel, "Entrada VIP", (entradasVIP - reservasCliente.getOrDefault("Entrada VIP", 0)), "Photos\\star.png");
            addContainer(tipoTicketsContainerPanel, "Abono VIP", (abonosVIP - reservasCliente.getOrDefault("Abono VIP", 0)), "Photos\\pie-chart.png");

            tipoTicketsContainerPanel.revalidate();
            tipoTicketsContainerPanel.repaint();
            
            actualizarIngresosTotales();
		} finally {
			lock.unlock();
		} 
    }
	
    private synchronized void actualizarIngresosTotales() {
        lock.lock();
        try {
        	containerPanel.removeAll();

            addContainer(containerPanel, "Total Capacidad", 1000, "Photos\\users.png");
            addContainer(containerPanel, "Total Tickets Venta", entradasTotales, "Photos\\ticket.png");
            addContainer(containerPanel, "Ingresos Totales", precioTotal, "Photos\\credit-card.png");

            containerPanel.revalidate();
            containerPanel.repaint();
		} finally {
			lock.unlock();
		}
    }
    
    private void generarInforme() {
        // Implementar la lógica para generar un informe
        System.out.println("Generando informe...");
        
        String entradasGeneralesVendidas = String.valueOf(entradasCompradas.getOrDefault("Entrada General", 0));
        String entradasVIPVendidas = String.valueOf(entradasCompradas.getOrDefault("Entrada VIP", 0));
        String abonosVIPVendidos = String.valueOf(entradasCompradas.getOrDefault("Abono VIP", 0));
        
        String precioEntradasGenerales = String.valueOf(precioEntradaGeneral * Integer.parseInt(entradasGeneralesVendidas));
        String precioEntradasVIP = String.valueOf(precioEntradaVIP * Integer.parseInt(entradasVIPVendidas));
        String precioAbonosVIP = String.valueOf(precioAbonoVIP * Integer.parseInt(abonosVIPVendidos));
        
        generarPDF(entradasGeneralesVendidas, entradasVIPVendidas, abonosVIPVendidos, precioEntradasGenerales, precioEntradasVIP, precioAbonosVIP);
        
        mostrarPDF();
    }
    
    private void generarPDF(String entradasGeneralesVendidas, String entradasVIPVendidas, String abonosVIPVendidos,
            String precioEntradasGenerales, String precioEntradasVIP, String precioAbonosVIP) {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport("JasperSoft/Informe.jrxml");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Cantidad_General", entradasGeneralesVendidas);
            parameters.put("Cantidad_VIP", entradasVIPVendidas);
            parameters.put("Cantidad_AbonoVIP", abonosVIPVendidos);
            
            parameters.put("Precio_General", precioEntradasGenerales);
            parameters.put("Precio_VIP", precioEntradasVIP);
            parameters.put("Precio_AbonoVIP", precioAbonosVIP);
            
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            
            String pdfPath = "pdf/Informe.pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);
            
        } catch (JRException e) {
            e.printStackTrace();
        }
    }
    
    private void mostrarPDF() {
        try {
            File pdfDirectory = new File("pdf/");
            if (pdfDirectory.exists() && pdfDirectory.isDirectory()) {
                File[] pdfFiles = pdfDirectory.listFiles();
                if (pdfFiles != null) {
                    for (File pdfFile : pdfFiles) {
                        if (pdfFile.isFile() && pdfFile.getName().equals("Informe.pdf")) {
                            Desktop.getDesktop().open(pdfFile);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
