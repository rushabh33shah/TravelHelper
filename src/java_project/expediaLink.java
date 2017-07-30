
package java_project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * A servlet that shows reviews of specific hotel.
 * doGet() method displays an HTML page with hotel info., reviews for that hotel, and form to enter new reviews
 * <p>
 * doPost() processes the form:  and adds review info to the database.
 */
@SuppressWarnings("serial")
public class expediaLink extends BaseServlet {

    /**
     * Used to show saved hotels from database.
     */
    private static final String SEARCH_HOTELS_SQL = "SELECT hotel_name,expedia_master.expedia_hotel FROM expedia_master,hotels_master WHERE expedia_master.expedia_hotel=hotels_master.hotel_id AND expedia_master.user_id=?";

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public Map<Integer, ArrayList> map;

    public expediaLink() {
        Status status = Status.OK;

        map = new HashMap<Integer, ArrayList>();

        try {
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? status : Status.CONNECTION_FAILED;
        } catch (FileNotFoundException e) {
            status = Status.MISSING_CONFIG;
        } catch (IOException e) {
            status = Status.MISSING_VALUES;
        }

        if (status != Status.OK) {
            System.out.println("Error while obtaining a connection to the database: " + status);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = loggedinUsernameSession(request);
        if (username == null) {

            String url = "/login";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

        String hotelId = request.getParameter("expedia_hotel");
        HttpSession session = request.getSession();
        int user_id = (int) session.getAttribute("user_id");

        if (request.getParameter("expedia_hotel") != null) {
            if (username != null) {
                dbhandler.insertExpediaHotels(user_id, hotelId);
                String url = "https://www.expedia.com/h" + hotelId + ".Hotel-Information";
                url = response.encodeRedirectURL(url);
                response.sendRedirect(url);

            } else {
                // send a get request  (redirect to the same path)
            }
        } else {

            prepareResponse("Expedia History", response, request);
            displaySearchResult(user_id, response, request);
            finishResponse(response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = loggedinUsernameSession(request);
        if (username == null) {

            String url = "/login";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

        HttpSession session = request.getSession();
        int user_id = 0;
        user_id = (int) session.getAttribute("user_id");

        String delete_status = request.getParameter("delete");

        if (delete_status.contains("yes")) {
            dbhandler.deleteExpediaHotels(user_id);
        }


        prepareResponse("Reviews", response, request);
        displaySearchResult(user_id, response, request);
        finishResponse(response);

    }

    public void displaySearchResult(int user_id, HttpServletResponse response, HttpServletRequest request) throws IOException {

        PrintWriter out = response.getWriter();
        int expedia_hotel = 0;
        String hotels_name = "";
        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(SEARCH_HOTELS_SQL);) {
                statement.setInt(1, user_id);

                ResultSet results = statement.executeQuery();

                results = statement.executeQuery();
                out.print("<h3>Visited Expedia Links</h3>");
                while (results.next()) {

                    hotels_name = results.getString("hotel_name");
                    expedia_hotel = results.getInt("expedia_hotel");

                    map.put(expedia_hotel, new ArrayList());
                    map.get(expedia_hotel).add(hotels_name);

                }

                VelocityEngine ve = new VelocityEngine();
                ve.init();
                Template t = ve.getTemplate("WebContent/expediaHistoryHotelListTable.html");
                VelocityContext context = new VelocityContext();
                context.put("map", map);
                t.merge(context, out);
                map.clear();
            }
        } catch (SQLException e) {
            System.out.println("Exception occured while processing SQL statement:" + e);
        }

    }

}