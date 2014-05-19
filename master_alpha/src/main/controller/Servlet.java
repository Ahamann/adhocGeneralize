package main.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Servlet implementation class Servlet
 */
@WebServlet("/Servlet")
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Servlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String modeString = (String) request.getParameter("mode");
		int mode = Integer.parseInt(modeString);
		
		if ( mode==3){
			String minxString = (String) request.getParameter("minx");
			double minx = Double.parseDouble(minxString);
			String minyString = (String) request.getParameter("miny");
			double miny = Double.parseDouble(minyString);
			String maxxString = (String) request.getParameter("maxx");
			double maxx = Double.parseDouble(maxxString);
			String maxyString = (String) request.getParameter("maxy");
			double maxy = Double.parseDouble(maxyString);
			Envelope env = new Envelope(minx,maxx,miny,maxy);
			RequestHandler reqHandler = new RequestHandler(env);  //1 - all polygons from rtree
			String json = reqHandler.getjsonString();
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
		    out.println(json);
		}else {
			
			
			//RequestHandler reqHandler = new RequestHandler(0);  //TODO: 0 - test purposes - changeable via front end
			//String json = reqHandler.getjsonString();			//TODO: getPram: extent, zoom level, operation
			
			RequestHandler reqHandler = new RequestHandler(2);  //1 - all polygons from rtree
			String json = reqHandler.getjsonString();			//2 - polygons based on envelope
			
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
		    out.println(json);
		}	
			
		}

		

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
