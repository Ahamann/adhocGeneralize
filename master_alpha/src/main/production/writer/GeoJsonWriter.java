package main.production.writer;

import java.io.File;
import java.io.IOException;

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
	 * writes a GeoJson/Json File
	 * 
	 * @param jsonPolygons
	 * @param path
	 * @param name
	 * @param type
	 * @throws IOException
	 */
	public static void writeJson(Polygon[] jsonPolygons, String path, String name, String type) throws IOException{
		
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
	
}
