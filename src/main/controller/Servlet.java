package main.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.helper.Watch;


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

		Watch requestTime = new Watch();
		requestTime.start();
		
		String modeString = (String) request.getParameter("mode");
		String minxString = (String) request.getParameter("minx");
		String minyString = (String) request.getParameter("miny");
		String maxxString = (String) request.getParameter("maxx");
		String maxyString = (String) request.getParameter("maxy");
		String zoomString = (String) request.getParameter("zoom");
				
		//if ( modeString.equals("0")){
			String json = RequestHandler.getJson(modeString, minxString, minyString, maxxString, maxyString, zoomString);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
		    out.println(json);
		//}else {
		//	String json = RequestHandler.getJson();
		//	response.setContentType("text/html");
		//	PrintWriter out = response.getWriter();
		//	out.println(json);
		//}	
			
		    requestTime.stop();
		    System.out.println("Total time for request (backend) : "+ requestTime.getElapsedTime());
		    
		}

		

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}