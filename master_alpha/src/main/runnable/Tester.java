package main.runnable;





import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import main.controller.Factory;
import main.reader.geoJsonReader;

public class Tester {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		
		//System.out.println("Working Directory = " +
	    //          System.getProperty("user.dir"));
		
		
		
		//String json;
		//json = geoJsonReader.readFile();
		//System.out.println(json);
		

		Factory lab = new Factory("write");
		//lab.temp();
		lab.json2polygons();
	}

}
