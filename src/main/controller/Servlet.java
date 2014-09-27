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
 * Servlet implementation class Servlet, handles requests
 * @author Bernd Grafe
 */
@WebServlet("/Servlet")
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Servlet() {
        super();
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
		String scaleString = (String) request.getParameter("scale");
		String maxTypifyString = (String) request.getParameter("maxTypify");
		String fixElementsString = (String) request.getParameter("fixElements");
		String minAreaString = (String) request.getParameter("minArea");
		String minDistanceString = (String) request.getParameter("minDistance");
		String speed = (String) request.getParameter("speed");
		String typmode = (String) request.getParameter("typmode");
		String weight = (String) request.getParameter("weight");
		String union = (String) request.getParameter("union");
		String simplify = (String) request.getParameter("simplify");

			String json = RequestHandler.getJson(modeString, minxString, minyString, maxxString, maxyString, scaleString, maxTypifyString, fixElementsString, minAreaString, minDistanceString,speed,typmode,weight,union,simplify);
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			//gzip
			//response.setHeader("Content-Encoding", "gzip");
			//PrintWriter out = new PrintWriter(new GZIPOutputStream(response.getOutputStream()));//response.getWriter();
			//!!!gzip doesnt send a valid response - its just a part of json file
			PrintWriter out = response.getWriter();
			System.out.println("Total time before sending : "+ (double)requestTime.getElapsedTime() /1000+" s");
		    out.println(json);
		    requestTime.stop();
		    System.out.println("Total time for request (backend) : "+ (double)requestTime.getElapsedTime() /1000+" s");
		    System.out.println("//////////////////");  
		}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
