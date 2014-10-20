package me.stuartdouglas.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.stuartdouglas.lib.CassandraHosts;
import me.stuartdouglas.lib.Convertors;
import me.stuartdouglas.models.User;
import me.stuartdouglas.stores.UserSession;

import com.datastax.driver.core.Cluster;

/**
 * Servlet implementation class Account
 */

public class Account extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Cluster cluster;
	private HashMap<String, Integer> CommandsMap = new HashMap<String, Integer>();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Account() {
        super();
        CommandsMap.put("account", 1);
        CommandsMap.put("editprofile", 2);
        CommandsMap.put("editpassword", 2);
        
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        cluster = CassandraHosts.getCluster();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String args[] = Convertors.SplitRequestPath(request);
        int command;
        try {
            command = (Integer) CommandsMap.get(args[1]);
        } catch (Exception et) {
            error("Bad Operator", response);
            return;
        }
        switch (command) {
            case 1:
            	displayAccount(args[1], request, response);
            	System.out.println("Display user settings options");
                break;
            case 2:
            	displayAccountDetails(args[1], request, response);
            	System.out.println("Displaying options for editing");
                break;
            case 3:
            	editPassword(args[1], request, response);
                break;
            default:
                error("Bad Operator", response);
        }
		
	}
	
	
	private void displayAccount(String args, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User tm = new User();
        tm.setCluster(cluster); 
        String Username = request.getSession().getAttribute("user").toString();
        
        LinkedList<UserSession> lsUser = tm.getUserInfo(Username);
		request.setAttribute("UserInfo", lsUser);
		RequestDispatcher rd = request.getRequestDispatcher("/account.jsp");
        
        rd.forward(request, response);
        
	}

	private void displayAccountDetails(String args, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
        RequestDispatcher rd = request.getRequestDispatcher("/account/userdetails.jsp");

        rd.forward(request, response);
        
	}
	
	private void error(String mess, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = null;
        out = new PrintWriter(response.getOutputStream());
        out.println("<h1>You have an a error in your input</h1>");
        out.println("<h2>" + mess + "</h2>");
        out.close();
        return;
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String args[] = Convertors.SplitRequestPath(request);
        int command;
        try {
            command = (Integer) CommandsMap.get(args[1]);
        } catch (Exception et) {
            error("Bad Operator", response);
            return;
        }
        switch (command) {
            case 1:
            	//displayAccountDetails(request, response);
            	//System.out.println("derp");
            	editAccountDetails(request, response);
                break;
            case 2:
                //editAccountDetails(args[1], request, response);
            	System.out.println("User has attempted to change details");
                break;
            case 3:
                //DisplayImage(Convertors.DISPLAY_THUMB,args[1],  response);
                System.out.println("nerP");
            	break;
            default:
                error("Bad Operator", response);
        }
	}
	
	private void editAccountDetails (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Editing user informations");
		String newUsername = request.getParameter("username");
		String previousUsername = request.getParameter("previousUsername");
		String newFname = request.getParameter("fname");
		String previousFname = request.getParameter("previousFname");
		String newLname = request.getParameter("lname");
		String previousLname = request.getParameter("previousLname");
		String password = request.getParameter("password");
		String username = request.getSession().getAttribute("user").toString();
		String location = request.getParameter("location");
		
		//If statement commented out for revision: caused error
		
		//if (newUsername.equals(previousUsername) && newFname .equals (previousFname) && newLname .equals (previousLname)) {
		//				System.out.println("No changes to be made");
		//} else {
			System.out.println("Changes to be made");
			User tm = new User();
	        tm.setCluster(cluster); 
	        boolean isValidUser = tm.IsValidUser(username, password);
	        if (isValidUser) {
	        	try {
	        		tm.updateUserDetails(username, newFname, newLname, location);
	        		response.sendRedirect("/instashutter/account");
	        	} catch (Exception e) {
		        	System.out.println("Error: " + e);
		        }
	        	
	        } else {
	        	System.out.println("Invalid user details");
	        }
	        
		//}
		
		//System.out.println("Username: " + previousUsername + " > " + newUsername  + "\nFirst name: " + previousFname + " > " + newFname + "\nLast name: " + previousLname + " > " +  newLname);
	}
	
	private void editPassword (String username, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("time to change your password");
	}

}
