import java.util.ArrayList;
import java.util.List;

class Proceso {
    int id;
    boolean activo;
    boolean esCoordinador;
    List<Proceso> todosLosProcesos;

    public Proceso(int id) {
        this.id = id;
        this.activo = true;
        this.esCoordinador = false;
    }

    public void setTodosLosProcesos(List<Proceso> procesos) {
        this.todosLosProcesos = procesos;
    }

    public void desactivar() {
        this.activo = false;
        this.esCoordinador = false;
        System.out.println("El proceso P" + id + " ha fallado (se ha desactivado).");
    }

    public void detectarFallaCoordinador() {
        if (this.activo) {
            System.out.println("\n[!] Proceso P" + id + " detecta que el coordinador no responde.");
            iniciarEleccion();
        }
    }

    public void enviarMensaje(String tipoMensaje, int idDestino) {
        System.out.println("   P" + this.id + " envia " + tipoMensaje + " a P" + idDestino);
    }

    public void iniciarEleccion() {
        System.out.println("\n=> Proceso P" + id + " inicia un proceso de ELECCION.");
        boolean recibioRespuesta = false;

        for (Proceso p : todosLosProcesos) {
            if (p.id > this.id) {
                enviarMensaje("ELECTION", p.id);
                if (p.activo) {
                    System.out.println("   <- P" + p.id + " responde OK a P" + id);
                    recibioRespuesta = true;
                }
            }
        }

        if (!recibioRespuesta) {
            anunciarCoordinador();
        } else {
            System.out.println("   P" + id + " se detiene y espera a que un proceso mayor termine la eleccion.");
            // Hacer que los procesos con mayor ID inicien su elección.
            for (Proceso p : todosLosProcesos) {
                if (p.id > this.id && p.activo) {
                    p.iniciarEleccion();
                    break; // Solo el primero superior desencadena, él llamará a los demás o ganará
                }
            }
        }
    }

    public void anunciarCoordinador() {
        this.esCoordinador = true;
        System.out.println("\n[***] Proceso P" + id + " se anuncia como nuevo COORDINATOR.");
        for (Proceso p : todosLosProcesos) {
            if (p.id != this.id && p.activo) {
                p.recibirMensajeCoordinador(this.id);
            }
        }
    }

    public void recibirMensajeCoordinador(int idCoordinador) {
        this.esCoordinador = false;
        System.out.println("   P" + id + " reconoce a P" + idCoordinador + " como nuevo coordinador.");
    }
}

public class Bully {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println(" Simulación de Algoritmo Bully");
        System.out.println("==========================================\n");

        // 1. Crear una lista de procesos P1, P2, P3, P4, P5
        List<Proceso> procesos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            procesos.add(new Proceso(i));
        }

        // Asignar la lista a cada proceso para que se conozcan
        for (Proceso p : procesos) {
            p.setTodosLosProcesos(procesos);
        }

        // 2. Definir inicialmente a P5 como coordinador
        Proceso p5 = procesos.get(4);
        p5.esCoordinador = true;
        System.out.println("Estado Inicial: P" + p5.id + " es el Coordinador actual.");

        // 3. Simulación de falla del coordinador
        p5.desactivar();

        // 4. Hacer que P2 detecte la falla
        Proceso p2 = procesos.get(1); // El id es 2, el índice es 1
        p2.detectarFallaCoordinador();

        // Contar la cantidad de mensajes generados es un análisis que se hace
        // a partir de la salida de la consola.
    }
}
