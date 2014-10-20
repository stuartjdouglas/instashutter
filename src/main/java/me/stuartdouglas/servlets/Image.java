package me.stuartdouglas.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.datastax.driver.core.Cluster;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import me.stuartdouglas.lib.CassandraHosts;
import me.stuartdouglas.lib.Convertors;
import me.stuartdouglas.models.PicModel;
import me.stuartdouglas.models.User;
import me.stuartdouglas.stores.UserSession;
import me.stuartdouglas.stores.Pic;

/**
 * Servlet implementation class Image
 */

@WebServlet(urlPatterns = {
	    "/Image",
	    "/Image/*",
	    "/Thumb/*",
	    "/Images",
	    "/Images/*"
	})
@MultipartConfig
public class Image extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private Cluster cluster;
    private HashMap<String, Integer> CommandsMap = new HashMap<String, Integer>();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
    public Image() {
        super();
        // TODO Auto-generated constructor stub
        CommandsMap.put("Image", 1);
        CommandsMap.put("Images", 2);
        CommandsMap.put("Thumb", 3);
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
                DisplayImage(Convertors.DISPLAY_PROCESSED,args[2], response);
                break;
            case 2:
                DisplayImageList(args[2], request, response);
                break;
            case 3:
                DisplayImage(Convertors.DISPLAY_THUMB,args[2],  response);
                break;
            default:
                error("Bad Operator", response);
        }
	}
	
	
	private void DisplayImageList(String User, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PicModel tm = new PicModel();
        tm.setCluster(cluster);
        java.util.LinkedList<Pic> lsPics = tm.getPicsForUser(User);
        RequestDispatcher rd = request.getRequestDispatcher("/UserPics.jsp");
        request.setAttribute("Pics", lsPics);
        rd.forward(request, response);

    }
	
	private void DisplayImage(int type,String Image, HttpServletResponse response) throws ServletException, IOException {
        PicModel tm = new PicModel();
        tm.setCluster(cluster);
        Pic p = tm.getPic(type,java.util.UUID.fromString(Image));
        
        OutputStream out = response.getOutputStream();

        response.setContentType(p.getType());
        response.setContentLength(p.getLength());
        //out.write(Image);
        InputStream is = new ByteArrayInputStream(p.getBytes());
        BufferedInputStream input = new BufferedInputStream(is);
        byte[] buffer = new byte[8192];
        for (int length = 0; (length = input.read(buffer)) > 0;) {
            out.write(buffer, 0, length);
        }
        out.close();
    }

	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
		Part file = request.getPart("file");
		
		String description = request.getParameter("description");
		String type = file.getContentType();
        String filename = file.getSubmittedFileName();
		
		InputStream is = request.getPart(file.getName()).getInputStream();
        int i = is.available();
        HttpSession session=request.getSession();
        UserSession lg= (UserSession)session.getAttribute("LoggedIn");
        String username="null";
        if (lg.getUserSession()){
            username=lg.getUsername();
        }
        if (i > 0) {
            byte[] b = new byte[i + 1];
            is.read(b);
            System.out.println("Length: " + b.length);
            System.out.println("Title: " + description);
            PicModel tm = new PicModel();
            tm.setCluster(cluster);

            try {
            	tm.insertPic(b, type, filename, username, description);
            } catch (Exception e) {
    			System.out.println("Error with uploading file: " + e);
    		}

            is.close();
        }
         System.out.println("ended");
		} catch (Exception e) {
			System.out.println("Error processing upload request: " + e);
		}
		response.sendRedirect("/instashutter/upload");
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
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Attempted Delete");
	}

}
