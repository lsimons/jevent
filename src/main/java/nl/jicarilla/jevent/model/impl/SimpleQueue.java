package nl.jicarilla.jevent.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

import nl.jicarilla.jevent.model.Event;
import nl.jicarilla.jevent.model.Queue;
import nl.jicarilla.jevent.model.Subscription;
import nl.jicarilla.jevent.model.QueueService;

public class SimpleQueue extends SimpleModelObject implements Queue {
	private String name;
    
    private QueueService queueService;

    public SimpleQueue(SimpleStore store, String name, QueueService queueService) {
        super(store);
        this.name = name;
        this.queueService = queueService;
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleQueue)) return false;

        SimpleQueue queue = (SimpleQueue) o;

        if (!name.equals(queue.name)) return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public Set<Event> getEventsFromThisQueue() {
        Set<Event> result = new HashSet<Event>();
        for(Event e : this.store.events) {
            if(e.getSourceQueue().equals(this)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableSet(result);
	}

	public Set<Event> getNotificationsToThisQueue() {
        Set<Event> result = new HashSet<Event>();
        for(SimpleNotification n : this.store.notifications) {
            if(n.getTargetQueue().equals(this)) {
                result.add(n.getEvent());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public Set<Subscription> getSubscribers() {
        Set<Subscription> result = new HashSet<Subscription>();
        for(Subscription s : this.store.subscriptions) {
            if(s.getSubscribed().equals(this)) {
                result.add(s);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public Set<Subscription> getSubscribed() {
        Set<Subscription> result = new HashSet<Subscription>();
        for(Subscription s: this.store.subscriptions) {
            if(s.getSubscriber().equals(this)) {
                result.add(s);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public void addEvent(String data) {
        addEvent(new Date(), data);
    }

    public void addEvent(Date occurred, String data) {
        SimpleEvent e = new SimpleEvent(this.store, occurred, data, this);
        e.save();
    }

    public Subscription addSubscriber(Queue subscriber) {
        SimpleSubscription s = new SimpleSubscription(this.store, subscriber, this, new Date());
		s.save();
		return s;
	}

	public Subscription addSubscriber(String subscriberName) {
        Queue subscriber = queueService.findByName(subscriberName);
        return addSubscriber(subscriber);
    }

	public void removeSubscriber(Queue subscriber) {
        for(SimpleSubscription s : this.store.subscriptions) {
            if(!s.getSubscribed().equals(this)) continue;
            if (!s.getSubscriber().equals(subscriber)) continue;
            s.delete();
            break;
        }
    }

	public void removeSubscriber(String subscriberName) {
        Queue subscriber = queueService.findByName(subscriberName);
        removeSubscriber(subscriber);
    }
	
}
