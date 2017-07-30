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
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

@SuppressWarnings("serial")
public class userReviewUpdate extends BaseServlet {

    private static final String USER_REVIEWS_SQL = "SELECT * FROM reviews_master WHERE review_id=?  ";
    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public userReviewUpdate() {
        Status status = Status.OK;
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
        // redirect if user is not logged in

        String username = loggedinUsernameSession(request);
        if (username == null) {

            String url = "/login";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

        String review_id = request.getParameter("review_id");

        if (request.getParameter("delete") != null) {
            String delete = request.getParameter("delete");

            dbhandler.deleteReviews(review_id);
            //redirect to same page after updating review to the table
            String url = "/userreviews";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }

        PrintWriter out = response.getWriter();
        prepareResponse("Reviews", response, request);

        String hotel_id = request.getParameter("hotel_id");
        updateReview(out, review_id, hotel_id);
        finishResponse(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        /**
         * add user review to the database table
         */
        String overAllRating = request.getParameter("overAllRating");
        int new_overAllRating = Integer.parseInt(overAllRating);

        String reviewTitle = request.getParameter("reviewTitle");
        String reviewText = request.getParameter("reviewText");
        String review_id = request.getParameter("review_id");
        String date = request.getParameter("date");
        String isrecom = request.getParameter("isrecom");
        String userName = loggedinUsernameSession(request);

        dbhandler.updateReviews(review_id, new_overAllRating, reviewTitle, reviewText, date, userName, isrecom);

        String url = "/userreviews";
        url = response.encodeRedirectURL(url);
        response.sendRedirect(url);

    }

    /**
     * Writes an HTML form and takes new reviews from the user
     */

    public void updateReview(PrintWriter out, String review_id, String hotel_id) {

        ResultSet results;

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(USER_REVIEWS_SQL);) {
                statement.setString(1, review_id);

                results = statement.executeQuery();

                while (results.next()) {
                    String title = results.getString("title");
                    String text = results.getString("text");
                    String date = results.getString("date");
                    String isrecom = results.getString("isrecom");
                    int rating = results.getInt("rating");

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    Date date1 = new Date();
                    String current_date = dateFormat.format(date1);

                    VelocityEngine ve1 = new VelocityEngine();
                    ve1.init();
                    Template temp = ve1.getTemplate("WebContent/reviewUpdateForm.html");
                    VelocityContext contextcount = new VelocityContext();
                    contextcount.put("date", current_date);
                    contextcount.put("title", title);
                    contextcount.put("text", text);
                    contextcount.put("rating", rating);
                    contextcount.put("isrecom", isrecom);

                    contextcount.put("review_id", review_id);
                    temp.merge(contextcount, out);

                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
