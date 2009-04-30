package nl.jicarilla.jevent.model.impl;

import nl.jicarilla.jevent.model.QueueService;
import nl.jicarilla.jevent.model.Queue;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleQueueService implements QueueService, Externalizable {
    private SimpleStore store = new SimpleStore(this);
    private Timer t;
    private TimerTask task;

    public SimpleQueueService() {
    }

    public Queue findByName(String name) {
        Queue result = null;

        for (Queue q : store.queues) {
            if (q.getName().equals(name)) {
                result = q;
                break;
            }
        }
        
        return result;
    }

    public Set<String> listQueues() {
        Set<String> result = new HashSet<String>();
        for (Queue q : store.queues) {
            result.add(q.getName());
        }
        return Collections.unmodifiableSet(result);
    }

    public Queue newQueue(String name) {
        SimpleQueue sq = new SimpleQueue(this.store, name, this);
        sq.save();
        return sq;
    }

    public void load() throws IOException, ClassNotFoundException {
        store.load("/tmp/sqs.ser");
    }
    
    public void save() throws IOException {
        store.save("/tmp/sqs.ser");
    }

    public void writeExternal(ObjectOutput output) throws IOException {
        // nothing!
    }

    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        // nothing!
    }
    
    public void schedulePeriodicNotifications() {
        if(t != null) {
            return;
        }
        t = new Timer();
        task = new TimerTask() {
            public void run() {
                store.doNotifications();
            }
        };
        t.scheduleAtFixedRate(task, 100, 1000 * 30);
    }
    
    public void cancelPeriodicNotifications() {
        task.cancel();
    }
}