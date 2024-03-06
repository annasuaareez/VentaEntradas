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

/**
 * La clase InterfazCliente representa la interfaz grafica de un sistema de gestion de reservas y compras de entradas.
 * Permite al usuario agregar, reservar y comprar entradas para un evento. La interfaz incluye opciones para seleccionar
 * el tipo y la cantidad de entradas, asi como botones para agregar, reservar y comprar. Tambien muestra un area de texto
 * para visualizar las entradas agregadas, una barra de progreso para el tiempo de reserva y mensajes de confirmacion para
 * las acciones realizadas. Ademas, la clase se encarga de la comunicacion con el servidor para enviar mensajes relacionados
 * con la reserva y compra de entradas, asi como de la generacion y visualizacion de archivos PDF con las entradas reservadas.
 * El codigo se estructura en metodos que realizan funciones especificas, como agregar, reservar y comprar entradas, gestionar
 * el temporizador de reserva, limpiar la reserva, cargar fuentes personalizadas y enviar mensajes al servidor. 
 * 
 * @author Ana Suarez
 * 
 */


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

        compraInfoLabel = new JLabel("Información Entradas"); // Modificación: se inicializa aqui
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

        /**
         * Configura un ActionListener para el boton "Añadir".
         * Cuando se hace clic en el boton, llama al metodo agregarEntradas().
         */
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarEntradas();
            }
        });

        /**
         * Configura un ActionListener para el boton "Reservar".
         * Cuando se hace clic en el boton, llama al metodo reservarEntradas().
         */
        reservarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reservarEntradas();
            }
        });

        /**
         * Configura un ActionListener para el boton "Comprar".
         * Cuando se hace clic en el boton, llama al metodo comprarEntradas() y detiene el temporizador.
         */
        comprarButton.addActionListener(new ActionListener() {
            
			@Override
            public void actionPerformed(ActionEvent e) {
                comprarEntradas();
                detenerTemporizador();
            }
        });
    }

    /**
     * Se encarga de conectar con el servidor.
     * Intenta establecer una conexión con el servidor en el localhost y el puerto 124.
     * Obtiene el flujo de salida del socket para enviar mensajes al servidor.
     * Imprime un mensaje indicando que la conexion con el servidor se ha establecido correctamente.
     * En caso de excepcion, imprime los errores.
     */

    private void conectarServidor() {
        try {
            socket = new Socket("localhost", 124);
            outputStream = socket.getOutputStream();
            System.out.println("Conexión establecida con el servidor.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Se encarga de agregar entradas al sistema de reservas.
     * Verifica la seleccion del tipo y la cantidad de entradas, mostrando un mensaje de error
     * si no se han seleccionado correctamente. Controla tambien que no se exceda el limite
     * maximo de tres entradas en total. Actualiza la interfaz de usuario mostrando las entradas
     * agregadas en un área de texto y habilitando el boton de reservar cuando corresponde.
     */
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

    /**
     * Se encarga de reservar las entradas seleccionadas por el cliente.
     * Imprime un mensaje por consola indicando que el cliente ha realizado una reserva.
     * Verifica la validez de las entradas seleccionadas utilizando el metodo validarEntradas().
     * Muestra una barra de progreso y inicia un temporizador.
     * Envia un mensaje al servidor para reservar cada entrada individualmente.
     * Deshabilita los botones de agregar y reservar, y habilita el boton de comprar.
     */
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

    /**
     * Se encarga de realizar la compra de las entradas seleccionadas por el cliente.
     * Imprime un mensaje por consola indicando que el cliente ha realizado una compra.
     * Envia un mensaje al servidor para confirmar la compra.
     * Detiene el temporizador utilizado para la reserva de entradas.
     * Genera un archivo PDF con las entradas reservadas.
     * Muestra el archivo PDF generado al cliente.
     * Muestra un mensaje de confirmación de la compra.
     * Limpia el área de texto de las entradas agregadas y deshabilita el boton de comprar.
     * Oculta la barra de progreso.
     * Limpia el arreglo de entradas reservadas.
     */
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

    /**
     * Se encarga de generar archivos PDF con las entradas reservadas.
     * Compila el archivo JRXML que contiene el diseño del informe utilizando JasperSoft.
     * Itera sobre las entradas reservadas y genera un archivo PDF para cada una de ellas.
     * Asigna un precio al tipo de entrada y utiliza este valor en el informe.
     * Los archivos PDF se guardan en la carpeta "pdf" con nombres descriptivos.
     * Agrega los nombres de los archivos generados a una lista para su posterior uso.
     */
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
    
    /**
     * Se encarga de mostrar los archivos PDF generados al cliente.
     * Itera sobre los nombres de los archivos PDF generados.
     * Verifica si el archivo existe y es un archivo valido.
     * Abre el archivo PDF utilizando el programa predeterminado del sistema.
     */
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

    /**
     * Se encarga de validar las entradas antes de proceder con la reserva.
     * Verifica si la lista de entradas reservadas esta vacia.
     * Muestra un mensaje de error si no se han agregado entradas para reservar.
     * @return true si las entradas estan validadas correctamente, de lo contrario, devuelve false.
     */
	private boolean validarEntradas() {
        if (entradasReservadas.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No ha agregado ninguna entrada para reservar.");
            return false;
        }
        return true;
    }
    
	/**
	 * Se encarga de iniciar el temporizador para la reserva de entradas.
	 * Verifica si el temporizador está activo antes de iniciarlo.
	 * Establece el tiempo restante en 15000 milisegundos y actualiza el valor de la barra de progreso.
	 * Crea un nuevo objeto Timer que se ejecuta cada segundo.
	 * Durante cada ejecución del temporizador, se reduce en 1000 milisegundos el tiempo restante
	 * y se actualiza el valor de la barra de progreso.
	 * Si el tiempo restante llega a cero, se detiene el temporizador, se envia un mensaje de cancelación
	 * al servidor, se muestra un mensaje de advertencia al cliente y se limpia la reserva.
	 */
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
    
    /**
     * Se encarga de detener el temporizador utilizado para la reserva de entradas.
     * Verifica si el temporizador está activo antes de detenerlo.
     * Detiene el temporizador y establece la bandera de temporizador activo a false.
     */
    private void detenerTemporizador() {
        if (temporizadorActivo) {
            timer.stop();
            temporizadorActivo = false;
        }
    }
    
    /**
     * Se encarga de limpiar la reserva de entradas.
     * Limpia el area de texto donde se muestran las entradas agregadas.
     * Limpia la lista de entradas reservadas.
     * Deshabilita los botones de reservar y comprar.
     * Establece la bandera de temporizador activo a false.
     * Habilita el botón de agregar.
     * Oculta la barra de progreso.
     */
    private void limpiarReserva() {
        entradasAgregadasTextArea.setText("");
        entradasReservadas.clear();
        reservarButton.setEnabled(false);
        comprarButton.setEnabled(false);
        temporizadorActivo = false;
        addButton.setEnabled(true);
        progressBar.setVisible(false);
    }
    
    /**
     * Se encarga de enviar un mensaje al servidor.
     * Toma como parámetro el mensaje a enviar.
     * Intenta escribir el mensaje en el flujo de salida y lo envía.
     * En caso de excepcion de entrada/salida, imprime el error.
     * @param mensaje El mensaje a enviar al servidor.
     */
    private void enviarMensaje(String mensaje) {
        try {
            outputStream.write(mensaje.getBytes());
            outputStream.flush();
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
     * En caso de excepción de formato de fuente o de entrada/salida, imprime los errores.
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
}