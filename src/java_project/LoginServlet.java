
package java_project;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/** 
 * A servlet that handles user login. 
 * doGet() method displays an HTML form with a button and two textfields: one for the username, one for the password.
 * doPost() processes the form: login if username and password is correct and start session.
 *
 */
@SuppressWarnings("serial")
public class LoginServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		// redirect if user is already logged in 
		String username=loggedinUsernameSession(request);
		if(username!=null){
		
			String url = "/hotels";
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url); // send a get request  (redirect to the same path)
		
		}
	
		String logout = request.getParameter("logout");
		if(logout != null){
		HttpSession session = request.getSession();
		
		session.invalidate();
		}
		prepareResponse("Login User", response,request);
		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}
	
		VelocityEngine ve = new VelocityEngine();
        ve.init();
        Template t = ve.getTemplate( "WebContent/loginForm.html" );
        VelocityContext context = new VelocityContext();
        t.merge( context, out ); 
		
		finishResponse(response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Login User", response,request);

		// redirect if user is already logged in 
		String username=loggedinUsernameSession(request);
		if(username!=null){
		
			String url = "/hotels";
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url); // send a get request  (redirect to the same path)
		
		}
		// Get data from the textfields of the html form
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		// sanitize user input to avoid XSS attacks:
		newuser = StringEscapeUtils.escapeHtml4(newuser);
		newpass = StringEscapeUtils.escapeHtml4(newpass);
		
		// check user's info to the database 
		Status status = dbhandler.loginUser(newuser, newpass);
		int user_id = dbhandler.getuser_id(newuser);

		if(status == Status.OK) { // login was successful

			String last_login = dbhandler.getuser_last_login(newuser);
			System.out.println(last_login);
		
			dbhandler.update_user_lastlogin(user_id);

			HttpSession session = request.getSession();
			session.setAttribute("username", newuser);
			session.setAttribute("user_id", user_id);
			session.setAttribute("last_login", last_login);

			String url = "/hotels";
			response.getWriter().println("login OK! Database updated.");
			response.sendRedirect(url);
		}
		else { // if something went wrong
			String url = "/login?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url); // send a get request  (redirect to the same path)
		}
	}
	/** Writes an HTML form that shows two textfields and a button to the PrintWriter */
	private void displayForm(PrintWriter out) {
		assert out != null;
		
		out.println("<form action=\"/login\" method=\"post\" class=\"form-inline\"> "); // the form will be processed by POST
		out.println("<input type=\"text\" name=\"user\" class=\"input-small\" placeholder=\"Username\">");
		out.println("<input type=\"password\" name=\"pass\" class=\"input-small\" placeholder=\"Password\">");
		out.println("<button type=\"submit\" class=\"btn\">Sign In </button>");
		out.println("</form>");
	}
}