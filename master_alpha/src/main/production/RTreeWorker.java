package main.production;

import java.io.File;
import java.io.IOException;
import java.util.List;

import main.production.reader.GeoJsonReader;
import main.production.writer.GeoJsonWriter;
import main.save.Container;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Class to save polygons/path to json file with polygon in a RTree
 * 
 * @author Bernd Grafe
 *
 */
public class RTreeWorker {

	Polygon[] polygons; 
	String saveFolder;
	STRtree tree;
	/**
	 * Constructor creates RTree for given Polygon and saves in given folder
	 * @param polygons
	 * @param saveFolder
	 * @throws IOException 
	 */
	public RTreeWorker (Polygon[] polygons, String saveFolder) throws IOException{
		this.polygons = polygons;
		this.saveFolder = saveFolder;
		createRTree();
	}
	
	/**
	 * empty constructer to call getPolygons
	 */
	public RTreeWorker () {
		
	}
	
	/**
	 * saves polygon to file, path to r-tree
	 * @throws IOException
	 */
	public void createRTree () throws IOException{
		String name = "water";
		String type = "FeatureCollection";  //TODO: fixed strings
		String path = "";
		
		tree = new STRtree();
		//create folder
		try{
			new File(saveFolder).mkdirs();
		}catch(Exception e){
			System.out.println("couldnt create folder: " + e);
		}
			
		for(int i=0;i<polygons.length;i++){
			path = saveFolder+"\\"+i+".json";
			GeoJsonWriter.writeJsonSingle(polygons[i], i, path, name, type);
			tree.insert(polygons[i].getEnvelopeInternal(), path);
		}
		
		Container.setTreeSaveTest(tree);
		System.out.println("depth="+tree.depth()+"  size="+tree.size()+" saved in container");
	}
	
	/**
	 * get Polygons based container rtree, returns json string of all polygons in tree
	 * @return
	 * @throws IOException 
	 */
	public String getAllPolygonsFromRTree() throws IOException{
		tree = Container.getTreeSaveTest();
		if(tree==null || tree.size()==0){
			System.out.println("tree is empty - create new tree ");
			Factory lab = new Factory(0);
			String folder = "C:\\jsonTempFolder\\rtree";	
			Polygon[] polygons = lab.getPolygons();
			this.polygons = polygons;
			this.saveFolder = folder;
			createRTree();
		}
		Envelope env = new Envelope(-180,180,-180,180);
		@SuppressWarnings("unchecked")
		List<String> paths = tree.query(env);
		polygons=new Polygon[paths.size()];
		String jsonTemp;
		Polygon polygonTemp;
		for(int i=0;i<paths.size();i++){
			jsonTemp = GeoJsonReader.readFile(paths.get(i));
			polygonTemp = PolygonWorker.json2polygons(jsonTemp)[0];
			polygons[i]=polygonTemp;
		}
		String test = GeoJsonWriter.writeJsonString(polygons, "test", "FeatureCollection");
		return test;
	}
	
	public String getPolygonsFromRTree(Envelope env) throws IOException{
		//env (minx,maxx,miny,maxy)))
		
		tree = Container.getTreeSaveTest();
		if(tree==null || tree.size()==0){
			System.out.println("tree is empty - create new tree ");
			Factory lab = new Factory(0);
			String folder = "C:\\jsonTempFolder\\rtree";	
			Polygon[] polygons = lab.getPolygons();
			this.polygons = polygons;
			this.saveFolder = folder;
			createRTree();
		}
		
		@SuppressWarnings("unchecked")
		List<String> paths = tree.query(env);
		polygons=new Polygon[paths.size()];
		String jsonTemp;
		Polygon polygonTemp;
		for(int i=0;i<paths.size();i++){
			jsonTemp = GeoJsonReader.readFile(paths.get(i));
			polygonTemp = PolygonWorker.json2polygons(jsonTemp)[0];
			polygons[i]=polygonTemp;
		}
		String test = GeoJsonWriter.writeJsonString(polygons, "test", "FeatureCollection");
		return test;
	}
	
}
