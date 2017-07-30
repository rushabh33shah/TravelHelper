
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A servlet that shows tourist attractions near the hotel
 */

@SuppressWarnings("serial")
public class touristAttractionListServlet extends BaseServlet {

    // DatabaseHandler interacts with the MySQL database
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Used to get all the hotelnames from hotels_master.
     */
    //private static final String hotelNames_SQL = "SELECT hotels_master.hotel_id,hotel_name,rating FROM hotels_master,reviews_master WHERE hotels_master.hotel_id = reviews_master.hotel_id";
    private static final String hotelNames_SQL = "SELECT * FROM hotels_master WHERE hotel_id = ?";

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;
    public Map<String, ArrayList> map;

    public touristAttractionListServlet() {
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

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        prepareResponse("Attractions", response, request);

        // Get data from the textfields of the html form
        String hotelid = request.getParameter("hotel_id");
        String radius = request.getParameter("radius");
        // sanitize user input to avoid XSS attacks:

        String newhotelid = StringEscapeUtils.escapeHtml4(hotelid);
        String newradius = StringEscapeUtils.escapeHtml4(radius);

        fetch_attraction(hotelid, radius, out);

    }

    /**
     * Method to fetch attraction multithreaded
     */
    public void fetch_attraction(String hotel_id, String radius, PrintWriter out) {

        int radiusInMiles = Integer.parseInt(radius);
        int meters = radiusInMiles * 1609;
        String query = generateQueries(hotel_id);
        String urlString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&radius=" + meters + "&key=AIzaSyDsL25IyJgdBCfwxXpYB8mYKHgSVNYhkkY";
        URL url;
        try {
            url = new URL(urlString);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
            PrintWriter outsocket = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request = getRequest(url.getHost(), url.getPath() + "?" + url.getQuery());
            outsocket.println(request);
            outsocket.flush();

            /*
            * printwriter error checking
            */
            if (outsocket.checkError())
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

                    /** for printing attraction to html file*/
                    map.put(attractionId, new ArrayList());
                    map.get(attractionId).add(attractionName);
                    map.get(attractionId).add(attractionAddress);

                    // threadSafeHD.addAttraction(attractionId,attractionName,attractionRating, attractionAddress, hotelId);
                }
                displayAttractionsHtml(out, map);

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

    public String generateQueries(String hotelId) {

        ResultSet results;
        String query = "";
        int int_hotelId = Integer.parseInt(hotelId);

        try (Connection connection = db.getConnection();) {

            try (PreparedStatement statement = connection.prepareStatement(hotelNames_SQL);) {
                statement.setInt(1, int_hotelId);
                results = statement.executeQuery();
                while (results.next()) {

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

    /**
     * Writes an HTML to print tourist attraction
     */
    public void displayAttractionsHtml(PrintWriter out, Map map) {

        assert out != null;
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        Template t = ve.getTemplate("WebContent/touristAttractionListTable.html");
        VelocityContext context = new VelocityContext();
        context.put("map", map);
        t.merge(context, out);
        map.clear();

    }
}