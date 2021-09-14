package edu.eci.arep.demoService;

import edu.eci.arep.nanospring.components.NanoSpringException;
import edu.eci.arep.nanospring.components.PathVariable;
import edu.eci.arep.nanospring.components.RequestMapping;
import edu.eci.arep.persistence.PersistenceService;
import edu.eci.arep.persistence.PersistenceServiceImpl;

/**
 */
public class WebService {

    private static final PersistenceService persistenceService = new PersistenceServiceImpl();

    /**
     * Obtain a predetermined value as a response.
     * @throws edu.eci.arep.nanospring.components.NanoSpringException
     */
    @RequestMapping(value = "/hello")
    public static String index(@PathVariable String value) throws NanoSpringException {
        return persistenceService.getGreeting(value) + " desde NanoSpring";
    }
}
