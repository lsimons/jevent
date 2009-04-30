package nl.jicarilla.jevent.servlets;

import nl.jicarilla.jevent.model.QueueService;
import nl.jicarilla.jevent.model.Queue;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

public class SubscriptionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private QueueService qs;
    {
        qs = ServletUtil.getQueueService();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            ServletUtil.doError(
                    res,
                    400,
                    "Bad url for subscription request",
                    "Must put source queue name in path info");
        } else {
            String queueName = pathInfo.substring(1);
            doSubscribe(queueName, req, res);
        }
    }

    private void doSubscribe(String queueName, HttpServletRequest req, HttpServletResponse res) throws IOException {
        Queue q = qs.findByName(queueName);
        if(q == null) {
            ServletUtil.doError(res, 404, "Queue Not Found", String.format("Cannot find queue with name '%s'", queueName));
        } else {
            String subscriber = req.getParameter("subscriber");
            if (subscriber == null) {
                ServletUtil.doError(res, 400, "Missing subscriber", "Must supply subscriber to create a subscription");
            } else {
                q.addSubscriber(subscriber);
                qs.save();
                res.setStatus(200);
                PrintWriter pw = ServletUtil.getHtmlWriter(res);
                ServletUtil.printHeader(queueName + " received a new subscriber", pw);
                pw.println(String.format(
                        "<p>Go back to queue: <a href=\"%s\">%s</a></p>",
                        ServletUtil.relativeUrl(req, "/queue", queueName),
                        ServletUtil.escapeHtml(queueName)));
                ServletUtil.printFooter(pw);
            }
        }
    }

}
