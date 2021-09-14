package edu.eci.arep.persistence;

import edu.eci.arep.nanospring.components.NanoSpringException;

public interface PersistenceService {
    String getGreeting(String name) throws NanoSpringException;
}
