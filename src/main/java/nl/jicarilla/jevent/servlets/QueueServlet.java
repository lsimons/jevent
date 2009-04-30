package nl.jicarilla.jevent.servlets;

import nl.jicarilla.jevent.model.QueueService;
import nl.jicarilla.jevent.model.Queue;
import nl.jicarilla.jevent.model.Subscription;
import nl.jicarilla.jevent.model.Event;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Set;

public class QueueServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    private QueueService qs;
    {
        qs = ServletUtil.getQueueService();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();
        if(pathInfo == null || "/".equals(pathInfo)) {
            doListQueues(req, res);
        } else {
            String queueName = pathInfo.substring(1);
            doGetQueue(queueName, req, res);
        }
    }

    private void doListQueues(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Set<String> names = qs.listQueues();

        PrintWriter pw = ServletUtil.getHtmlWriter(res);
        //
        // header
        //
        pw.println("<html><head><title>Queue Service Index Page</title></head><body>");
        //
        // queue list
        //
        pw.println("<h2>List of queues</h2>");
        pw.println("<ul>");
        for (String name : names) {
            pw.println(
                    String.format(
                            "<li><a href=\"%s\">%s</a></li>",
                            ServletUtil.relativeUrl(req,name),
                            ServletUtil.escapeHtml(name)
                    )
            );
        }
        pw.println("</ul>");

        //
        // form to add a queue
        //
        pw.println("<h2>Add a new queue</h2>");
        pw.println("<form method=\"POST\" action=\"\">");
        pw.println("<input type=\"text\" name=\"name\" />");
        pw.println("<input type=\"submit\" />");
        pw.println("</form>");

        ServletUtil.printFooter(pw);
    }

    private void doGetQueue(String queueName, HttpServletRequest req, HttpServletResponse res) throws IOException {
        Queue q = qs.findByName(queueName);
        if(q == null) {
            ServletUtil.doError(res, 404, "Queue Not Found", String.format("Cannot find queue with name '%s'", queueName));
        } else {
            PrintWriter pw = ServletUtil.getHtmlWriter(res);
            //
            // header
            //
            ServletUtil.printHeader(queueName, pw);
            ServletUtil.printServiceLink(req, pw);

            //
            // form to post an event
            //
            pw.println("<h2>Add a new event to this queue</h2>");
            pw.println("<form method=\"POST\" action=\"\">");
            pw.println("<input type=\"text\" name=\"data\" />");
            pw.println("<input type=\"submit\" />");
            pw.println("</form>");

            //
            // form to add a subscriber
            //
            pw.println("<h2>Add a new subscriber to this queue</h2>");
            pw.println(String.format("<form method=\"POST\" action=\"%s\">",
                    ServletUtil.relativeUrl(req,"/subscription",queueName)));
            pw.println("<input type=\"text\" name=\"subscriber\" />");
            pw.println("<input type=\"submit\" />");
            pw.println("</form>");

            //
            // subscribers
            //
            pw.println("<h2>Subscribers:</h2>");
            pw.println("<ul class=\"subscribers\">");
            for (Subscription s : q.getSubscribers()) {
                String subscriberName = s.getSubscriber().getName(); 
                pw.println(
                        String.format(
                                "<li><a href=\"%s\">%s</a></li>",
                                ServletUtil.relativeUrl(req,subscriberName),
                                ServletUtil.escapeHtml(subscriberName)
                        )
                );
            }
            pw.println("</ul>");

            //
            // subscribed
            //
            pw.println("<h2>Subscribed to:</h2>");
            pw.println("<ul class=\"subscribed\">");
            for (Subscription s : q.getSubscribed()) {
                String subscribedName = s.getSubscribed().getName();
                pw.println(
                        String.format(
                                "<li><a href=\"%s\">%s</a></li>",
                                ServletUtil.relativeUrl(req,subscribedName),
                                ServletUtil.escapeHtml(subscribedName)
                        )
                );
            }
            pw.println("</ul>");

            //
            // events
            //
            pw.println("<h2>Events:</h2>");
            pw.println("<ul class=\"events\">");
            for (Event e : q.getEventsFromThisQueue()) {
                pw.println(
                        String.format(
                                "<li>%s</li>",
                                ServletUtil.escapeHtml(e.getData())
                        )
                );
            }
            pw.println("</ul>");

            //
            // notifications
            //
            pw.println("<h2>Notifications:</h2>");
            pw.println("<ul class=\"notifications\">");
            for (Event e : q.getNotificationsToThisQueue()) {
                String sourceName = e.getSourceQueue().getName();
                pw.println(
                        String.format(
                                "<li><a href=\"%s\">%s</a>: %s</li>",
                                ServletUtil.relativeUrl(req,sourceName),
                                ServletUtil.escapeHtml(sourceName),
                                ServletUtil.escapeHtml(e.getData())
                        )
                );
            }
            pw.println("</ul>");
            ServletUtil.printFooter(pw);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            doNewQueue(req, res);
        } else {
            String queueName = pathInfo.substring(1);
            doNewEvent(queueName, req, res);
        }
    }

    private void doNewQueue(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String newQueueName = req.getParameter("name");
        if(newQueueName == null) {
            ServletUtil.doError(res, 400, "Missing queue name", "Must supply a queue name to create a new queue");
        } else {
            qs.newQueue(newQueueName);
            qs.save();
            res.setStatus(200);
            String location = ServletUtil.relativeUrl(req, newQueueName);
            res.setHeader("Location", location);

            PrintWriter pw = ServletUtil.getHtmlWriter(res);
            ServletUtil.printHeader(newQueueName + " created", pw);
            pw.println(String.format(
                    "<p>Go to the new queue: <a class=\"newQueue\" href=\"%s\">%s</a></p>",
                    location,
                    ServletUtil.escapeHtml(newQueueName)));
            ServletUtil.printFooter(pw);
        }
    }

    private void doNewEvent(String queueName, HttpServletRequest req, HttpServletResponse res) throws IOException {
        String newEventData = req.getParameter("data");
        if(newEventData == null) {
            ServletUtil.doError(res, 400, "Missing event data", "Must supply event data to create a new event");
        } else {
            Queue q = qs.findByName(queueName);
            if (q == null) {
                ServletUtil.doError(res, 404, "Queue Not Found", String.format("Cannot find queue with name '%s'", queueName));
            } else {
                q.addEvent(newEventData);
                qs.save();
                res.setStatus(200);
                PrintWriter pw = ServletUtil.getHtmlWriter(res);
                ServletUtil.printHeader(queueName + " received a new event", pw);
                pw.println(String.format(
                        "<p>Go back to queue: <a href=\"%s\">%s</a></p>",
                        ServletUtil.relativeUrl(req,queueName),
                        ServletUtil.escapeHtml(queueName)));
                ServletUtil.printFooter(pw);
            }
        }
    }

}
