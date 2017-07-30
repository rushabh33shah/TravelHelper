
package java_project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that inserts user reviews into database
 * <p>
 * doPost() insert data into database
 */
@SuppressWarnings("serial")
public class insertUserReview extends BaseServlet {

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to get all the hotelnames from hotels_master.
     */
    private static final String HOTEL_REVIEWS_SQL = "SELECT * FROM hotels_master, reviews_master WHERE reviews_master.hotel_id = ? AND reviews_master.hotel_id = hotels_master.hotel_id  ";
    /**
     * print hotel info
     */
    private static final String hotelNames_SQL = "SELECT * FROM hotels_master WHERE hotel_id = ?";


    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public insertUserReview() {
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

    }

    /**
     * doPost() insert data into database
     */
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
        String hotelId = request.getParameter("hotelId");
        String date = request.getParameter("date");
        String isrecom = request.getParameter("isrecom");
        String userName = loggedinUsernameSession(request);

        Random random = new Random();
        String reviewID = random.toString();
        dbhandler.insertReviews(hotelId, reviewID, new_overAllRating, reviewTitle, reviewText, date, userName, isrecom);

        //redirect to same page after adding review to the table
        String url = "/reviews?hotel_id=" + hotelId;
        url = response.encodeRedirectURL(url);
        response.sendRedirect(url);

    }
}