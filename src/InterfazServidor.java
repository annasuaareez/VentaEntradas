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

/**
 * La clase InterfazServidor implementa la interfaz grafica de usuario para un sistema de gestion de tickets.
 * Permite realizar reservas y compras de entradas, generar informes de ventas y visualizarlos en formato PDF.
 * El servidor TCP incorporado maneja las conexiones de los clientes y coordina las operaciones del sistema.
 * La interfaz se actualiza dinamicamente para reflejar las cantidades de entradas disponibles y los ingresos totales.
 * Ademas, se utilizan bloqueos de sincronizacion para garantizar la consistencia de los datos compartidos entre hilos.
 * 
 * @author annas
 * 
 */

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
       
    /**
     * Devuelve el numero total de entradas disponibles.
     * @return El numero total de entradas disponibles.
     */
    public int getEntradasDisponibles() {
        return entradasTotales;
    }

    /**
     * Devuelve el numero de entradas generales disponibles.
     * @return El numero de entradas generales disponibles.
     */
    public int getEntradasGeneralesDisponibles() {
        return entradasGenerales;
    }

    /**
     * Devuelve el numero de entradas VIP disponibles.
     * @return El numero de entradas VIP disponibles.
     */
    public int getEntradasVIPDisponibles() {
        return entradasVIP;
    }

    /**
     * Devuelve el numero de abonos VIP disponibles.
     * @return El numero de abonos VIP disponibles.
     */
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

    /**
     * Agrega un contenedor al panel principal de la interfaz grafica.
     * El contenedor incluye un titulo, un numero y un icono.
     * @param parentPanel El panel principal al que se agrega el contenedor.
     * @param title El titulo del contenedor.
     * @param number El numero que se muestra en el contenedor.
     * @param iconFileName El nombre del archivo de icono para el contenedor.
     */
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

    /**
     * Inicia el servidor TCP en el puerto 124 y espera conexiones de clientes.
     * Muestra un mensaje indicando que el servidor se ha iniciado correctamente y esta esperando conexiones.
     * Utiliza un bucle infinito para aceptar multiples conexiones de clientes de forma concurrente.
     * Cuando un cliente se conecta, muestra un mensaje indicando la conexion del nuevo cliente.
     * Crea un nuevo hilo para manejar las operaciones del cliente y lo inicia.
     * En caso de excepcion de entrada/salida al abrir el servidor, imprime los errores.
     */
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
        
    /**
     * Se encarga de cargar una fuente personalizada.
     * Toma como parámetros el tamaño de la fuente y el nombre del archivo de fuente.
     * Intenta cargar el archivo de fuente desde la carpeta "font".
     * Crea una instancia de la fuente a partir del archivo de fuente cargado.
     * Devuelve la fuente personalizada con el tamaño especificado.
     * En caso de excepcion de formato de fuente o de entrada/salida, imprime los errores.
     * @param size El tamaño de la fuente.
     * @param fontFileName El nombre del archivo de fuente.
     * @return La fuente personalizada cargada.
     */
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
    
    /**
     * Clase interna que representa un hilo de cliente para manejar la comunicacion con un cliente.
     * Se encarga de recibir mensajes del cliente, procesarlos y actualizar la interfaz del servidor según sea necesario.
     */
    private class ClientThread extends Thread {
        private final Socket socket;
        
        private Map<String, Integer> reservasClienteLocal = new HashMap<>();
        
        /**
         * Constructor de la clase ClientThread.
         * Toma como parametro el socket del cliente con el que se va a comunicar.
         * @param socket El socket del cliente.
         */
        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        /**
         * Metodo run del hilo de cliente.
         * Se encarga de recibir mensajes del cliente, procesarlos y actualizar la interfaz del servidor segun sea necesario.
         */
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
    
    /**
     * Calcula el precio total de todas las entradas reservadas y compradas por el cliente.
     * Este metodo toma como parametro un mapa que contiene las reservas locales del cliente,
     * donde la clave es el tipo de entrada y el valor es la cantidad de entradas reservadas.
     * El precio total se calcula sumando el precio de todas las entradas generales, VIP y abonos VIP,
     * tanto las reservadas como las compradas.
     * Ademas, se imprimen en la consola los precios totales calculados para proporcionar informacion adicional.
     * 
     * @param reservasClienteLocal Mapa que contiene las reservas locales del cliente.
     */
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
       
    /**
     * Se encarga de actualizar la interfaz grafica del servidor con las cantidades actualizadas de entradas disponibles
     * despues de que un cliente haya realizado una reserva o una compra. Ademas, recalcula los ingresos totales teniendo en cuenta
     * las nuevas reservas y compras realizadas por el cliente.
     */
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
	
    /**
     * Se encarga de actualizar la interfaz del servidor con los ingresos totales obtenidos hasta el momento.
     * Actualiza el contenedor que muestra los ingresos totales y la capacidad total del evento. Utiliza bloqueos de
     * sincronizacion para garantizar la consistencia de los datos al actualizar la interfaz.
     */
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
    
    /**
     * Se encarga de generar un informe con la cantidad de entradas vendidas y los ingresos totales
     * obtenidos hasta el momento. Utiliza los datos almacenados sobre las entradas vendidas y los precios de cada tipo
     * de entrada para calcular los ingresos generados por la venta de entradas. Luego, genera un archivo PDF con esta
     * informacion y lo muestra al usuario.
     */
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
    
    /**
     * Este metodo genera un archivo PDF con los datos proporcionados sobre la cantidad de entradas vendidas
     * y los precios de cada tipo de entrada. Utiliza un archivo de plantilla JRXML para el diseño del informe,
     * que debe estar ubicado en la ruta "JasperSoft/Informe.jrxml". Los parametros del informe incluyen la cantidad
     * de entradas vendidas y los precios de cada tipo de entrada. El archivo PDF generado se guarda en la ruta "pdf/Informe.pdf".
     *
     * @param entradasGeneralesVendidas Cantidad de entradas generales vendidas.
     * @param entradasVIPVendidas Cantidad de entradas VIP vendidas.
     * @param abonosVIPVendidos Cantidad de abonos VIP vendidos.
     * @param precioEntradasGenerales Precio total de las entradas generales vendidas.
     * @param precioEntradasVIP Precio total de las entradas VIP vendidas.
     * @param precioAbonosVIP Precio total de los abonos VIP vendidos.
     */
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
    
    /**
     * Este metodo busca el archivo PDF generado en el directorio "pdf/" y lo abre utilizando el programa
     * predeterminado para archivos PDF en el sistema. Si el directorio y el archivo existen, se utiliza
     * la clase Desktop para abrir el archivo PDF. En caso de no encontrar el archivo o si ocurre algun error
     * al intentar abrirlo, se maneja la excepcion IOException y se imprime el rastreo de la pila.
     */
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
