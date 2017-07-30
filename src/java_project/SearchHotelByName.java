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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * search hotel by name and print that on html
 *
 * @author RUSHABH
 */
public class SearchHotelByName extends BaseServlet {

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to search hotels in database.
     */
    private static final String SEARCH_HOTELS_SQL = "SELECT hotel_name,hotel_id FROM hotels_master WHERE hotel_name LIKE CONCAT('%',?,'%')";

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;
    /**
     * HOTEL PRINT MAP
     */
    public Map<Integer, ArrayList> map;

    //Status status = Status.OK;

    public SearchHotelByName() {
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

    }

    /**
     * get parameter and search hotel and print the result
     */

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        prepareResponse("Reviews", response, request);
        String searchName = request.getParameter("searchName");
        displaySearchResult(searchName, response);
        finishResponse(response);
    }

    public void displaySearchResult(String searchName, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        String hotel_name = "";
        int hotel_id = 0;

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(SEARCH_HOTELS_SQL);) {
                statement.setString(1, searchName);

                ResultSet results = statement.executeQuery();

                results = statement.executeQuery();
                while (results.next()) {

                    hotel_name = results.getString("hotel_name");
                    hotel_id = results.getInt("hotel_id");

                    map.put(hotel_id, new ArrayList());
                    map.get(hotel_id).add(hotel_name);
                }

                VelocityEngine ve = new VelocityEngine();
                ve.init();
                Template t = ve.getTemplate("WebContent/searchHotelListTable.html");
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
