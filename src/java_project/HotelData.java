package java_project;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.json.simple.*;
import org.json.simple.parser.*;


/**
 * Class HotelData - a data structure that stores information about hotels and
 * hotel reviews. Allows to quickly lookup a hotel given the hotel id.
 * Allows to easily find hotel reviews for a given hotel, given the hotelID.
 * Reviews for a given hotel id are sorted by the date and user nickname.
 */
public class HotelData {


    // TODO: You should initializa data structures in constructor.
    private Map<String, TreeSet> reviewTreeMap;//= new TreeMap<String,TreeSet>();
    private Map<String, Hotel> hotelTreeMap;// = new TreeMap<String,Hotel>();

    private List<String> hotelList;// = new ArrayList<String>();
    private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

    /**
     * Default constructor.
     */
    public HotelData() {
        reviewTreeMap = new TreeMap<String, TreeSet>();
        hotelTreeMap = new TreeMap<String, Hotel>();

        hotelList = new ArrayList<String>();

    }

    /**
     * Create a Hotel given the parameters, and add it to the appropriate data
     * structure(s).
     *
     * @param hotelId       - the id of the hotel
     * @param hotelName     - the name of the hotel
     * @param city          - the city where the hotel is located
     * @param state         - the state where the hotel is located.
     * @param streetAddress - the building number and the street
     * @param latitude
     * @param longitude
     */

    public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, String country, double lat,
                         double lon) {
        if (hotelTreeMap.containsKey(hotelId)) {
        } else {
            Address address = new Address(streetAddress, city, state, lat, lon);
            Hotel hotel = new Hotel(hotelId, hotelName, address);
            hotelTreeMap.put(hotelId, hotel);

            /**
             * method to upload hotels from json file to database
             */
            //dbhandler.insertHotels(hotelId, hotelName, city, state, streetAddress, country, lat, lon);

        }

    }

    /**
     * Add a new review.
     *
     * @param hotelId       - the id of the hotel reviewed
     * @param reviewId      - the id of the review
     * @param rating        - integer rating 1-5.
     * @param reviewTitle   - the title of the review
     * @param review        - text of the review
     * @param isRecommended - whether the user recommends it or not
     * @param date          - date of the review in the format yyyy-MM-dd, e.g.
     *                      2016-08-29.
     * @param username      - the nickname of the user writing the review.
     * @return true if successful, false if unsuccessful because of invalid date
     * or rating. Needs to catch and handle ParseException if the date is invalid.
     * Needs to check whether the rating is in the correct range
     */

    public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
                             String isRecom, String date, String username) {

        System.out.println(isRecom);
        String reviewDate = date;

        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");


        try {
            Date tempDate = df1.parse(date);
            reviewDate = df1.format(tempDate);

        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            return false;
        }

        if (username.isEmpty()) {
            username = "anonymous";
        }
        if (rating > 5 || rating < 1) {
            return false;
        }
        if (hotelTreeMap.containsKey(hotelId) == false) {
            return false;
        } else if (reviewTreeMap.containsKey(hotelId)) {

            Review ro = new Review(reviewId, hotelId, reviewTitle, review, username, reviewDate, rating);

            TreeSet<Review> r = reviewTreeMap.get(hotelId);
            r.add(ro);

            /**
             * inserting reviews from json file to database; calling a method of database handler
             */
            dbhandler.insertReviews(hotelId, reviewId, rating, reviewTitle, review, date, username, isRecom);
        } else {
            Review ro = new Review(reviewId, hotelId, reviewTitle, review, username, reviewDate, rating);

            TreeSet<Review> r1 = new TreeSet();
            r1.add(ro);
            reviewTreeMap.put(hotelId, r1);
            /**
             * inserting reviews from json file to database; calling a method of database handler
             */
            dbhandler.insertReviews(hotelId, reviewId, rating, reviewTitle, review, date, username, isRecom);

        }
        return true; // don't forget to change it

    }

    /**
     * Return an alphabetized list of the ids of all hotels
     *
     * @return
     */
    public List<String> getHotels() {
        hotelList.addAll(hotelTreeMap.keySet());
        return hotelList; // don't forget to change it
    }

    /**
     * Read the json file with information about the hotels (id, name, address,
     * etc) and load it into the appropriate data structure(s). Note: This
     * method does not load reviews
     *
     * @param filename the name of the json file that contains information about the
     *                 hotels
     */

    public void loadHotelInfo(String jsonFilename) {

        Path p = Paths.get(jsonFilename);
        Path p1 = p.toAbsolutePath();
        String jfile = p1.toString();

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(jfile));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonArray = (JSONArray) jsonObject.get("sr");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonobject = (JSONObject) jsonArray.get(i);
                String id = (String) jsonobject.get("id");
                String fname = (String) jsonobject.get("f");
                String ad = (String) jsonobject.get("ad");
                String ci = (String) jsonobject.get("ci");
                String pr = (String) jsonobject.get("pr");
                String country = (String) jsonobject.get("c");
                JSONObject joobject = (JSONObject) jsonobject.get("ll");
                String lat = (String) joobject.get("lat");
                String lng = (String) joobject.get("lng");
                double latt = Double.parseDouble(lat);
                double lng1 = Double.parseDouble(lng);

                addHotel(id, fname, ci, pr, ad, country, latt, lng1);
            }
        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Load reviews for all the hotels into the appropriate data structure(s).
     * Traverse a given directory recursively to find all the json files with
     * reviews and load reviews from each json. Note: this method must be
     * recursive and use DirectoryStream as discussed in class.
     *
     * @param path the path to the directory that contains json files with
     *             reviews Note that the directory can contain json files, as
     *             well as subfolders (of subfolders etc..) with more json files
     */
    public void loadReviews(Path path) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            for (Path file : stream) {
                if (Files.isDirectory(file)) {
                    loadReviews(file);
                } else {
                    Path filepath = file.toAbsolutePath();
                    //System.out.println(filepath.toString());
                    loadReview(filepath);
                 }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private void loadReview(Path jsonpath) {

        JSONParser parser = new JSONParser();
        String stringJSONPath = jsonpath.toString();
        Object obj;

        try {
            obj = parser.parse(new FileReader(stringJSONPath));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject reviewDetails = (JSONObject) jsonObject.get("reviewDetails");
            JSONObject reviewCollectionJSON = (JSONObject) reviewDetails.get("reviewCollection");
            JSONArray review = (JSONArray) reviewCollectionJSON.get("review");
            for (int i = 0; i < review.size(); i++) {
                JSONObject reviewTextJSON = (JSONObject) review.get(i);
                String reviewID1 = (String) reviewTextJSON.get("reviewId");
                //System.out.println("reviewid: " +reviewID1);
                String hotelID = (String) reviewTextJSON.get("hotelId");
                //System.out.println("hotelID" +hotelID);
                String reviewText = (String) reviewTextJSON.get("reviewText");
                String title = (String) reviewTextJSON.get("title");
                long rating = (long) reviewTextJSON.get("ratingOverall");
                String isRecom = (String) reviewTextJSON.get("isRecommended");
                boolean isRecoms = "YES".equals(isRecom) ? true : false;
                String uname = (String) reviewTextJSON.get("userNickname");
                String date = (String) reviewTextJSON.get("reviewSubmissionTime");
                //System.out.println(isRecom);
                addReview(hotelID, reviewID1, (int) rating, title, reviewText, isRecom, date, uname);
                //addReview(hotelID, reviewID1,(int)rating, title, reviewText, isRecoms, date, uname);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Returns a string representing information about the hotel with the given
     * id, including all the reviews for this hotel separated by
     * -------------------- Format of the string: HoteName: hotelId
     * streetAddress city, state -------------------- Review by username: rating
     * ReviewTitle ReviewText -------------------- Review by username: rating
     * ReviewTitle ReviewText ...
     *
     * @param hotel
     * id
     * @return - output string.
     */
    int j = 1;

    public String toString(String hotelId) {

        String id = hotelId;
        String p = "";

        for (Entry<String, TreeSet> entry1 : reviewTreeMap.entrySet()) {

            String reviewHotelId = entry1.getKey();
            if (id.equalsIgnoreCase(reviewHotelId)) {

                Hotel hotel = hotelTreeMap.get(id);
                p = p + hotel.toString();
                TreeSet r = reviewTreeMap.get(entry1.getKey());
                Iterator<Review> it = r.iterator();

                while (it.hasNext()) {

                    Review rv = it.next();
                    p = p + rv.toString();
                }

            }

        }
        if (hotelTreeMap.containsKey(id) == true && reviewTreeMap.containsKey(id) == false) {

            Hotel hotel1 = hotelTreeMap.get(id);
            p = p + hotel1.toString();
        }
        return p + "\n";
    }

    /**
     * Save the string representation of the hotel data to the file specified by
     * filename in the following format:
     * an empty line
     * A line of 20 asterisks ******************** on the next line
     * information for each hotel, printed in the format described in the toString method of this class.
     *
     * @param filename - Path specifying where to save the output.
     */
    public void printToFile(Path filename) {

        FileWriter fstream;
        BufferedWriter out;
        try {
            String outputPath = filename.toString();
            fstream = new FileWriter(outputPath);
            out = new BufferedWriter(fstream);

            for (Entry<String, Hotel> entry : hotelTreeMap.entrySet()) {
                String hotelId = entry.getKey();
                out.write("\n********************");
                out.write("\n");
                out.write(toString(hotelId));
                out.flush();

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
