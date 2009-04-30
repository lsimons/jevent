package nl.jicarilla.jevent.model.impl;

import nl.jicarilla.jevent.model.Event;
import nl.jicarilla.jevent.model.Queue;

class SimpleNotification extends SimpleModelObject {
    private Event event;
    private Queue targetQueue;

    SimpleNotification(SimpleStore store, Event event, Queue targetQueue) {
        super(store);
        this.event = event;
        this.targetQueue = targetQueue;
    }

    public Event getEvent() {
        return event;
    }

    public Queue getTargetQueue() {
        return targetQueue;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleNotification)) return false;

        SimpleNotification that = (SimpleNotification) o;

        if (!event.equals(that.event)) return false;
        if (!targetQueue.equals(that.targetQueue)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = event.hashCode();
        result = 31 * result + targetQueue.hashCode();
        return result;
    }
}
