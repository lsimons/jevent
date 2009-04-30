package nl.jicarilla.jevent.model.impl;

import java.util.Date;

import nl.jicarilla.jevent.model.Subscription;
import nl.jicarilla.jevent.model.Queue;

public class SimpleSubscription extends SimpleModelObject implements Subscription {
    private Queue subscriber;
    private Queue subscribed;
    private Date created = new Date();

    public SimpleSubscription(SimpleStore store, Queue subscriber, Queue subscribed, Date created) {
        super(store);
        this.subscriber = subscriber;
        this.subscribed = subscribed;
        this.created = created;
    }

    public Queue getSubscriber() {
        return this.subscriber;
    }
    public Queue getSubscribed() {
        return this.subscribed;
    }
    
    public Date getCreated() {
        return this.created;
    }
}
