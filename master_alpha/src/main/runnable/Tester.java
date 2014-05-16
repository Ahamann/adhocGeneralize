package main.runnable;
import java.io.IOException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import main.production.Factory;

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
		Factory lab = new Factory(mode);

	}

}
