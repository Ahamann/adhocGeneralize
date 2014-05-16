package main.controller;

import main.production.reader.GeoJsonReader;

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
	 */
	public RequestHandler(Integer mode){
		this.operationMode = mode;
		
		switch(operationMode){
		case 0: //standard for test - read 1 jsonFile
			jsonFile2String(pathCopy);

		}
	}
	
	//////////////METHOD SECTION//////////////
	/**
	 * simply read File and save String as jsonString
	 * @param path
	 */
	public void jsonFile2String (String path){
		System.out.println(path);
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
