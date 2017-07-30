package java_project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author RUSHABH
 */

public class touristAttraction {


    /**
     * Used to get all the hotelnames from hotels_master.
     */
    //private static final String hotelNames_SQL = "SELECT hotels_master.hotel_id,hotel_name,rating FROM hotels_master,reviews_master WHERE hotels_master.hotel_id = reviews_master.hotel_id";
    private static final String hotelNames_SQL = "SELECT * FROM hotels_master WHERE hotel_id = ?";

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public touristAttraction() {
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

    /**
     * Method to fetch attraction multithreaded
     */


    public void fetch_attraction(int hotel_id, int radius) {

        int radiusInMiles = radius;
        int meters = radiusInMiles * 1609;

        String query = generateQueries(hotel_id);
        String urlString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&radius=" + meters + "&key=AIzaSyDsL25IyJgdBCfwxXpYB8mYKHgSVNYhkkY";
        URL url;
        try {
            url = new URL(urlString);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request = getRequest(url.getHost(), url.getPath() + "?" + url.getQuery());
            System.out.println(request);

            out.println(request);
            out.flush();

	          /*
               * printwriter error checking
	           */
            if (out.checkError())
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");
	          
	         /* read response */

            String inputLine;
            StringBuffer input = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {

                input.append(inputLine);
            }
            String response = input.toString();
            String jsonResponse = removeHeader(response, "{");
            try {
                JSONParser parser = new JSONParser();
                Object obj;
                obj = parser.parse(jsonResponse);

                JSONObject jsonObject = (JSONObject) obj;
                JSONArray jsonArray = (JSONArray) jsonObject.get("results");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonobject = (JSONObject) jsonArray.get(i);
                    String attractionId = (String) jsonobject.get("id");
                    String attractionName = (String) jsonobject.get("name");
                    double attractionRating = 4;
                    String attractionAddress = (String) jsonobject.get("formatted_address");
                    System.out.println(attractionName + " " + attractionId + " " + attractionRating + "\n" + attractionAddress);
                    //threadSafeHD.addAttraction(attractionId,attractionName,attractionRating, attractionAddress, hotelId);
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            in.close();
            out.close();
            socket.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**iterate over hotel list and runs new thread
     *
     * @param radiusInMiles
     */
    /*	public void fetchAttractions(int radiusInMiles){

            int meters = radiusInMiles * 1609;

            Iterator<String> iterator = threadSafeHD.getHotels().iterator();
            while (iterator.hasNext()) {

                    String hotelId = iterator.next();
                    queue.execute(new WorkerGetRequest(meters,hotelId));

            }
    }
    */

    /**
     * removes header from the json response
     *
     * @return
     */
    public String removeHeader(String input, String word) {
        return input.substring(input.indexOf(word));
    }

    /**
     * Takes a host and a string containing path/resource/query and creates a
     * string of the HTTP GET request
     *
     * @param host
     * @param pathResourceQuery
     * @return
     */
    private String getRequest(String host, String pathResourceQuery) {
        String request = "GET " + pathResourceQuery + " HTTP/1.1" + System.lineSeparator() // GET
                // request
                + "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
                + "Connection: close" + System.lineSeparator() // make sure the server closes the
                // connection after we fetch one page
                + System.lineSeparator();
        return request;
    }

    /**
     * generates subquery for the GET request
     *
     * @return
     */

    public String generateQueries(int hotelId) {

        ResultSet results;
        String query = "";

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(hotelNames_SQL);) {
                statement.setInt(1, hotelId);
                results = statement.executeQuery();
                int i = 1;
                while (results.next()) {

                    String hotel_name = results.getString("hotel_name");
                    int hotel_id = results.getInt("hotel_id");
                    String hotel_state = results.getString("hotel_state");
                    String hotel_city = results.getString("hotel_city");
                    float lat = results.getFloat("latitude");
                    float lng = results.getFloat("longitude");
                    String newCity = hotel_city.replaceAll("\\s", "%20");
                    query = "tourist%20attractions+in+" + newCity + "&location=" + lat + "," + lng;
                    return query;

                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return query;
    }
}
