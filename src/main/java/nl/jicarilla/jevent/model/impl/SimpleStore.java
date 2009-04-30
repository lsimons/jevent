package nl.jicarilla.jevent.model.impl;

import nl.jicarilla.jevent.model.QueueService;
import nl.jicarilla.jevent.model.Subscription;
import nl.jicarilla.jevent.model.Queue;

import java.util.HashSet;
import java.util.Set;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;

class SimpleStore implements Externalizable {
    Set<SimpleQueue> queues = new HashSet<SimpleQueue>();
    Set<SimpleSubscription> subscriptions = new HashSet<SimpleSubscription>();
    Set<SimpleEvent> events = new HashSet<SimpleEvent>();
    Set<SimpleNotification> notifications = new HashSet<SimpleNotification>();
    
    private QueueService queueService;

    public SimpleStore(QueueService queueService) {
        this.queueService = queueService;
    }

    public SimpleStore() { // for deserialization hacks
    }

    public void create(SimpleModelObject object) {
        if(object instanceof SimpleQueue) {
            queues.add((SimpleQueue) object);
        } else if(object instanceof SimpleSubscription) {
            subscriptions.add((SimpleSubscription) object);
        } else if (object instanceof SimpleEvent) {
            events.add((SimpleEvent) object);
        } else if (object instanceof SimpleNotification) {
            notifications.add((SimpleNotification) object);
        } else {
            throw new RuntimeException("unsupported object type");
        }
    }

    public void update(SimpleModelObject object) {
        // in this simple case, all the in-memory objects are used-by-reference so auto-updated
    }

    public void delete(SimpleModelObject object) {
        if (object instanceof SimpleQueue) {
            queues.remove(object);
            SimpleQueue q = (SimpleQueue)object;
            for(SimpleSubscription s : subscriptions) {
                if(s.getSubscribed().getName().equals(q.getName())) {
                    s.delete();
                }
            }
            for (SimpleEvent e : events) {
                if(e == object) {
                    e.delete();
                }
            }
        } else if (object instanceof SimpleSubscription) {
            subscriptions.remove(object);
        } else if (object instanceof SimpleEvent) {
            events.remove(object);
            for(SimpleNotification n : notifications) {
                if(n.getEvent() == object) {
                    n.delete();
                }
            }
        } else if (object instanceof SimpleNotification) {
            notifications.remove(object);
        } else {
            throw new RuntimeException("unsupported object type");
        }
    }
    
    public synchronized void doNotifications() {
        for(SimpleEvent e : events) {
            if(e.areNotificationsComplete()) {
                continue;
            }
            Queue source = e.getSourceQueue();
            for(Subscription s : source.getSubscribers()) {
                Queue subscriber = s.getSubscriber();
                SimpleNotification n = new SimpleNotification(this, e, subscriber);
                n.save();
            }
            e.notificationsCompleted();
        }
    }

    public synchronized void load(String fileName) throws IOException,ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        load(fis);
    }

    public synchronized void load(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        try {
            Object object;
            while((object = ois.readObject()) != null) {
                if(object instanceof SimpleQueue) {
                    SimpleQueue oldQueue = (SimpleQueue)object;
                    SimpleQueue newQueue = new SimpleQueue(this, oldQueue.getName(), queueService);
                    newQueue.save();
                } else if(object instanceof SimpleSubscription) {
                    SimpleSubscription oldSubscription = (SimpleSubscription)object;
                    SimpleSubscription newSubscription = new SimpleSubscription(
                            this,
                            queueService.findByName(oldSubscription.getSubscriber().getName()),
                            queueService.findByName(oldSubscription.getSubscribed().getName()),
                            oldSubscription.getCreated()
                            );
                    newSubscription.save();
                } else if(object instanceof SimpleEvent) {
                    SimpleEvent oldEvent = (SimpleEvent) object;
                    SimpleEvent newEvent = new SimpleEvent(this, oldEvent.getOccurred(), oldEvent.getData(),
                            queueService.findByName(oldEvent.getSourceQueue().getName()));
                    newEvent.save();
                } else if(object instanceof SimpleNotification) {
                    SimpleNotification oldNotification = (SimpleNotification) object;
                    SimpleEvent oldEvent = (SimpleEvent) oldNotification.getEvent();
                    SimpleEvent newEvent = null;
                    for(SimpleEvent event : events) {
                        if(!event.equals(oldEvent)) continue;
                        newEvent = event;
                        break;
                    }
                    SimpleNotification newNotification = new SimpleNotification(this, newEvent,
                            queueService.findByName(oldNotification.getTargetQueue().getName()));
                    newNotification.save();
                }
            }
        } catch (EOFException e) {
            // done
        }
    }
    
    public synchronized void save(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        save(fos);
    }
    
    public synchronized void save(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        for(Object o : queues) {
            oos.writeObject(o);
        }
        for (Object o : subscriptions) {
            oos.writeObject(o);
        }
        for (Object o : events) {
            oos.writeObject(o);
        }
        for (Object o : notifications) {
            oos.writeObject(o);
        }
    }

    public void writeExternal(ObjectOutput output) throws IOException {
        // nothing!
    }

    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        // nothing!
    }
}
