
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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * A servlet that list of hotels.
 * doGet() method displays an HTML page and allows user to see attractions near specific hotel.
 * doPost() method displays an HTML page and allows user to see attractions near specific hotel.
 */
@SuppressWarnings("serial")
public class hotelListServlet extends BaseServlet {


    private static final String hotelNames_SQL = "SELECT * FROM hotels_master LIMIT ?,10";
    private static final String hotelNames_bycity_SQL = "SELECT * FROM hotels_master WHERE hotel_city= ? LIMIT ?,10";
    private static final String hotelNames_bystate_SQL = "SELECT * FROM hotels_master WHERE hotel_state= ? LIMIT ?,10";
    private static final String HOTEL_REVIEWS_SQL = "SELECT AVG(rating) FROM reviews_master WHERE hotel_id = ?";
    private static final String HOTEL_CITY_SQL = "SELECT DISTINCT hotel_city FROM hotels_master";
    private static final String HOTEL_STATE_SQL = "SELECT DISTINCT hotel_state  FROM hotels_master";
    private static final String HOTEL_COUNT_SQL = "SELECT COUNT(hotel_id) FROM hotels_master";
    private static final String hotelNames_bycity_Count_SQL = "SELECT COUNT(hotel_id) FROM hotels_master WHERE hotel_city= ?";
    private static final String hotelNames_bystate_Count_SQL = "SELECT COUNT(hotel_id) FROM hotels_master WHERE hotel_state= ?";


    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;
    /**
     * HOTEL PRINT MAP
     */
    public Map<Integer, ArrayList> map;
    //Status status = Status.OK;
    public String sortType = "";

    public hotelListServlet() {
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

        PrintWriter out = response.getWriter();
        prepareResponse("Hotels", response, request);
        cityStateList(out);

        String error = request.getParameter("error");
        if (error != null) {
            String errorMessage = getStatusMessage(error);
            out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
        }

        try {
            String city = request.getParameter("city");
            String state = request.getParameter("state");

            int intstartq = 0;
            if (request.getParameter("startq") != null) {
                String startq = request.getParameter("startq");
                intstartq = Integer.parseInt(startq);
            }
            if (city != null && !city.isEmpty()) {
                sortType = "city=city";
                displayHotels(out, hotelNames_bycity_SQL, city, "city", intstartq);

            } else if (state != null && !state.isEmpty()) {
                sortType = "state=state";
                displayHotels(out, hotelNames_bystate_SQL, state, "state", intstartq);

            } else {
                displayHotels(out, hotelNames_SQL, "", "", intstartq);

            }
        } catch (NullPointerException e) {
            System.out.println("hotel list servlet");
        }


        finishResponse(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        prepareResponse("Hotels", response, request);
        displayHotels(out, hotelNames_SQL, "", "", 0);
        finishResponse(response);
    }

    public void cityStateList(PrintWriter out) {

        ArrayList citylist = new ArrayList<>();
        ArrayList statelist = new ArrayList<>();

        ResultSet results;
        ResultSet results_rating;

        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(HOTEL_CITY_SQL);) {
                results = statement.executeQuery();

                while (results.next()) {
                    String tempCity = results.getString("hotel_city");
                    citylist.add(tempCity);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(HOTEL_STATE_SQL);) {
                results = statement.executeQuery();

                while (results.next()) {
                    String tempstate = results.getString("hotel_state");
                    statelist.add(tempstate);
                }
            }

            VelocityEngine ve = new VelocityEngine();
            ve.init();
            Template t = ve.getTemplate("WebContent/viewHotelsByCityStateForm.html");
            VelocityContext context = new VelocityContext();
            context.put("citylist", citylist);
            context.put("statelist", statelist);
            t.merge(context, out);


        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Writes and HTML page that displays hotel list
     */
    public void displayHotels(PrintWriter out, String query, String sortType, String sortBy, int startq) {

        assert out != null;
        Status status = Status.ERROR;
        ResultSet results;
        ResultSet results_rating;
        int hotel_id = 0;
        int hotels_count = 1;
        try (Connection connection = db.getConnection();) {
            //status = duplicateUser(connection, username);
            try (PreparedStatement statement = connection.prepareStatement(query);) {
                if (sortType.isEmpty()) {
                    statement.setInt(1, startq);

                } else {
                    statement.setString(1, sortType);
                    statement.setInt(2, startq);

                }
                results = statement.executeQuery();

                while (results.next()) {
                    String hotel_name = results.getString("hotel_name");
                    String hotel_city = results.getString("hotel_city");
                    String hotel_state = results.getString("hotel_state");

                    hotel_id = results.getInt("hotel_id");
                    //========printing average rating
                    try (Connection connection1 = db.getConnection();) {
                        try (PreparedStatement statement1 = connection1.prepareStatement(HOTEL_REVIEWS_SQL);) {
                            statement1.setInt(1, hotel_id);
                            results_rating = statement1.executeQuery();
                            float rating = 0;
                            while (results_rating.next()) {
                                rating = results_rating.getFloat("avg(rating)");
                                map.put(hotel_id, new ArrayList());
                                map.get(hotel_id).add(hotel_name);
                                map.get(hotel_id).add(hotel_city + ", " + hotel_state);

                                if (rating == 0) {

                                    map.get(hotel_id).add("N/A");

                                } else {
                                    map.get(hotel_id).add(rating);

                                }
                            }
                        }

                    }
                }

/**
 * PRINT HOTEL LIST IN HOTELLISTTABLE.HTML
 */
                VelocityEngine ve = new VelocityEngine();
                ve.init();
                Template t = ve.getTemplate("WebContent/hotelListTable.html");
                VelocityContext context = new VelocityContext();
                context.put("map", map);
                t.merge(context, out);
                map.clear();
                status = Status.OK;
            }
            pagination(out, hotel_id, sortType, sortBy);
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }
    }

    public void pagination(PrintWriter out, int hotel_id, String sortName, String sortBy) {

        ResultSet results;
        String reviewCount = "";
        String countQuery = "";
        boolean flag = false;
        if (sortBy.contains("state")) {
            countQuery = hotelNames_bystate_Count_SQL;
            flag = true;
        } else if (sortBy.contains("city")) {
            countQuery = hotelNames_bycity_Count_SQL;
            flag = true;
        } else {
            countQuery = HOTEL_COUNT_SQL;
            flag = false;
        }

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(countQuery);) {
                if (flag == true) {
                    statement.setString(1, sortName);
                }
                results = statement.executeQuery();
                while (results.next()) {

                    reviewCount = results.getString("COUNT(hotel_id)");
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VelocityEngine ve1 = new VelocityEngine();
        ve1.init();
        Template temp = ve1.getTemplate("WebContent/pagination.html");
        VelocityContext contextcount = new VelocityContext();
        double intCount = Integer.parseInt(reviewCount);
        int pageCount = (int) Math.ceil(intCount / 10);
        contextcount.put("pageCount", pageCount);
        contextcount.put("size", 10);
        contextcount.put("url", "/hotels?" + sortBy + "=" + sortName);
        temp.merge(contextcount, out);
    }
}