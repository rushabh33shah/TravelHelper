package java_project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class reviewUserRegister extends BaseServlet {

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    private static final String AllReviews_SQL = "SELECT * FROM reviews_master";
    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public reviewUserRegister() {
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

        /**
         * use this method to register users from reviews_master table
         */
        //loadUsers();
    }

    public void loadUsers() {

        ResultSet results;
        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(AllReviews_SQL);) {

                results = statement.executeQuery();
                int i = 0;
                while (results.next()) {

                    String username = results.getString("username");
                    System.out.println(username);
                    if (username.contains("anonymous")) {
                        System.out.println(username);
                    } else {
                        /**
                         * run this line to register users from reviews_master table
                         */

                        // dbhandler.registerReviewUser(username, "", "", "", "", "", "", "","", "");
                        System.out.println(i++);
                    }

                }

            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
