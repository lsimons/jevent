package nl.jicarilla.jevent.model;

import java.util.Date;

public interface Subscription {
    Queue getSubscriber();
    Queue getSubscribed();
    
    Date getCreated();
}
