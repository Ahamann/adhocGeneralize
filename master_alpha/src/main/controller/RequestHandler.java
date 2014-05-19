package main.controller;

import java.io.IOException;

import main.production.RTreeWorker;
import main.production.reader.GeoJsonReader;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class handles front end requests, gets polygons based on extent+zoom level and generalizes them based on chosen operation.
 * Ad-hoc generalization etc.-
 * @author Bernd Grafe
 *
 */
public class RequestHandler {

	String jsonString = ""; //is send to front end
	Polygon[] jsonPolygons; //intern usage for operations
	Integer operationMode = 0; 
	
	String pathCopy = "C:\\jsonTempFolder\\test.json";	
	String pathOrig = "C:\\Users\\Ahamann\\Desktop\\MASTER_Topo\\workspace\\git_repo\\Generalize\\master_alpha\\WebContent\\data\\lakesGeo.json";
	//TODO: path need to be changeable 
	
	
	//////////////CONSTRUCTER//////////////
	/**
	 * Constructor based on given mode
	 * @param mode
	 * @throws IOException 
	 */
	public RequestHandler(Integer mode) throws IOException{
		this.operationMode = mode;
		
		switch(operationMode){
		case 0: //standard for test - read 1 jsonFile
			jsonFile2String(pathCopy);
			break;
		case 1: //read all polygons from saved r-tree
			RTreeWorker tree = new RTreeWorker();
			jsonString = tree.getAllPolygonsFromRTree();
			break;
		case 2: //read all polygons from saved r-tree
			Envelope env = new Envelope(4.50 , 5, 45.98 , 45.985);
			RTreeWorker treeEnv = new RTreeWorker();
			jsonString = treeEnv.getPolygonsFromRTree(env);
			break;

		}
	}
	
	/**
	 * request based on envelope
	 * @param env
	 * @throws IOException
	 */
	public RequestHandler(Envelope env) throws IOException{
		RTreeWorker treeEnv = new RTreeWorker();
		jsonString = treeEnv.getPolygonsFromRTree(env);
	}
	
	//////////////METHOD SECTION//////////////
	/**
	 * simply read File and save String as jsonString
	 * @param path
	 */
	public void jsonFile2String (String path){
		jsonString = GeoJsonReader.readFile(path);
	}
	
	//////////////GETTERS//////////////
	/**
	 * returns jsonString - operation needed befor call
	 * @return
	 */
	public String getjsonString(){
		return jsonString;
	}
	
	
}
