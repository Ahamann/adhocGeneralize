package main.production;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import main.production.reader.GeoJsonReader;
import main.production.writer.GeoJsonWriter;
import main.save.Container;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Class with STRtree data structure - should be saved in container to get 
 * 
 * @author Bernd Grafe
 *
 */
public class TreeWorker {	
	String saveFolder;	//path to single polygon json files folder
	String jsonPath;    //path to original json file
	String name;  		//name of polygons
	String type; 		//FeatureCollection
	STRtree jsonTree;	//tree to save single json files - need to be set via factory
	//ad hoc attributes
	private STRtree polygonTree;//ad hoc polygon tree for extent
	private Polygon[] polygons; //requested polygon from jsonTree

	/**
	 * Sets mandatory information and can create the jsonTree and saved in container for further requests
	 * @param jsonPath
	 * @param saveFolder
	 * @param name
	 * @param type
	 * @param createTree
	 * @throws IOException
	 */
	public TreeWorker (String jsonPath, String saveFolder, String name, String type, boolean createTree, boolean save) throws IOException{
		this.jsonPath = jsonPath;
		this.saveFolder = saveFolder;
		this.name = name;
		this.type = type;
		if(createTree)createJsonTree();
		if(save)saveTreeWorker();
		//experimental - save=false - setPolygonTree - saveTreeWorker -<saves polygonTree in Container (memory)
	}

	/**
	 * Creates Tree for JSON Files
	 * @throws IOException
	 */
	public void createJsonTree () throws IOException{
		String path = "";
		String jsonString;
		jsonTree = new STRtree();
		//create folder
		try{
			new File(saveFolder).mkdirs();
		}catch(Exception e){
			System.out.println("couldnt create folder: " + e);
		}
		//read original json file to get polygons
		jsonString = GeoJsonReader.readFile(jsonPath);
		//convert String to Polygon[]
		polygons = PolygonWorker.json2polygons(jsonString);
		//write every polygon in separate json file and save envelope + path in json Tree
		for(int i=0;i<polygons.length;i++){
			path = saveFolder+"\\"+i+".json";
			GeoJsonWriter.writeJsonToFile(polygons[i], path, name, type, i);
			jsonTree.insert(polygons[i].getEnvelopeInternal(), path);
		}
		jsonTree.build(); //TODO:is build necessary for RTREE?
		polygons = null;
		}
	
	/**
	 * saves this TreeWorker in Container
	 */
	public void saveTreeWorker(){
		Container.addTreeWorker(this);
	}
	
	/**
	 * returns all polygons
	 * @return polygons
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Polygon[] getPolygons() throws JsonParseException, JsonMappingException, IOException{
		Envelope env = new Envelope(-180,180,-180,180); //TODO: check if this is Extent for whole map
		getPolygons(env);
		return polygons;
	}
	
	/**
	 * returns polygons based on extent
	 * @param env
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public Polygon[] getPolygons(Envelope env) throws JsonParseException, JsonMappingException, IOException{
		setPolygon(true,env);
		return polygons;
	}
	
	/**
	 * returns tree with all polygons - could be used instead of array for further operations
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public STRtree getTree() throws JsonParseException, JsonMappingException, IOException{
		Envelope env = new Envelope(-180,180,-180,180); //TODO: check if this is Extent for whole map
		getTree(env);
		return polygonTree;
	}
	
	/**
	 * returns tree with polygons based on extent - could be used instead of array for further operations
	 * @param env
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public STRtree getTree(Envelope env) throws JsonParseException, JsonMappingException, IOException{
		setPolygon(false,env);
		return polygonTree;
	}
	
	/**
	 * creates polygon array or tree
	 * @param getPoly
	 * @param env
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private void setPolygon(boolean getPoly, Envelope env) throws JsonParseException, JsonMappingException, IOException{
		@SuppressWarnings("unchecked")
		//get json file paths for given extent
		List<String> paths = jsonTree.query(env);
		//reset/create new polygons array or new tree
		if(getPoly)polygons = new Polygon[paths.size()];
		else polygonTree = new STRtree();
		//temporary path+polygon
		String jsonPathTemp;
		Polygon polygonTemp;
		//get all polygons
		for(int i=0;i<paths.size();i++){
			jsonPathTemp = GeoJsonReader.readFile(paths.get(i));
			polygonTemp = PolygonWorker.json2polygons(jsonPathTemp)[0];
			//add polygon to array or tree
			if(getPoly)polygons[i]=polygonTemp;
			else polygonTree.insert(polygonTemp.getEnvelopeInternal(), polygonTemp);
		}
	}
		

	/**
	 * experimental - set polygonTree on startup - saves time to read jsonTree and creating polygons - but could bring some memory issues
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void setPolygonTree() throws JsonParseException, JsonMappingException, IOException{
		Envelope env = new Envelope(-180,180,-180,180); //TODO: check if this is Extent for whole map
		setPolygon(false,env);
	}
	/**
	 * experimental - get the whole polygonTree and query for yourself
	 * @return
	 */
	public STRtree getpolygonTree(){
		return polygonTree;
	}

	public String getSaveFolder() {
		return saveFolder;
	}

	public void setSaveFolder(String saveFolder) {
		this.saveFolder = saveFolder;
	}

	public String getJsonPath() {
		return jsonPath;
	}

	public void setJsonPath(String jsonPath) {
		this.jsonPath = jsonPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
