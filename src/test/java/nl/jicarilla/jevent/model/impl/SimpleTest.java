package nl.jicarilla.jevent.model.impl;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.testng.Assert.*;

import nl.jicarilla.jevent.model.Queue;
import nl.jicarilla.jevent.model.Event;

public class SimpleTest {
    @Test
    public void simpleIntegrationTest() throws IOException, ClassNotFoundException, InterruptedException {
        File f = new File("/tmp/sqs.ser");
        if(f.exists()) {
            f.delete();
        }
        SimpleQueueService sqs = new SimpleQueueService();
        sqs.save();
        sqs.load();
        
        sqs.newQueue("lsimons");
        sqs.newQueue("gettingthingswrong");
        Queue lsimons = sqs.findByName("lsimons");
        Queue gettingthingswrong = sqs.findByName("gettingthingswrong");
        gettingthingswrong.addSubscriber(lsimons);
        gettingthingswrong.addEvent("foo");
        sqs.schedulePeriodicNotifications();
        Thread.sleep(500);
        Set<Event> events = lsimons.getNotificationsToThisQueue();
        assertEquals(events.size(), 1);
        sqs.cancelPeriodicNotifications();
        
        sqs.save();
        
        sqs = new SimpleQueueService();
        sqs.load();
        lsimons = sqs.findByName("lsimons");
        events = lsimons.getNotificationsToThisQueue();
        assertEquals(events.size(), 1);
    }
}
