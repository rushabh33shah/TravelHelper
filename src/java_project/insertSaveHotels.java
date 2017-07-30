
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
 * A inserts save hotels into database and printing it on the html
 * doGet() delete saved hotels and print
 * <p>
 * doPost() insert saved hotels and print
 */
@SuppressWarnings("serial")
public class insertSaveHotels extends BaseServlet {

    /**
     * Used to show saved hotels from database.
     */
    private static final String SEARCH_HOTELS_SQL = "SELECT hotel_name,saved_hotels.hotel_id FROM saved_hotels,hotels_master WHERE saved_hotels.hotel_id=hotels_master.hotel_id AND saved_hotels.user_id=?";

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;
    public Map<Integer, ArrayList> map;

    public insertSaveHotels() {

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

    /**
     * doGet() delete saved hotels and print
     */

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
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

        if (request.getParameter("delete") != null) {

            dbhandler.deleteSavedHotels(user_id);
        }

        prepareResponse("Reviews", response, request);
        displaySearchResult(user_id, response, request);
        finishResponse(response);

    }

    /**
     * doPost() insert saved hotels and print
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        String username = loggedinUsernameSession(request);
        if (username == null) {

            String url = "/login";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

        String hotelId = request.getParameter("hotel_id");
        HttpSession session = request.getSession();

        int user_id = 0;
        if (username != null) {
            user_id = (int) session.getAttribute("user_id");
            dbhandler.insertSavedHotels(hotelId, user_id);
        } else {
            String url = "/login";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url); // send a get request  (redirect to the same path)
        }

        prepareResponse("Reviews", response, request);
        displaySearchResult(user_id, response, request);
        finishResponse(response);
    }

    /**
     * display saved hotels
     *
     * @param user_id
     * @param response
     * @param request
     * @return
     * @throws IOException
     */
    public String displaySearchResult(int user_id, HttpServletResponse response, HttpServletRequest request) throws IOException {


        PrintWriter out = response.getWriter();

        String hotels_name = "";
        int hotel_id = 0;
        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(SEARCH_HOTELS_SQL);) {
                statement.setInt(1, user_id);

                ResultSet results = statement.executeQuery();

                results = statement.executeQuery();
                out.print("<h3>SAVED HOTELS</h3>");
                while (results.next()) {

                    hotels_name = results.getString("hotel_name");
                    hotel_id = results.getInt("hotel_id");

                    map.put(hotel_id, new ArrayList());
                    map.get(hotel_id).add(hotels_name);
                }

                VelocityEngine ve = new VelocityEngine();
                ve.init();
                Template t = ve.getTemplate("WebContent/savedHotelListTable.html");
                VelocityContext context = new VelocityContext();
                context.put("map", map);
                context.put("user_id", user_id);

                t.merge(context, out);
                map.clear();

            }
        } catch (SQLException e) {
            System.out.println("Exception occured while processing SQL statement:" + e);
        }

        return hotels_name;
    }
}