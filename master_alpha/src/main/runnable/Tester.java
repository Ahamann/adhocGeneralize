package main.runnable;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.vividsolutions.jts.geom.Polygon;

import main.production.Factory;
import main.production.TreeWorker;
import main.production.writer.GeoJsonWriter;
import main.save.Container;

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
		
		String pathOrig = Container.pathOrig;
		String pathFolder = Container.pathFolder;
		String name = Container.name;
		String type = Container.type;
		int mode = 0; // normal
		//create Tree
		Factory.createTree(mode, pathOrig, pathFolder, name, type);
		//test Tree
		List<TreeWorker> list = Container.getsavedTrees();
		
		System.out.printf("saved "+list.size()+ " treeWorker");
		System.out.printf("Json String for first Worker:");
		Polygon[] polys= list.get(0).getPolygons();
		String jsonString;		
		jsonString= GeoJsonWriter.getJsonString(polys, list.get(0).getName(), list.get(0).getType());
		System.out.println(jsonString);
	}
}
