package main.controller;

import java.io.File;
import java.io.IOException;

import main.reader.geoJsonReader;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
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
	String type = "FeatureCollection";
	

	/**
	 * Constructor -
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Factory (String mode) throws JsonParseException, JsonMappingException, IOException{	
		if(mode.equals("read/create")){
		jsonString = geoJsonReader.readFile();
		json2polygons();
		} else if (mode.equals("write")){
			System.out.println("read file");
			jsonString = geoJsonReader.readFile();
			System.out.println("json 2 polygon");
			json2polygons();
			System.out.println("polygon 2 json");
			generateJsonFile();	
			System.out.println("done");
		}
	}
	/**
	 * Constructor - just reads File (Json) - saves as String and converts to polygon array
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Factory () throws JsonParseException, JsonMappingException, IOException{	
		jsonString = geoJsonReader.readFile();
		//json2polygons();
		
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
	
	public void generateJsonFile() throws IOException{
	
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(new File("c:\\jsonTempFolder\\test.json"), JsonEncoding.UTF8); //write in file
		
		jsonGenerator.writeStartObject(); // "{"
			jsonGenerator.writeStringField("name", name);
			jsonGenerator.writeStringField("type", type);
			jsonGenerator.writeFieldName("features");
				jsonGenerator.writeStartArray(); // "["
				
				for (int i=0; i< jsonPolygons.length; i++){
					//feature element
					jsonGenerator.writeStartObject(); // "{"
						jsonGenerator.writeStringField("type", "Feature");
						jsonGenerator.writeFieldName("geometry");
							jsonGenerator.writeStartObject(); // "{"
							jsonGenerator.writeStringField("type", "Poylgon");
							jsonGenerator.writeFieldName("coordinates");
								jsonGenerator.writeStartArray(); // "["
									jsonGenerator.writeStartArray(); // "["
									
									Coordinate[] tempCoords = jsonPolygons[i].getCoordinates();
									for(int j=0; j< tempCoords.length;j++ ){
										jsonGenerator.writeStartArray(); // "[" -x,y values incl z(default 0 - could be used for zoom level)
											jsonGenerator.writeNumber(tempCoords[j].x);
											jsonGenerator.writeNumber(tempCoords[j].y);
											jsonGenerator.writeNumber(0);
										jsonGenerator.writeEndArray(); // "]"
									}
									jsonGenerator.writeEndArray(); // "]"
								jsonGenerator.writeEndArray(); // "]"
							jsonGenerator.writeEndObject(); // "}"
							jsonGenerator.writeFieldName("properties");
								jsonGenerator.writeStartObject(); // "{"
								jsonGenerator.writeStringField("polygon_from", name);
								jsonGenerator.writeNumberField("poly_id",i);
								jsonGenerator.writeNumberField("area",jsonPolygons[i].getArea());
								jsonGenerator.writeNumberField("length",jsonPolygons[i].getLength());
								jsonGenerator.writeEndObject(); // "}"
					jsonGenerator.writeEndObject(); // "}"
				}
					
					//feature element
					jsonGenerator.writeStartObject(); // "{"
						jsonGenerator.writeStringField("type", "Feature");
						jsonGenerator.writeFieldName("geometry");
							jsonGenerator.writeStartObject(); // "{"
							jsonGenerator.writeStringField("type", "Poylgon");
							jsonGenerator.writeFieldName("coordinates");
								jsonGenerator.writeStartArray(); // "["
									jsonGenerator.writeStartArray(); // "["
										jsonGenerator.writeStartArray(); // "[" -x,y values incl z(default 0 - could be used for zoom level)
											jsonGenerator.writeNumber(21);
											jsonGenerator.writeNumber(22);
											jsonGenerator.writeNumber(0);
										jsonGenerator.writeEndArray(); // "]"
									jsonGenerator.writeEndArray(); // "]"
								jsonGenerator.writeEndArray(); // "]"
							jsonGenerator.writeEndObject(); // "}"
							jsonGenerator.writeFieldName("properties");
								jsonGenerator.writeStartObject(); // "{"
								jsonGenerator.writeStringField("stuff", "more_stuff");
								jsonGenerator.writeNumberField("number",666);
								jsonGenerator.writeEndObject(); // "}"
					jsonGenerator.writeEndObject(); // "}"
					
				//end feature	
				jsonGenerator.writeEndArray(); // "]"
		jsonGenerator.writeEndObject(); // "}"
		
		jsonGenerator.close();
			
		
		//TODO: created json seems to be invalid - after correction still no result on map
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
