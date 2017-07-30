
package java_project;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Handles all database-related actions. Uses singleton design pattern. Modified
 * by Prof. Karpenko from the original example of Prof. Engle.
 *
 * @see RegisterServer
 */
public class DatabaseHandler {

    /**
     * Makes sure only one database handler is instantiated.
     */
    private static DatabaseHandler singleton = new DatabaseHandler();

    /**
     * Used to determine if login_users table exists.
     */
    private static final String TABLES_SQL = "SHOW TABLES LIKE 'login_users';";

    /**
     * Used to create login_users table for this example.
     */
    private static final String CREATE_SQL = "CREATE TABLE login_users (user_id INTEGER AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, password CHAR(64) NOT NULL, usersalt CHAR(32) NOT NULL);";

    /**
     * Used to insert a new user's info into the users_master table
     */
    //private static final String REGISTER_SQL = "INSERT INTO login_users (username, password, usersalt) "
    //+ "VALUES (?, ?, ?);";

    private static final String REGISTER_SQL = "INSERT INTO users_master (username, firstname, lastname, email, mobile, "
            + "streetAddress, city, state, country, last_login) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";


    /**
     * Used to insert a new hotel info into the hotels_master table
     */

    private static final String INSERT_HOTEL_SQL = "INSERT INTO hotels_master (hotel_id, hotel_name, hotel_city, hotel_state, hotel_address, "
            + "hotel_country, latitude, longitude) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    /**
     * Used to insert a new review info into the reviews_master table
     */

    private static final String INSERT_REVIEWS_SQL = "INSERT INTO reviews_master (hotel_id, review_id, rating, title, text, "
            + "date, username, isrecom) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    /**
     * Used to insert a new save hotel info into the saved_hotels table
     */

    private static final String INSERT_SAVE_HOTELS_SQL = "INSERT INTO saved_hotels (user_id, hotel_id) "
            + "VALUES (?, ?);";

    /**
     * USED TO INSER EXPEDIA HISTORY TO DATABASE
     */
    private static final String INSERT_EXPEDIA_HOTELS_SQL = "INSERT INTO expedia_master (user_id, expedia_hotel) "
            + "VALUES (?, ?);";
    /**
     * UPDATES REVIEW TABLE
     */
    private static final String UPDATE_REVIEWS_SQL = "UPDATE reviews_master SET rating=?, title=?, text=?, date=?, isrecom=? WHERE review_id=?";

    /**
     * UPDATE LAST LOGIN TIME
     */
    private static final String UPDATE_LAST_LOGIN_SQL = "UPDATE users_master SET last_login=?  WHERE user_id=?";


    /**
     * Used to remove a reviews from the database.
     */
    private static final String DELETE_REVIEW_SQL = "DELETE FROM reviews_master WHERE review_id = ?";

    /**
     * Used to remove a saved hotels from the database.
     */
    private static final String DELETE_SAVED_HOTELS_SQL = "DELETE FROM saved_hotels WHERE user_id = ?";

    /**
     * Used to remove a saved hotels from the database.
     */
    private static final String DELETE_EXPEDIA_HOTELS_SQL = "DELETE FROM expedia_master WHERE user_id = ?";

    /**
     * Used to insert a new user's login info into the login_users table
     */
    private static final String STORE_PASSWORD_SQL = "INSERT INTO login_users (username, password, usersalt) VALUES (?, ?, ?);";

    /**
     * Used to determine if a username already exists.
     */
    private static final String USER_SQL = "SELECT username FROM login_users WHERE username = ?";

    /**
     * Used to retrieve userid.
     */
    private static final String USERID_SQL = "SELECT user_id FROM users_master WHERE username = ?";

    /**
     * Used to retrieve last_login.
     */
    private static final String USER_LASTLOGIN_SQL = "SELECT last_login FROM users_master WHERE username = ?";


    // ------------------ constants below will be useful for the login operation
    // once you implement it
    /**
     * Used to retrieve the salt associated with a specific user.
     */
    private static final String SALT_SQL = "SELECT usersalt FROM login_users WHERE username = ?";

    /**
     * Used to authenticate a user.
     */
    private static final String AUTH_SQL = "SELECT username FROM login_users " + "WHERE username = ? AND password = ?";


    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    /**
     * Used to generate password hash salt for user.
     */
    private Random random;

    /**
     * This class is a singleton, so the constructor is private. Other classes
     * need to call getInstance()
     */
    private DatabaseHandler() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());

        try {
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
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
     * Gets the single instance of the database handler.
     *
     * @return instance of the database handler
     */
    public static DatabaseHandler getInstance() {
        return singleton;
    }

    /**
     * Checks to see if a String is null or empty.
     *
     * @param text - String to check
     * @return true if non-null and non-empty
     */
    public static boolean isBlank(String text) {
        return (text == null) || text.trim().isEmpty();
    }

    /**
     * Checks if necessary table exists in database, and if not tries to create
     * it.
     *
     * @return {@link Status.OK} if table exists or create is successful
     */
    private Status setupTables() {
        Status status = Status.ERROR;
/*
        try (Connection connection = db.getConnection(); Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(TABLES_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				} else {
					status = Status.OK;
				}
			} else {
				status = Status.OK;
			}
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
		}
*/
        return status;
    }

    /**
     * Tests if a user already exists in the database. Requires an active
     * database connection.
     *
     * @param connection - active database connection
     * @param user       - username to check
     * @return Status.OK if user does not exist in database
     * @throws SQLException
     */
    private Status duplicateUser(Connection connection, String user) {

        assert connection != null;
        assert user != null;

        Status status = Status.ERROR;

        try (PreparedStatement statement = connection.prepareStatement(USER_SQL);) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();
            status = results.next() ? Status.DUPLICATE_USER : Status.OK;
        } catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            System.out.println("Exception occured while processing SQL statement:" + e);
        }

        return status;
    }

    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes  - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    public static String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);

        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param password - password to hash
     * @param salt     - salt associated with user
     * @return hashed password
     */
    public static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = salted;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        } catch (Exception ex) {
            System.out.println("Unable to properly hash password." + ex);
        }

        return hashed;
    }

    /**
     * Gets the salt for a specific user.
     *
     * @param connection - active database connection
     * @param user       - which user to retrieve salt for
     * @return salt for the specified user or null if user does not exist
     * @throws SQLException if any issues with database connection
     */
    private String getSalt(Connection connection, String user) throws SQLException {
        assert connection != null;
        assert user != null;

        String salt = null;

        try (PreparedStatement statement = connection.prepareStatement(SALT_SQL);) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                salt = results.getString("usersalt");
            }
        }

        return salt;
    }

    /**
     * Registers a new user, placing the username, password hash, and salt into
     * the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return {@link Status.OK} if registration successful
     */
    public Status registerUser(String username, String firstname, String lastname, String email, String mobile, String streetAddress,
                               String city, String state, String country, String password) {
        Status status = Status.ERROR;
        System.out.println("Registering " + username + ".");

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(username) || isBlank(password)) {
            status = Status.INVALID_LOGIN;
            System.out.println("Invalid regiser info");
            return status;
        }

        // try to connect to database and test for duplicate user
        try (Connection connection = db.getConnection();) {
            status = duplicateUser(connection, username);

            // if okay so far, try to insert new user
            if (status == Status.OK) {
                // generate salt
                byte[] saltBytes = new byte[16];
                random.nextBytes(saltBytes);

                String usersalt = encodeHex(saltBytes, 32); // hash salt
                String passhash = getHash(password, usersalt); // combine
                // password and
                // salt and hash
                // again

                String default_logindate = "null";
                // add user's login info to the database table
                try (PreparedStatement statement_um = connection.prepareStatement(REGISTER_SQL);) {
                    statement_um.setString(1, username);
                    statement_um.setString(2, firstname);
                    statement_um.setString(3, lastname);
                    statement_um.setString(4, email);
                    statement_um.setString(5, mobile);
                    statement_um.setString(6, streetAddress);
                    statement_um.setString(7, city);
                    statement_um.setString(8, state);
                    statement_um.setString(9, country);
                    statement_um.setString(10, default_logindate);

                    statement_um.executeUpdate();
                    status = Status.OK;
                }
                // add user's login info to the database table
                try (PreparedStatement statement = connection.prepareStatement(STORE_PASSWORD_SQL);) {
                    statement.setString(1, username);
                    statement.setString(2, passhash);
                    statement.setString(3, usersalt);
                    statement.executeUpdate();
                    status = Status.OK;
                }
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }

    /**
     * Registers a new user, placing the username, password hash, and salt into
     * the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return {@link Status.OK} if registration successful
     */
    public Status loginUser(String username, String password) {

        Status status = Status.ERROR;

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(username) || isBlank(password)) {
            status = Status.INVALID_LOGIN;
            System.out.println("Invalid regiser info");
            return status;
        }

        // try to connect to database and test for duplicate user
        try (Connection connection = db.getConnection();) {

            String salt = getSalt(connection, username);
            String hashedPass = getHash(password, salt);
            try (PreparedStatement statement = connection.prepareStatement(AUTH_SQL);) {
                statement.setString(1, username);
                statement.setString(2, hashedPass);
                ResultSet results = statement.executeQuery();
                status = results.next() ? Status.OK : Status.INVALID_LOGIN;
            }
            // if okay so far,login successfull
            if (status == Status.OK) {

                System.out.println("login successfull !");

            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }

    /**
     * this method inserts into the hotels_master
     *
     * @param hotel_id      -hotel id
     * @param hotel_name    -hotel name
     * @param hotel_city    -hotel city
     * @param hotel_state   -hotel state
     * @param hotel_address -hotel address
     * @param hotel_country -hotel country
     * @param latitude      -latitude
     * @param longitude     -longitude
     * @return -Status.OK if registration successful
     */

    public Status insertHotels(String hotel_id, String hotel_name, String hotel_city, String hotel_state, String hotel_address, String hotel_country,
                               double latitude, double longitude) {
        //System.out.println("going here");

        Status status = Status.ERROR;
        boolean results = false;


        try (Connection connection = db.getConnection();) {
            int new_hotel_id = Integer.parseInt(hotel_id);
            float new_latitude = (float) latitude;
            float new_longitude = (float) longitude;

            try (PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL_SQL);) {
                statement.setInt(1, new_hotel_id);
                statement.setString(2, hotel_name);
                statement.setString(3, hotel_city);
                statement.setString(4, hotel_state);
                statement.setString(5, hotel_address);
                statement.setString(6, hotel_country);
                statement.setFloat(7, new_latitude);
                statement.setFloat(8, new_longitude);
                results = statement.execute();
                status = Status.OK;
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }

    /**
     * Insert reviews into database
     *
     * @param hotelId       hotel id
     * @param reviewID      review id
     * @param overAllRating over all rating
     * @param reviewTitle   title of the review
     * @param reviewText    text of the review
     * @param date          date
     * @param userName      user name
     * @param isrecom       is recommended?
     * @return
     */

    public Status insertReviews(String hotelId, String reviewID, int overAllRating,
                                String reviewTitle,
                                String reviewText, String date, String userName, String isrecom) {
        System.out.println(hotelId);
        Status status = Status.ERROR;
        boolean results = false;
        try (Connection connection = db.getConnection();) {
            int new_hotel_id = Integer.parseInt(hotelId);
            try (PreparedStatement statement = connection.prepareStatement(INSERT_REVIEWS_SQL);) {
                statement.setInt(1, new_hotel_id);
                statement.setString(2, reviewID);
                statement.setInt(3, overAllRating);
                statement.setString(4, reviewTitle);
                statement.setString(5, reviewText);
                statement.setString(6, date);
                statement.setString(7, userName);
                statement.setString(8, isrecom);
                results = statement.execute();
                status = Status.OK;
                System.out.println("OK======");
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }
        return status;
    }

    /**
     * method is used to update user's reviews
     *
     * @param reviewID
     * @param overAllRating
     * @param reviewTitle
     * @param reviewText
     * @param date
     * @param userName
     * @param isrecom
     * @return
     */

    public Status updateReviews(String reviewID, int overAllRating,
                                String reviewTitle,
                                String reviewText, String date, String userName, String isrecom) {
        Status status = Status.ERROR;
        int results;
        //System.out.println("id"+reviewID+" title "+reviewTitle+" text "+reviewText+" username "+userName+ "recom"+ isrecom);

        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_REVIEWS_SQL);) {
                statement.setInt(1, overAllRating);
                statement.setString(2, reviewTitle);
                statement.setString(3, reviewText);
                statement.setString(4, date);
                statement.setString(5, isrecom);
                statement.setString(6, reviewID);
                results = statement.executeUpdate();
                System.out.println(results);
                status = Status.OK;
                System.out.println("UPDATE OK");
            }

        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }
    /**
     * method is used to delete review
     */
    public Status deleteReviews(String review_id) {

        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_REVIEW_SQL);) {
                statement.setString(1, review_id);

                boolean results = statement.execute();
                status = Status.OK;
            }
        } catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            System.out.println("Exception occured while processing SQL statement:" + e);
        }

        return status;
    }

    /**
     * delete saved hotels
     *
     * @param user_id
     * @return
     */
    public Status deleteSavedHotels(int user_id) {

        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SAVED_HOTELS_SQL);) {
                statement.setInt(1, user_id);
                boolean results = statement.execute();
                status = Status.OK;
            }
        } catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            System.out.println("Exception occured while processing SQL statement:" + e);
        }

        return status;
    }

    /**
     * delete expedia hotels
     *
     * @param user_id
     * @return
     */

    public Status deleteExpediaHotels(int user_id) {

        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_EXPEDIA_HOTELS_SQL);) {
                statement.setInt(1, user_id);

                boolean results = statement.execute();
                status = Status.OK;
            }
        } catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
            System.out.println("Exception occured while processing SQL statement:" + e);
        }

        return status;
    }

    /**
     * return userid of username
     *
     * @param username
     * @return
     */

    protected int getuser_id(String username) {

        assert username != null;
        int user_id = 0;
        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(USERID_SQL);) {
                statement.setString(1, username);

                ResultSet results = statement.executeQuery();

                results = statement.executeQuery();
                while (results.next()) {

                    user_id = results.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception occured while processing SQL statement:" + e);
        }
        return user_id;
    }

    /**
     * registering users from reviewJSON
     *
     * @param username
     * @param firstname
     * @param lastname
     * @param email
     * @param mobile
     * @param streetAddress
     * @param city
     * @param state
     * @param country
     * @param password
     * @return
     */
    public Status registerReviewUser(String username, String firstname, String lastname, String email, String mobile, String streetAddress,
                                     String city, String state, String country, String password1) {
        String password = "reviews";
        Status status = Status.ERROR;
        System.out.println("Registering " + username + ".");

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(username) || isBlank(password)) {
            status = Status.INVALID_LOGIN;
            System.out.println("Invalid regiser info");
            return status;
        }

        // try to connect to database and test for duplicate user
        try (Connection connection = db.getConnection();) {
            status = duplicateUser(connection, username);

            // if okay so far, try to insert new user
            if (status == Status.OK) {
                // generate salt
                byte[] saltBytes = new byte[16];
                random.nextBytes(saltBytes);

                String usersalt = encodeHex(saltBytes, 32); // hash salt
                String passhash = getHash(password, usersalt); // combine
                // password and
                // salt and hash
                // again
                // add user's login info to the database table
                try (PreparedStatement statement_um = connection.prepareStatement(REGISTER_SQL);) {
                    statement_um.setString(1, username);
                    statement_um.setString(2, "Review User");
                    statement_um.setString(3, "Reiew user lastname");
                    statement_um.setString(4, "review_user@default.com");
                    statement_um.setString(5, "1234567890");
                    statement_um.setString(6, "review user's default address");
                    statement_um.setString(7, "fake city");
                    statement_um.setString(8, "fake state");
                    statement_um.setString(9, "fake country");

                    statement_um.executeUpdate();
                    status = Status.OK;
                }
                // add user's login info to the database table
                try (PreparedStatement statement = connection.prepareStatement(STORE_PASSWORD_SQL);) {
                    statement.setString(1, username);
                    statement.setString(2, passhash);
                    statement.setString(3, usersalt);
                    statement.executeUpdate();
                    status = Status.OK;
                }
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }

    /**
     * update user's last login time
     *
     * @param user_id
     * @return status
     */
    protected Status update_user_lastlogin(int user_id) {

        Status status = Status.ERROR;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date1 = new Date();
        String current_date = dateFormat.format(date1);

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_LOGIN_SQL);) {
                statement.setString(1, current_date.trim());
                statement.setInt(2, user_id);
                statement.executeUpdate();
                status = Status.OK;
            }
        } catch (SQLException e) {
            System.out.println("Exception occured while processing SQL statement:" + e);
        }
        return status;
    }

    /**
     * @param username
     * @return
     */
    protected String getuser_last_login(String username) {

        assert username != null;
        String last_login = "";

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(USER_LASTLOGIN_SQL);) {
                statement.setString(1, username);

                ResultSet results = statement.executeQuery();

                results = statement.executeQuery();
                while (results.next()) {

                    last_login = results.getString("last_login");
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception occured while processing SQL statement:" + e);
        }
        return last_login;
    }
    /**
     * insert user's saved hotels to database
     *
     * @param hotelId
     * @param userId
     * @return status
     */

    public Status insertSavedHotels(String hotelId, int userId) {
        System.out.println(hotelId);
        Status status = Status.ERROR;
        boolean results = false;
        int new_hotel_id = Integer.parseInt(hotelId);

        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SAVE_HOTELS_SQL);) {
                statement.setInt(1, userId);
                statement.setInt(2, new_hotel_id);
                results = statement.execute();
                status = Status.OK;
                System.out.println("OK======");
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }

    /**
     * insert visited expedia links
     *
     * @param userId
     * @param expedia_hotel
     * @return
     */
    public Status insertExpediaHotels(int userId, String expedia_hotel) {
        System.out.println(expedia_hotel);
        Status status = Status.ERROR;
        boolean results = false;
        int new_hotel_id = Integer.parseInt(expedia_hotel);
        try (Connection connection = db.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_EXPEDIA_HOTELS_SQL);) {
                statement.setInt(1, userId);
                statement.setInt(2, new_hotel_id);

                results = statement.execute();
                status = Status.OK;
                System.out.println("OK======");
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            System.out.println("Error while connecting to the database: " + ex);
        }

        return status;
    }
}
