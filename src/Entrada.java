import java.io.Serializable;

public class Entrada implements Serializable{
	private static final long serialVersionUID = 1L;
    private String nombre;
    private int cantidad;
    private double precio;

    public Entrada(String nombre, int cantidad, double precio) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }
    
    public Double getPrecio() {
        return precio;
    }
}
