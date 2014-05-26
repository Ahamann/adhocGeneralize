package main.production.writer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Collection of static methods to write files (JSON)
 * @author Bernd Grafe
 *
 */
public class GeoJsonWriter {

	/**
	 * writes a GeoJson/Json File for several polygons
	 * 
	 * @param jsonPolygons
	 * @param path
	 * @param name
	 * @param type
	 * @throws IOException
	 */
	public static void writeJsonToFile(Polygon[] jsonPolygons, String path, String name, String type) throws IOException{
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(new File(path), JsonEncoding.UTF8); //write in file
		
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
							jsonGenerator.writeStringField("type", "Polygon");
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
					
				//end feature	
				jsonGenerator.writeEndArray(); // "]"
		jsonGenerator.writeEndObject(); // "}"
		
		jsonGenerator.close();

	}
	
	

	
	
	/**
	 * writes Json File for 1 Polygon (used for RTree - 1 polygon/1 node/ 1 file)
	 * 
	 * @param jsonPolygons
	 * @param path
	 * @param name
	 * @param type
	 * @throws IOException
	 */
	public static void writeJsonToFile(Polygon jsonPolygon, String path, String name, String type,int polygonID) throws IOException{
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(new File(path), JsonEncoding.UTF8); //write in file
		jsonGenerator.writeStartObject(); // "{"
			jsonGenerator.writeStringField("name", name);
			jsonGenerator.writeStringField("type", type);
			jsonGenerator.writeFieldName("features");
				jsonGenerator.writeStartArray(); // "["
					//feature element
					jsonGenerator.writeStartObject(); // "{"
						jsonGenerator.writeStringField("type", "Feature");
						jsonGenerator.writeFieldName("geometry");
							jsonGenerator.writeStartObject(); // "{"
							jsonGenerator.writeStringField("type", "Polygon");
							jsonGenerator.writeFieldName("coordinates");
								jsonGenerator.writeStartArray(); // "["
									jsonGenerator.writeStartArray(); // "["
									
									Coordinate[] tempCoords = jsonPolygon.getCoordinates();
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
								jsonGenerator.writeNumberField("poly_id",polygonID);
								jsonGenerator.writeNumberField("area",jsonPolygon.getArea());
								jsonGenerator.writeNumberField("length",jsonPolygon.getLength());
								jsonGenerator.writeStringField("envelope",jsonPolygon.getEnvelopeInternal().toString());
								jsonGenerator.writeEndObject(); // "}"
					jsonGenerator.writeEndObject(); // "}"
				
					
				//end feature	
				jsonGenerator.writeEndArray(); // "]"
		jsonGenerator.writeEndObject(); // "}"
		
		jsonGenerator.close();

	}
	
	
	
	/**
	 * returns json String for given polygon list
	 * @param jsonPolygons
	 * @param name
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static String getJsonString(List<Polygon> jsonPolygons, String name, String type) throws IOException{
		Polygon[] array = jsonPolygons.toArray(new Polygon[jsonPolygons.size()]);
		return getJsonString(array, name, type);
	}
	
	
	
	
	
	
	
	/**
	 * returns json String for given polygon array
	 * @param jsonPolygons
	 * @param name
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static String getJsonString(Polygon[] jsonPolygons, String name, String type) throws IOException{
		Writer writer = new StringWriter();
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer); //write in file

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
							jsonGenerator.writeStringField("type", "Polygon");
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
								jsonGenerator.writeStringField("envelope",jsonPolygons[i].getEnvelopeInternal().toString());
								jsonGenerator.writeEndObject(); // "}"
					jsonGenerator.writeEndObject(); // "}"
				}
					
				//end feature	
				jsonGenerator.writeEndArray(); // "]"
		jsonGenerator.writeEndObject(); // "}"
		
		jsonGenerator.close();
		return writer.toString();

	}
	
	/**
	 * returns json string for given polygon
	 * @param jsonPolygon
	 * @param path
	 * @param name
	 * @param type
	 * @param polygonID
	 * @return
	 * @throws IOException
	 */
	public static String getJsonString (Polygon jsonPolygon, String path, String name, String type,int polygonID) throws IOException{
		Writer writer = new StringWriter();
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer); //write in file
		jsonGenerator.writeStartObject(); // "{"
			jsonGenerator.writeStringField("name", name);
			jsonGenerator.writeStringField("type", type);
			jsonGenerator.writeFieldName("features");
				jsonGenerator.writeStartArray(); // "["
					//feature element
					jsonGenerator.writeStartObject(); // "{"
						jsonGenerator.writeStringField("type", "Feature");
						jsonGenerator.writeFieldName("geometry");
							jsonGenerator.writeStartObject(); // "{"
							jsonGenerator.writeStringField("type", "Polygon");
							jsonGenerator.writeFieldName("coordinates");
								jsonGenerator.writeStartArray(); // "["
									jsonGenerator.writeStartArray(); // "["
									
									Coordinate[] tempCoords = jsonPolygon.getCoordinates();
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
								jsonGenerator.writeNumberField("poly_id",polygonID);
								jsonGenerator.writeNumberField("area",jsonPolygon.getArea());
								jsonGenerator.writeNumberField("length",jsonPolygon.getLength());
								jsonGenerator.writeStringField("envelope",jsonPolygon.getEnvelopeInternal().toString());
								jsonGenerator.writeEndObject(); // "}"
					jsonGenerator.writeEndObject(); // "}"
				
					
				//end feature	
				jsonGenerator.writeEndArray(); // "]"
		jsonGenerator.writeEndObject(); // "}"
		
		jsonGenerator.close();
		return writer.toString();
	}
}
