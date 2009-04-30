package nl.jicarilla.jevent.model;

import java.io.IOException;
import java.util.Set;

public interface QueueService {
    Queue findByName(String name);
    
    Set<String> listQueues();

    Queue newQueue(String name);
    
    void load() throws IOException, ClassNotFoundException;
    
    void save() throws IOException;
}