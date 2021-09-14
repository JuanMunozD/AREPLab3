package edu.eci.arep.httpserver;

import edu.eci.arep.nanospring.components.NanoSpringException;
import edu.eci.arep.nanospring.components.Request;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import static edu.eci.arep.nanospring.NanoSpringApplication.invoke;

/**
 * Implementation of HttpServer Methods.
 */
public class HttpServerImpl implements HttpServer {

    private static final String ROUTE_TO_STATIC_FILES = "/src/main/resources/static";
    private static final String BASIC_HTML_ERROR_START = "<!DOCTYPE html>\n <html>\n<head>\n<meta charset=\"UTF-8\">\n<title>";
    public static final String BASIC_HTML_ERROR_MEDIUM = " Error</title>\n</head>\n<body>\n";
    public static final String BASIC_HTML_END = "</body>\n</html>\n";
    private final Map<String, Method> componentsRoute;
    private ServerSocket serverSocket;
    private OutputStream out;
    private BufferedReader in;
    private boolean running;

    public HttpServerImpl(Map<String, Method> componentsRoute) {
        super();
        this.componentsRoute = componentsRoute;
    }

    @Override
    public void inicioServidor() throws IOException {
        int port = getPort();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
        }
        startAcceptingRequests();
    }

    @Override
    public void obtenerArchivosEstaticos(String endpoint) {
        String fullPath = ROUTE_TO_STATIC_FILES + endpoint;
        if (endpoint.contains("jpg")) {
            obtenerImagen(fullPath);
        } else if (endpoint.contains("html") || endpoint.contains("js")) {
            obtenerRecursos(fullPath);
        }
    }

    @Override
    public void obtenerRecursos(String fullPath) {
        String type = fullPath.split("\\.")[1];
        if (type.equals("js")) type = "json";
        try {
            in = new BufferedReader(new FileReader(System.getProperty("user.dir") + fullPath));
            String outLine = "";
            String line;
            while ((line = in.readLine()) != null) {
                outLine += line;
            }
            out.write(("HTTP/1.1 201 OK\r\n"
                    + "Content-Type: text/" + type + ";"
                    + "charset=\"UTF-8\" \r\n"
                    + "\r\n"
                    + outLine).getBytes());
        } catch (IOException e) {
            int statusCode = 404;
            errorMessage(statusCode,
                    BASIC_HTML_ERROR_START + statusCode + BASIC_HTML_ERROR_MEDIUM
                            + "<h1>404 File Not Found</h1>\n"
                            + BASIC_HTML_END, "Not Found");
        }
    }

    @Override
    public void obtenerImagen(String fullPath) {
        String type = fullPath.split("\\.")[1];
        try {
            BufferedImage image = ImageIO.read(new File(System.getProperty("user.dir") + fullPath));
            ByteArrayOutputStream arrBytes = new ByteArrayOutputStream();
            DataOutputStream outImage = new DataOutputStream(out);
            ImageIO.write(image, type, arrBytes);
            outImage.writeBytes("HTTP/1.1 200 OK \r\n"
                    + "Content-Type: image/" + type + " \r\n"
                    + "\r\n");
            out.write(arrBytes.toByteArray());
        } catch (IOException e) {
            int statusCode = 404;
            errorMessage(statusCode,
                    BASIC_HTML_ERROR_START + statusCode + BASIC_HTML_ERROR_MEDIUM
                            + "<h1>404 File Not Found</h1>\n"
                            + BASIC_HTML_END, "Not Found");
        }
    }

    @Override
    public void errorMessage(int statusCode, String message, String statusName) {
        try {
            out.write(("HTTP/1.1 " + statusCode + " " + statusName + "\r\n"
                    + "\r\n"
                    + message).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public OutputStream getOut() {
        return out;
    }

    @Override
    public void setOut(OutputStream out) {
        this.out = out;
    }

    private void startAcceptingRequests() throws IOException {
        running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            receiveRequest(clientSocket);
        }
        serverSocket.close();
    }

    private void receiveRequest(Socket clientSocket) throws IOException {
        out = clientSocket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String inputLine;
        String endpoint;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains("GET")) {
                endpoint = inputLine.split(" ")[1];
                if (endpoint.equals("/")) {
                    obtenerArchivosEstaticos("/index.html");
                } else if (endpoint.contains("/Nsapps")) {
                    endpoint = endpoint.replace("/Nsapps", "");
                    executeSpringEndpoint(endpoint, out);
                } else {
                    obtenerArchivosEstaticos(endpoint);
                }
            }
            if (!in.ready()) break;
        }
        in.close();
        clientSocket.close();
    }

    private void executeSpringEndpoint(String path, OutputStream out) throws IOException {
        Request request = new Request(path);
        String endpoint = request.getEndpoint();
        String value = request.getValue();
        if (componentsRoute.containsKey(endpoint)) {
            String result;
            try {
                if (value == null) {
                    result = invoke(componentsRoute.get(endpoint));
                } else {
                    result = invoke(componentsRoute.get(endpoint), value);
                }
                out.write(("HTTP/1.1 200 OK\r\n"
                        + "\r\n"
                        + result).getBytes());
            } catch (NanoSpringException e) {
                errorMessage(409,
                        BASIC_HTML_ERROR_START + 409 + BASIC_HTML_ERROR_MEDIUM
                                + "<h1>" + e.getMessage() + "</h1>\n"
                                + BASIC_HTML_END, "409 Conflict");
            }
        }
    }

    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4444; 
    }
}
