import java.io.Serializable;

public class Entrada implements Serializable{
	private static final long serialVersionUID = 1L;
    private String nombre;
    private int cantidad;

    public Entrada(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }
}
