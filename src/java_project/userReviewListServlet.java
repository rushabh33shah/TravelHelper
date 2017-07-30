
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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/** 
 * A servlet that shows list of reviews that user have written. 
 * doGet() method displays an HTML page  with a list of reviews 
 * 
 */
@SuppressWarnings("serial")
public class userReviewListServlet extends BaseServlet {
	
	/** Used to get all the hotelnames from hotels_master. */
	private static final String USER_REVIEWS_SQL = "SELECT * FROM reviews_master WHERE username=?  ";
	/** print hotel info */
	private static final String hotelNames_SQL = "SELECT * FROM hotels_master WHERE hotel_id = ?";

	/** Used to configure connection to database. */
	private DatabaseConnector db;
	public Map<String, ArrayList> map;

	public userReviewListServlet(){
		Status status = Status.OK;
		//random = new Random(System.currentTimeMillis());
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
		// redirect if user is not logged in 
		
		String username=loggedinUsernameSession(request);
		if(username==null){
			
			String url = "/login";
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);   
		}
		PrintWriter out = response.getWriter();
		prepareResponse("Reviews", response, request);
		
		// error will not be null if we were forwarded here from the post method where something went wrong
		String error = request.getParameter("error");
		
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}
		displayUserReviews(out,username); 
		finishResponse(response);
	}
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String username=loggedinUsernameSession(request);
		if(username==null){
			
			String url = "/login";
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);   
		}
	}
	
	/** Writes an HTML page that shows list of user's review */
	public void displayUserReviews(PrintWriter out, String username) {
		assert out != null;
		
		String newusername = username;
		ResultSet results;
		ResultSet results1;
		
		//System.out.println(new_hotel_id);
	
		try (Connection connection = db.getConnection();) {
			
			try (PreparedStatement statement = connection.prepareStatement(USER_REVIEWS_SQL);) {
				statement.setString(1,newusername);
				results = statement.executeQuery();
			
				int i=1;
				while (results.next()) {
					 	//String username = results.getString("username");
					 	String title = results.getString("title");
					 	String text = results.getString("text");
					 	String date = results.getString("date");
					 	String isrecom = results.getString("isrecom");
					 	int rating = results.getInt("rating");
					 	int hotel_id = results.getInt("hotel_id");
					 	String review_id = results.getString("review_id");
							
						try (Connection connection1 = db.getConnection();) {
							try (PreparedStatement statement1 = connection1.prepareStatement(hotelNames_SQL);) {
								statement1.setInt(1, hotel_id);
								results1 = statement1.executeQuery();
								while (results1.next()) {
									
									String hotel_name = results1.getString("hotel_name");
									map.put(review_id, new ArrayList());
									map.get(review_id).add(username);
									map.get(review_id).add(hotel_name);
									map.get(review_id).add(title);
									map.get(review_id).add(text);
									map.get(review_id).add(date);
									map.get(review_id).add(rating);
									map.get(review_id).add(isrecom);
									map.get(review_id).add(hotel_id);

								}
							}
						}
			 	}
			} 
			VelocityEngine ve1 = new VelocityEngine();
	        ve1.init();
	    	Template t1 = ve1.getTemplate( "WebContent/userReviewListTable.html" );
	        VelocityContext context1 = new VelocityContext();
	        context1.put("map", map);
	        t1.merge( context1, out ); 
	        map.clear();
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}
	}
}