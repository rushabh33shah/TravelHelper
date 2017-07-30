
package java_project;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/** 
 * A servlet that handles user registration. doGet() method displays an HTML form with a button and
 *  textfields for user information
 *  doGet() displays registration form
 * doPost() processes the form: if the username is not taken, it adds user info to the database.
 *
 */
@SuppressWarnings("serial")
public class RegistrationServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Register New User", response,request);

		// redirect if user is already logged in 
			String username=loggedinUsernameSession(request);
			if(username!=null){
			
				String url = "/hotels";
				url = response.encodeRedirectURL(url);
				response.sendRedirect(url); // send a get request  (redirect to the same path)
			
			}
			
		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		displayForm(out); 
		finishResponse(response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Register New User", response, request);

		// redirect if user is already logged in 
				String username1=loggedinUsernameSession(request);
				if(username1!=null){
				
					String url = "/hotels";
					url = response.encodeRedirectURL(url);
					response.sendRedirect(url); // send a get request  (redirect to the same path)
				
				}
			
		// Get data from the textfields of the html form
		String username = request.getParameter("user");
		String firstname = request.getParameter("firstname");
		String lastname = request.getParameter("lastname");
		String email = request.getParameter("email");
		String mobile = request.getParameter("mobile");
		String streetAddress = request.getParameter("streetAddress");
		String city = request.getParameter("city");
		String state = request.getParameter("state");
		String country = request.getParameter("country");
		
		
		String password = request.getParameter("pass");
		// sanitize user input to avoid XSS attacks:
		username = StringEscapeUtils.escapeHtml4(username);
		password = StringEscapeUtils.escapeHtml4(password);
		System.out.println(username+firstname+lastname+email+mobile+streetAddress+city+state+country+"===="+password);
		// add user's info to the database 
		Status status = dbhandler.registerUser(username, firstname, lastname, email, mobile,streetAddress, city, state, country, password);

		if(status == Status.OK) { // registration was successful
			response.getWriter().println("Registered! Database updated.");
		}
		else { // if something goes wrong
			String url = "/register?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url); // send a get request  (redirect to the same path)
		}
	}

	/** Writes and HTML form that shows textfields and a button to register that user */
	private void displayForm(PrintWriter out) {
		assert out != null;
		VelocityEngine ve = new VelocityEngine();
        ve.init();
        Template t = ve.getTemplate( "WebContent/registrationForm.html" );
        VelocityContext context = new VelocityContext();
        t.merge( context, out ); 
     
	}
}