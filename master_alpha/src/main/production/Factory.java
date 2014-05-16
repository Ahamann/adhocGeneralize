package main.production;


import java.io.IOException;
import main.production.reader.GeoJsonReader;
import main.production.writer.GeoJsonWriter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import com.vividsolutions.jts.geom.*;


/**
 * Factory Class - used for back end operations. read polygons from original json file to create r-tree structure.
 * Here should be everything to prepare data structure
 * @author Bernd Grafe
 *
 */
public class Factory {

	String jsonString = "";
	Polygon[] jsonPolygons;
	String name = "water";
	String type = "FeatureCollection";
	Integer mode = 0;
	
	String pathCopy = "C:\\jsonTempFolder\\test.json";	
	String pathOrig = "C:\\Users\\Ahamann\\Desktop\\MASTER_Topo\\workspace\\git_repo\\Generalize\\master_alpha\\WebContent\\data\\lakesGeo.json";
	//TODO: path need to be changeable 
	
	
	//////////////CONSTRUCTER//////////////
	/**
	 * Constructor - TEST read file, convert, write file
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Factory (Integer mode) throws JsonParseException, JsonMappingException, IOException{	
		this.mode=mode;
		//only for test purposes
		switch(mode){
		case 0: //read original JSON File and Create Polygons
			System.out.println("read file");
			jsonString = readFile();
			System.out.println("json 2 polygon");
			json2polygons();
			System.out.println("done");
		case 1: //read original JSON File,Create Polygons, write into new JSON File 1:1
			System.out.println("read file");
			jsonString = readFile();
			System.out.println("json 2 polygon");
			json2polygons();
			System.out.println("polygon 2 json");
			generateJsonFile();	
			System.out.println("done");
		}
		
	}
	/**
	 * Constructor - empty Constructor to orchestrate methods by yourself
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Factory () throws JsonParseException, JsonMappingException, IOException{	
	}
	
	
	//////////////METHOD SECTION//////////////
	/**
	 * reads json file and saves polygons
	 * json node -> feature node -> coord values -> Coordinate -> Coordinate[] -> LinearRing -> Polygon -> Polygon[]
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void json2polygons() throws JsonParseException, JsonMappingException, IOException{
		
		jsonPolygons = PolygonWorker.json2polygons(jsonString);
		
	}
		
	
	//////////////Reader/Writer//////////////
	/**
	 * returns JSON for given path
	 * @return
	 */
	public String readFile(){
		String jsonFile = GeoJsonReader.readFile(pathOrig);		
		return jsonFile;
	}
	
	/**
	 * calls method to write JSON File based on given Polygons
	 * @throws IOException
	 */
	public void generateJsonFile() throws IOException{			
		GeoJsonWriter.writeJson(jsonPolygons, pathCopy, name, type);
	}
	
	//TODO: with r-tree - input= folder with original file, everything else will be automatically
	
	//////////////GETTERS//////////////
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
	/**
	 * sets name/category of features
	 * @return
	 */
	public String getType(){
		return type;
	}
	//////////////GETTERS//////////////
	public void  setName(String name){
		this.name = name;
	}
	/**
	 * sets name/category of features
	 * @return
	 */
	public void setType(String type){
		this.type = type;
	}
		
}
