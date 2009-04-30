package nl.jicarilla.jevent.model;

import java.util.Set;
import java.util.Date;

public interface Queue {
    String getName();
    
    Set<Subscription> getSubscribers();
    Set<Subscription> getSubscribed();
    
    Subscription addSubscriber(Queue subscriber);
    Subscription addSubscriber(String subscriberName);
    void removeSubscriber(Queue subscriber);
    void removeSubscriber(String subscriberName);
    
    Set<Event> getEventsFromThisQueue();
    Set<Event> getNotificationsToThisQueue();
    
    void addEvent(String data);
    void addEvent(Date occurred, String data);
}