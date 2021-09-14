package edu.eci.arep;

import edu.eci.arep.nanospring.NanoSpringApplication;

/**
 * Basic HTTP Server With NanoSpring Framework App.
 */
public class App {

    /**
     * Main function Of The App.
     *
     * @param args List with the components that use the Nano Spring Framework.
     * @throws ClassNotFoundException When Some Component does not exist.
     */
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length == 0) {
            String[] path = {"edu.eci.arep.demoService.HelloWebService"};
            NanoSpringApplication.run(path);
        } else {
            NanoSpringApplication.run(args);
        }

    }
}
