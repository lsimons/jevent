package nl.jicarilla.jevent.servlets;

import nl.jicarilla.jevent.model.QueueService;
import nl.jicarilla.jevent.model.impl.SimpleQueueService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.net.URLEncoder;

public class ServletUtil {

    private static SimpleQueueService qs;

    static {
        qs = new SimpleQueueService();
        try {
            qs.load();
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        qs.schedulePeriodicNotifications();
    }
    
    public static QueueService getQueueService() {
        return qs;
    }

    public static String relativeUrl(HttpServletRequest req, String urlPart) throws UnsupportedEncodingException {
        return relativeUrl(req, req.getServletPath(), encodeUrl(urlPart));
    }

    public static String relativeUrl(HttpServletRequest req, String servlet, String urlPart) throws UnsupportedEncodingException {
        return req.getContextPath() + servlet + "/" + encodeUrl(urlPart);
    }

    public static void printFooter(PrintWriter pw) {
        pw.println("</body></html>");
    }

    public static void printServiceLink(HttpServletRequest req, PrintWriter pw) throws UnsupportedEncodingException {
        String location = relativeUrl(req,"");
        pw.println(String.format("<a href=\"%s\">up to queue service</a>", location));
    }

    public static void printHeader(String queueName, PrintWriter pw) {
        String htmlEncodedQueueName = escapeHtml(queueName);
        pw.println("<html><head>");
        pw.println(String.format("<title>Queue %s</title>", htmlEncodedQueueName));
        pw.println("</head><body>");
        pw.println(String.format("<h1>Queue %s</h1>", htmlEncodedQueueName));
    }

    public static void doError(HttpServletResponse res, int status, String title, String message) throws IOException {
        res.setStatus(status);
        PrintWriter pw = getHtmlWriter(res);
        pw.println(
                String.format(
                        "<html><head><title>%s</title></head><body>",
                        escapeHtml(title)
                )
        );
        pw.println(
                String.format(
                        "<p class=\"error\">%s</p>",
                        escapeHtml(message)
                )
        );
        printFooter(pw);
    }

    public static PrintWriter getHtmlWriter(HttpServletResponse res) throws IOException {
        res.setContentType("text/html; charset=UTF-8");
        return res.getWriter();
    }

    public static String encodeUrl(String newQueueName) throws UnsupportedEncodingException {
        return URLEncoder.encode(newQueueName, "UTF-8");
    }

    public static String escapeHtml(String s) {
        // from http://www.owasp.org/index.php/How_to_perform_HTML_entity_encoding_in_Java
        
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                // safe
                b.append(ch);
            } else if (Character.isWhitespace(ch)) {
                // paranoid version: whitespaces are unsafe - escape
                // conversion of (int)ch is naive
                //b.append("&#").append((int) ch).append(";");
                b.append(ch);
            } else if (Character.isISOControl(ch)) {
                // paranoid version:isISOControl which are not isWhitespace removed !
                // do nothing do not include in output !
            } else if (Character.isHighSurrogate(ch)) {
                int codePoint;
                if (i + 1 < s.length() && Character.isSurrogatePair(ch, s.charAt(i + 1))
                        && Character.isDefined(codePoint = (Character.toCodePoint(ch, s.charAt(i + 1))))) {
                    b.append("&#").append(codePoint).append(";");
                } else {
                    // ignore
                }
                i++; //in both ways move forward
            } else if (Character.isLowSurrogate(ch)) {
                // wrong char[] sequence,
                // ignore
                i++; // move forward,do nothing do not include in output !
            } else {
                if (Character.isDefined(ch)) {
                    // paranoid version
                    // the rest is unsafe, including <127 control chars
                    b.append("&#").append((int) ch).append(";");
                }
                //do nothing do not include undefined in output!
            }
        }
        return b.toString();
    }
}
