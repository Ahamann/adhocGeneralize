package main.runnable;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.vividsolutions.jts.geom.Polygon;

import main.production.Factory;
import main.production.RTreeWorker;

/**
 * Test class to prepare back end data structure
 * @author Bernd Grafe
 *
 */
public class Tester {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		

		
		int mode = 1; // read orig, get polygons, write copy
		//Factory lab = new Factory(mode); 
		
		//create polygons from json file, add to r-tree
		//Factory lab = new Factory(0);
		//String folder = "C:\\jsonTempFolder\\rtree";	
		//Polygon[] polygons = lab.getPolygons();
		
		//rtree constructur to create rtree and save to container
		//RTreeWorker tree = new RTreeWorker(polygons,folder);
		
		//gets polygons+json from rtree
		RTreeWorker tree = new RTreeWorker();
		tree.getAllPolygonsFromRTree();

	}

}
