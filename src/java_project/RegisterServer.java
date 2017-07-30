
package java_project;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Demonstrates how to use Jetty, servlets and JDBC for user registration. This is a
 * simplified example, and **NOT** secure.
 * Modified from the example by Prof. Engle.
 */
public class RegisterServer {
    private static int PORT = 8080;

    public static void main(String[] args) {

        Server server = new Server(PORT);
        WebAppContext ctx = new WebAppContext();
        ctx.setResourceBase("./src/java_project");
        ctx.addServlet(LoginServlet.class, "/login");

        ServletContextHandler servhandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        //default starting page
        servhandler.addServlet(LoginServlet.class, "/");

        servhandler.addServlet(LoginServlet.class, "/login");
        servhandler.addServlet(hotelListServlet.class, "/hotels");
        servhandler.addServlet(reviewListServlet.class, "/reviews");
        servhandler.addServlet(RegistrationServlet.class, "/register");
        servhandler.addServlet(touristAttractionListServlet.class, "/touristattraction");
        servhandler.addServlet(userReviewListServlet.class, "/userreviews");
        servhandler.addServlet(reviewUserRegister.class, "/reviewUserRegister");
        servhandler.addServlet(userReviewUpdate.class, "/userReviewUpdate");
        servhandler.addServlet(insertUserReview.class, "/insertUserReview");
        servhandler.addServlet(SearchHotelByName.class, "/searchHotelByName");
        servhandler.addServlet(insertSaveHotels.class, "/saveHotel");
        servhandler.addServlet(expediaLink.class, "/expedia");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{servhandler, ctx});

        server.setHandler(handlers);
        try {
            server.start();
            server.join();

        } catch (Exception ex) {
            System.out.println("An exception occurred while running the server. ");
            System.exit(-1);
        }
    }
}