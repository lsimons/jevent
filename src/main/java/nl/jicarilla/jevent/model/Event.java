package nl.jicarilla.jevent.model;

import java.util.Date;

public interface Event {
    Date getOccurred();
    String getData();
    Queue getSourceQueue();
}
