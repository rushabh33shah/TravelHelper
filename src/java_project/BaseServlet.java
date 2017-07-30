
package java_project;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Provides base functionality to all servlets in this example. Original author:
 * Prof. Engle
 *
 * @see RegisterServer
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {

    /**
     * method to prepare response, starting html and body tag
     *
     * @param title    title of the page
     * @param response HttpServletResponse
     * @param request  HttpServletRequest
     */

    protected void prepareResponse(String title, HttpServletResponse response, HttpServletRequest request) {
        try {
            PrintWriter writer = response.getWriter();

            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.print("<style>table {width: 100%;border-collapse: collapse;margin-bottom: 1%}th{height: 50px;}</style>");
            VelocityEngine ve = new VelocityEngine();
            ve.init();
            Template head = ve.getTemplate("WebContent/header.html");
            VelocityContext headercontext = new VelocityContext();
            headercontext.put("last_login", lastLoginSession(request));
            head.merge(headercontext, writer);

            String username = loggedinUsernameSession(request);
            if (username != null) {

                Template t = ve.getTemplate("WebContent/lastLogin.html");
                VelocityContext context = new VelocityContext();
                context.put("last_login", lastLoginSession(request));
                t.merge(context, writer);
            }

            writer.println("");
            writer.println("</ul>");
            ve.init();
            Template t1 = ve.getTemplate("WebContent/menuBar.html");
            VelocityContext context1 = new VelocityContext();
            context1.put("username", username);

            t1.merge(context1, writer);

        } catch (IOException ex) {
            System.out.println("IOException while preparing the response: " + ex);
            return;
        }
    }

    /**
     * finishing html response, closing body and html tag
     *
     * @param response HttpServletResponse response
     */
    protected void finishResponse(HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();

            writer.println();
            //container div from prepare response ends here
            writer.println("</div>");
            writer.println("</body>");
            writer.println("</html>");
            writer.flush();

            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
        } catch (IOException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    protected String getStatusMessage(String errorName) {
        Status status = null;

        try {
            status = Status.valueOf(errorName);
        } catch (Exception ex) {
            status = Status.ERROR;
        }

        return status.toString();
    }

    protected String getStatusMessage(int code) {
        Status status = null;

        try {
            status = Status.values()[code];
        } catch (Exception ex) {
            status = Status.ERROR;
        }

        return status.toString();
    }

    /**
     * method to get last login of loggedin user
     *
     * @param request HttpServletRequest
     * @return username
     */

    public String lastLoginSession(HttpServletRequest request) {

        HttpSession session = request.getSession();
        String last_login = "";
        if (session != null) {
            last_login = (String) session.getAttribute("last_login");

            // System.out.println("Hello, "+username+" !");
        }

        return last_login;
    }

    /**
     * method to get username of loggedin user
     *
     * @param request HttpServletRequest
     * @return username
     */

    public String loggedinUsernameSession(HttpServletRequest request) {

        HttpSession session = request.getSession();
        String username = "";
        if (session != null) {
            username = (String) session.getAttribute("username");

            // System.out.println("Hello, "+username+" !");
        }
        return username;
    }

    /**
     * method to get userid of loggedin user
     *
     * @param request HttpServletRequest
     * @return userid
     */

    public int get_loggedin_user_idSession(HttpServletRequest request) {

        HttpSession session = request.getSession();
        int user_id = 0;
        if (session != null) {
            user_id = (int) session.getAttribute("user_id");

        }
        return user_id;
    }
}