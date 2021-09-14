package edu.eci.arep.nanospring;

import edu.eci.arep.httpserver.HttpServer;
import edu.eci.arep.httpserver.HttpServerImpl;
import edu.eci.arep.nanospring.components.NanoSpringException;
import edu.eci.arep.nanospring.components.PathVariable;
import edu.eci.arep.nanospring.components.RequestMapping;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class NanoSpringApplication {
    private static final NanoSpringApplication nanoSpringApplication = new NanoSpringApplication();
    private static boolean componentsLoaded = false;
    private final Map<String, Method> componentsRoute = new HashMap<>();

    private NanoSpringApplication() {
        super();
    }

    public static void run(String[] args) throws ClassNotFoundException {
        if (!componentsLoaded) {
            nanoSpringApplication.loadComponents(args);
            componentsLoaded = true;
            nanoSpringApplication.startServer();
        }
    }

    private void startServer() {
        HttpServer httpServer = new HttpServerImpl(componentsRoute);
        try {
            httpServer.inicioServidor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadComponents(String[] components) throws ClassNotFoundException {
        for (String component : components) {
            for (Method method : Class.forName(component).getMethods()) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    componentsRoute.put(method.getAnnotation(RequestMapping.class).value(), method);
                }
            }
        }
    }

    public static String invoke(Method staticMethod, String... args) throws NanoSpringException {
        String result;
        String argument = null;
        try {
            for (Parameter parameter : staticMethod.getParameters()) {
                if (parameter.isAnnotationPresent(PathVariable.class)) {
                    if (args.length == 0) {
                        throw new NanoSpringException("No Hay Valor definido par el @PathVariable");
                    }
                    argument = args[0];
                }
            }
            if (argument != null) {
                result = staticMethod.invoke(null, argument).toString();
            } else {
                result = staticMethod.invoke(null).toString();
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NanoSpringException("Error al ejecutar la funcionalidad del endpoint");
        }
        return result;
    }
}
