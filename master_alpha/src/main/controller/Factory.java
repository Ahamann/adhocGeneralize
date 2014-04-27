package main.controller;

import java.io.IOException;

import main.reader.geoJsonReader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.JsonNodeFactory;

import com.vividsolutions.jts.geom.*;


/**
 * Class to create poylgons from json file
 * @author Bernd Grafe
 *
 */
public class Factory {

	String jsonString = "";
	Polygon[] jsonPolygons;
	String name;
	

	/**
	 * Constructor - just reads File (Json) - saves as String
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Factory (String mode) throws JsonParseException, JsonMappingException, IOException{	
		if(mode.equals("read/create")){
		jsonString = geoJsonReader.readFile();
		json2polygons();
		} else if (mode.equals("write")){
		generateJsonFile();	
		}
	}
	/**
	 * Constructor - just reads File (Json) - saves as String
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Factory () throws JsonParseException, JsonMappingException, IOException{	
		jsonString = geoJsonReader.readFile();
		json2polygons();
		
	}
	
	/**
	 * reads json file and saves polygons
	 * json node -> feature node -> coord values -> Coordinate -> Coordinate[] -> LinearRing -> Polygon -> Polygon[]
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void json2polygons() throws JsonParseException, JsonMappingException, IOException{
		
		ObjectMapper m = new ObjectMapper();
		JsonNode fullNode = null; //node of full json document
		JsonNode featureNode = null; //node of features []
		GeometryFactory geometryFactory = new GeometryFactory(); //factory to create geometries
				
		//read json string as tree structure
		fullNode = m.readTree(jsonString);
		featureNode = fullNode.path("features");
		name = fullNode.path("name").asText();
		
		//outer loop for features/polygons
		jsonPolygons = new Polygon[featureNode.size()];
		for (int i= 0; i< featureNode.size() ; i++){ //TODO: length of loop
			JsonNode coordNode;
			coordNode = featureNode.get(i).findValue("coordinates").get(0);
			
			//inner loop for coordinates of feature/polygon
			Coordinate[] tempCoords = new Coordinate[coordNode.size()];
			System.out.println(coordNode.size());
			for (int j = 0; j< coordNode.size();j++){
					//if ( coordNode.get(j)!=null){

					double x = coordNode.get(j).get(0).asDouble();
					double y = coordNode.get(j).get(1).asDouble();
					
					
					Coordinate tempCoord = new Coordinate(x,y);
					tempCoords[j] = tempCoord;
					//}		
			}
			LinearRing shell = geometryFactory.createLinearRing(tempCoords);
			Polygon tempPoly = geometryFactory.createPolygon(shell, null);
			jsonPolygons[i] = tempPoly;
		}
	}
	
	public void generateJsonFile(){
		ObjectWriter bla;
		JsonNodeFactory la;
	}
	
	/**
	 * returns json string
	 * @return
	 */
	public String getJSON(){
		return jsonString;
	}
	
	/**
	 * returns polygons created from json
	 * @return
	 */
	public Polygon[] getPolygons(){
		return jsonPolygons;
	}
	
	/**
	 * returns name/category of features
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	
}
