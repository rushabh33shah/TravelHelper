
package java_project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
public class reviewListServlet extends BaseServlet {

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to get all the reviews of hotel.
     */
    private static final String HOTEL_REVIEWS_SQL = "SELECT * FROM hotels_master, reviews_master WHERE reviews_master.hotel_id = ? AND reviews_master.hotel_id = hotels_master.hotel_id LIMIT ?,5 ";
    /**
     * Used to get all the reviews sorted by date.
     */
    private static final String HOTEL_REVIEWS_DATE_SQL = "SELECT * FROM hotels_master, reviews_master WHERE reviews_master.hotel_id = ? AND reviews_master.hotel_id = hotels_master.hotel_id ORDER BY date DESC LIMIT ?,5";
    /**
     * Used to get all the reviews sorted by rating.
     */
    private static final String HOTEL_REVIEWS_RATING_SQL = "SELECT * FROM hotels_master, reviews_master WHERE reviews_master.hotel_id = ? AND reviews_master.hotel_id = hotels_master.hotel_id ORDER BY rating DESC LIMIT ?,5";

    /**
     * print hotel info
     */
    private static final String hotelNames_SQL = "SELECT * FROM hotels_master WHERE hotel_id = ?";

    private static final String HOTEL_REVIEWSCOUNT_SQL = "SELECT COUNT(review_id) FROM reviews_master WHERE hotel_id = ?";

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public Map<String, ArrayList> map;

    public reviewListServlet() {
        Status status = Status.OK;
        map = new HashMap<String, ArrayList>();
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
        prepareResponse("Reviews", response, request);
        String error = request.getParameter("error");

        if (error != null) {
            String errorMessage = getStatusMessage(error);
            out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
        }

        String hotel_id = request.getParameter("hotel_id");
        int new_hotel_id = Integer.parseInt(hotel_id);
        int new_startq = 0;
        if (request.getParameter("startq") != null) {
            String startq = request.getParameter("startq");
            new_startq = Integer.parseInt(startq);
        }

        displayHotels(out, new_hotel_id);
        displayHotelReviews(out, new_hotel_id, HOTEL_REVIEWS_SQL, new_startq, request);

        HttpSession session = request.getSession();
        String username = loggedinUsernameSession(request);

        int user_id = 0;
        if (username != null) {
            user_id = (int) session.getAttribute("user_id");
            newUserReview(out, hotel_id);
        }
        finishResponse(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        prepareResponse("Reviews", response, request);

        String hotel_id = request.getParameter("hotel_id");
        int new_hotel_id = Integer.parseInt(hotel_id);
        String sort = request.getParameter("sort");

        if (sort.contains("date")) {
            displayHotels(out, new_hotel_id);
            displayHotelReviews(out, new_hotel_id, HOTEL_REVIEWS_DATE_SQL, 0, request);
        } else if (sort.contains("rating")) {
            displayHotels(out, new_hotel_id);
            displayHotelReviews(out, new_hotel_id, HOTEL_REVIEWS_RATING_SQL, 0, request);
        } else {
            displayHotels(out, new_hotel_id);
            displayHotelReviews(out, new_hotel_id, HOTEL_REVIEWS_SQL, 0, request);
        }
        HttpSession session = request.getSession();
        String username = loggedinUsernameSession(request);

        int user_id = 0;
        if (username != null) {
            user_id = (int) session.getAttribute("user_id");
            newUserReview(out, hotel_id);
        }
        finishResponse(response);
    }

    public void pagination(PrintWriter out, int hotel_id) {

        ResultSet results;
        String reviewCount = "";

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(HOTEL_REVIEWSCOUNT_SQL);) {
                statement.setInt(1, hotel_id);

                results = statement.executeQuery();
                while (results.next()) {

                    reviewCount = results.getString("COUNT(review_id)");
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
        int pageCount = (int) Math.ceil(intCount / 5);
        contextcount.put("pageCount", pageCount);
        contextcount.put("hotel_id", hotel_id);
        contextcount.put("size", 5);
        contextcount.put("url", "/reviews?hotel_id=" + hotel_id + "&");

        temp.merge(contextcount, out);

    }

    /**
     * Writes an HTML  that shows reviews of the hotel
     */
    public void displayHotelReviews(PrintWriter out, int hotel_id, String query, int startq, HttpServletRequest request) {
        assert out != null;

        VelocityEngine ve = new VelocityEngine();
        ve.init();
        Template t = ve.getTemplate("WebContent/sortReviewDropDown.html");
        VelocityContext context = new VelocityContext();
        context.put("hotel_id", hotel_id);
        t.merge(context, out);


        int new_hotel_id = hotel_id;
        ResultSet results;
        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(query);) {
                statement.setInt(1, new_hotel_id);
                statement.setInt(2, startq);

                results = statement.executeQuery();

                while (results.next()) {
                    String review_id = results.getString("review_id");
                    String username = results.getString("username");
                    String title = results.getString("title");
                    String text = results.getString("text");
                    String date = results.getString("date");
                    String isrecom = results.getString("isrecom");
                    int rating = results.getInt("rating");
                    //=============================reviews printing from here =======================
                    map.put(review_id, new ArrayList());
                    map.get(review_id).add(username);
                    map.get(review_id).add(title);
                    map.get(review_id).add(text);
                    map.get(review_id).add(date);
                    map.get(review_id).add(rating);
                    map.get(review_id).add(isrecom);

                }
                VelocityEngine ve1 = new VelocityEngine();
                ve1.init();
                Template t1 = ve1.getTemplate("WebContent/reviewListTable.html");
                VelocityContext context1 = new VelocityContext();
                context1.put("map", map);
                t1.merge(context1, out);
                map.clear();
            }

            pagination(out, hotel_id);

        } catch (SQLException ex) {
            System.out.println("Error while connecting to the database: " + ex);
        }

    }

    /**
     * Writes an HTML  that shows hotel
     */

    public void displayHotels(PrintWriter out, int hotel_id) {

        assert out != null;
        Status status = Status.ERROR;
        ResultSet results;

        float lat = 0, lng = 0;

        try (Connection connection = db.getConnection();) {
//			status = duplicateUser(connection, username);
            try (PreparedStatement statement = connection.prepareStatement(hotelNames_SQL);) {
                statement.setInt(1, hotel_id);
                results = statement.executeQuery();
                while (results.next()) {

                    String hotel_name = results.getString("hotel_name");
                    String hotel_address = results.getString("hotel_address");
                    String hotel_city = results.getString("hotel_city");
                    String hotel_state = results.getString("hotel_state");
                    String hotel_country = results.getString("hotel_country");
                    lat = results.getFloat("latitude");
                    lng = results.getFloat("longitude");

                    String address = hotel_address + ",<br/>" + hotel_city + ",<br/>" + hotel_state + ",<br/>" + hotel_country;

                    //google map
                    VelocityEngine ve1 = new VelocityEngine();
                    ve1.init();
                    Template t1 = ve1.getTemplate("WebContent/hotelDetails-ReviewListPage.html");
                    VelocityContext context1 = new VelocityContext();
                    context1.put("hotel_name", hotel_name);
                    context1.put("hotel_id", hotel_id);
                    context1.put("address", address);
                    t1.merge(context1, out);
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    Template t = ve.getTemplate("WebContent/googleMap.html");
                    VelocityContext context = new VelocityContext();
                    context.put("lat", lat);
                    context.put("lng", lng);
                    t.merge(context, out);

                }

                status = Status.OK;
            }


        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }
    }

    /**
     * Writes an HTML form and takes new reviews from the user
     */
    public void newUserReview(PrintWriter out, String hotelId) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date1 = new Date();
        String date = dateFormat.format(date1);

        VelocityEngine ve1 = new VelocityEngine();
        ve1.init();
        Template temp = ve1.getTemplate("WebContent/reviewForm.html");
        VelocityContext contextcount = new VelocityContext();
        contextcount.put("date", date);
        contextcount.put("hotelId", hotelId);
        temp.merge(contextcount, out);
    }
}