import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class BullyServer {

    static List<ProcesoNode> procesos = new ArrayList<>();
    static List<String> logs = new ArrayList<>();

    static class ProcesoNode {
        int id;
        boolean activo;
        boolean esCoordinador;

        public ProcesoNode(int id) {
            this.id = id;
            this.activo = true;
            this.esCoordinador = false;
        }
    }

    public static void main(String[] args) throws Exception {
        // Inicializar 5 procesos
        for (int i = 1; i <= 5; i++) {
            procesos.add(new ProcesoNode(i));
        }
        procesos.get(4).esCoordinador = true; // P5 es coordinador inicial
        log("Sistema inicializado. P5 es el coordinador.");

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/api/state", new StateHandler());
        server.createContext("/api/fail", new FailHandler());
        server.createContext("/api/detect", new DetectHandler());
        server.createContext("/api/reset", new ResetHandler());
        
        server.setExecutor(null); // creates a default executor
        System.out.println("Servidor Backend iniciado en el puerto 8081");
        server.start();
    }

    static void log(String msg) {
        System.out.println(msg);
        logs.add(msg);
    }

    static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            setCorsHeaders(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            StringBuilder json = new StringBuilder("{\"procesos\":[");
            for (int i = 0; i < procesos.size(); i++) {
                ProcesoNode p = procesos.get(i);
                json.append(String.format("{\"id\":%d,\"activo\":%b,\"esCoordinador\":%b}", 
                    p.id, p.activo, p.esCoordinador));
                if (i < procesos.size() - 1) json.append(",");
            }
            json.append("],\"logs\":[");
            for (int i = 0; i < logs.size(); i++) {
                json.append("\"").append(logs.get(i).replace("\"", "\\\"")).append("\"");
                if (i < logs.size() - 1) json.append(",");
            }
            json.append("]}");

            String response = json.toString();
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class FailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            setCorsHeaders(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }
            
            // Para simplificar, asumimos que siempre falla el P5 (coordinador actual)
            ProcesoNode p5 = procesos.get(4);
            p5.activo = false;
            p5.esCoordinador = false;
            log("El proceso P5 ha fallado (desactivado).");
            
            String response = "{\"status\":\"ok\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class DetectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            setCorsHeaders(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            // P2 detecta la falla
            log("[!] Proceso P2 detecta que el coordinador no responde.");
            ejecutarAlgoritmoBully(2);

            String response = "{\"status\":\"ok\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void ejecutarAlgoritmoBully(int iniciadorId) {
            iniciarEleccion(iniciadorId);
        }

        private void iniciarEleccion(int id) {
            log("=> Proceso P" + id + " inicia un proceso de ELECCION.");
            boolean recibioRespuesta = false;

            for (ProcesoNode p : procesos) {
                if (p.id > id) {
                    log("   P" + id + " envia ELECTION a P" + p.id);
                    if (p.activo) {
                        log("   <- P" + p.id + " responde OK a P" + id);
                        recibioRespuesta = true;
                    }
                }
            }

            if (!recibioRespuesta) {
                anunciarCoordinador(id);
            } else {
                log("   P" + id + " se detiene y espera a que un proceso mayor termine la eleccion.");
                for (ProcesoNode p : procesos) {
                    if (p.id > id && p.activo) {
                        iniciarEleccion(p.id);
                        break;
                    }
                }
            }
        }

        private void anunciarCoordinador(int idGanador) {
            log("[***] Proceso P" + idGanador + " se anuncia como nuevo COORDINATOR.");
            for (ProcesoNode p : procesos) {
                if (p.id == idGanador) {
                    p.esCoordinador = true;
                } else {
                    p.esCoordinador = false;
                    if (p.activo) {
                        log("   P" + p.id + " reconoce a P" + idGanador + " como nuevo coordinador.");
                    }
                }
            }
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            setCorsHeaders(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            procesos.clear();
            logs.clear();
            for (int i = 1; i <= 5; i++) {
                procesos.add(new ProcesoNode(i));
            }
            procesos.get(4).esCoordinador = true;
            log("Sistema reiniciado. P5 es el coordinador.");

            String response = "{\"status\":\"ok\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
