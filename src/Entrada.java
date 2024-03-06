import java.io.Serializable;

/**
 * Esta clase representa una entrada para un evento.
 * Tiene tres atributos: "nombre" para el nombre de la entrada, "cantidad" para la cantidad de entradas
 * y "precio" para el precio de cada entrada. La clase incluye un constructor para inicializar estos atributos,
 * asi como metodos para obtener el nombre, la cantidad y el precio de la entrada.
 * La clase tambien implementa la interfaz Serializable para permitir que los objetos de esta clase se puedan serializar y deserializar.
 * 
 * @author Ana Suarez
 * 
 */

public class Entrada implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private int cantidad;
    private double precio;

    /**
     * Constructor de la clase Entrada.
     * @param nombre Nombre de la entrada.
     * @param cantidad Cantidad de entradas.
     * @param precio Precio de cada entrada.
     */
    public Entrada(String nombre, int cantidad, double precio) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    /**
     * Obtiene el nombre de la entrada.
     * @return Nombre de la entrada.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la cantidad de entradas.
     * @return Cantidad de entradas.
     */
    public int getCantidad() {
        return cantidad;
    }
    
    /**
     * Obtiene el precio de cada entrada.
     * @return Precio de cada entrada.
     */
    public Double getPrecio() {
        return precio;
    }
}
