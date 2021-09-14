package edu.eci.arep.httpserver;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface with basic HttpServer Methods For The App.
 */
public interface HttpServer {
    /**
     * Basic metods for HTTP Server
     * @throws java.io.IOException
     */
    void inicioServidor() throws IOException;

    void obtenerArchivosEstaticos(String endpoint);

    void obtenerRecursos(String fullPath);

    void obtenerImagen(String fullPath);

    void errorMessage(int statusCode, String message, String statusName);

    OutputStream getOut();

    void setOut(OutputStream out);
}
