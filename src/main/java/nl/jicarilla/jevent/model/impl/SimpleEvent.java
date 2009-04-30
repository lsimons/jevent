package nl.jicarilla.jevent.model.impl;

import nl.jicarilla.jevent.model.Event;
import nl.jicarilla.jevent.model.Queue;

import java.util.Date;

public class SimpleEvent extends SimpleModelObject implements Event {
    private Date occurred;
    private String data;
    private Queue sourceQueue;
    private boolean notificationsComplete = false;

    public SimpleEvent(SimpleStore store, Date occurred, String data, Queue sourceQueue) {
        super(store);
        this.occurred = occurred;
        this.data = data;
        this.sourceQueue = sourceQueue;
    }

    public Date getOccurred() {
        return this.occurred;
    }

    public String getData() {
        return this.data;
    }

    public Queue getSourceQueue() {
        return this.sourceQueue;
    }

    public boolean areNotificationsComplete() {
        return notificationsComplete;
    }

    public void notificationsCompleted() {
        this.notificationsComplete = true;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleEvent)) return false;

        SimpleEvent event = (SimpleEvent) o;

        if (!data.equals(event.data)) return false;
        if (!occurred.equals(event.occurred)) return false;
        if (!sourceQueue.equals(event.sourceQueue)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = occurred.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + sourceQueue.hashCode();
        return result;
    }
}
