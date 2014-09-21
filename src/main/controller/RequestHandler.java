package main.controller;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import main.production.Factory;
import main.production.PolygonWorker;
import main.production.TreeWorker;
import main.production.writer.GeoJsonWriter;
import main.save.Container;
import main.save.TempParameterContainer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Class handles front end requests, gets polygons based on extent+zoom level and generalizes them based on chosen operation.
 * Ad-hoc generalization etc.-
 * @author Bernd Grafe
 *
 */
public class RequestHandler {

	/**
	 * get all Polygons
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static String getJson() throws JsonParseException, JsonMappingException, IOException{
		String jsonString;
		List<TreeWorker> list = Container.getsavedTrees();
		
		
		if(list.size()==0){
			String pathOrig = Container.pathOrig;
			String pathFolder = Container.pathFolder;
			String name = Container.name;
			String type = Container.type;
			//create Tree
			Factory.createTree(0, pathOrig, pathFolder, name, type);
			list = Container.getsavedTrees();
		}
		
		TreeWorker treeW = list.get(0);
		Polygon[] polys = treeW.getPolygons();
		jsonString= GeoJsonWriter.getJsonString(polys, treeW.getName(), treeW.getType());	
		return jsonString;
	}
	
	/**
	 * getPolygons based on extent
	 * @param modeS
	 * @param minxS
	 * @param minyS
	 * @param maxxS
	 * @param maxyS
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static String getJson(String modeS, String minxS, String minyS, String maxxS, String maxyS, String scaleS) throws JsonParseException, JsonMappingException, IOException{
		int mode = 0;
		double scale = 0; 
		double minx =-180;
		double miny =-180;
		double maxx =180;
		double maxy =180;
		String jsonString = "";	
		TempParameterContainer parameter;
		//set parameters
		try{
			mode = Integer.parseInt(modeS);
			scale= Double.parseDouble(scaleS);
			minx = Double.parseDouble(minxS);
			miny = Double.parseDouble(minyS);
			maxx = Double.parseDouble(maxxS);
			maxy = Double.parseDouble(maxyS);
			
		}catch(Exception e){
			System.out.println("could not parse parameter input: "+e.getMessage());
		}
		//save parameters
		parameter = new TempParameterContainer(mode, scale, minx, miny,maxx,maxy);
		//parameter.setSpeed(360);

		//get TreeWorker - just 1 for test
		List<TreeWorker> list = Container.getsavedTrees();
		TreeWorker treeW = list.get(0);

		//switch between generalization modes
		switch(mode){
		case 0:
			//normal request - get polygons based on extent	
			Polygon[] polys = treeW.getPolygons(parameter.getEnv());
			jsonString= GeoJsonWriter.getJsonString(polys, treeW.getName(), treeW.getType());
			break;
		case 1:
			//SELECTION
			Polygon[] arrayPoly1 = treeW.getPolygons(parameter.getEnv());
			List<Polygon> listPoly1 = PolygonWorker.useSelection(arrayPoly1, parameter.getEnv(), parameter.getMaxElementsSel());
			jsonString= GeoJsonWriter.getJsonString(listPoly1, treeW.getName(), treeW.getType());
			break;
		case 2:
			//Strange Typification 
			Polygon[] arrayPoly2 = treeW.getPolygons(parameter.getEnv());
			List<Polygon> listPoly2 = PolygonWorker.useTypification(arrayPoly2, parameter.getEnv());
			jsonString= GeoJsonWriter.getJsonString(listPoly2, treeW.getName(), treeW.getType());
			break;
		case 3:
			//Typification - nearest neighbour
			STRtree requestTree = treeW.getTree(parameter.getEnv());
			List<Polygon> listPoly3 = PolygonWorker.useNearestNeighborTypification(requestTree, parameter.getEnv(), parameter.getMaxElementsTyp());
			jsonString= GeoJsonWriter.getJsonString(listPoly3, treeW.getName(), treeW.getType());
			break;
		case 4:
			//Typification nN with pre selection
			Polygon[] arrayPoly4 = treeW.getPolygons(parameter.getEnv());
			List<Polygon> listPoly4 = PolygonWorker.useSelection(arrayPoly4, parameter.getEnv(), parameter.getMaxElementsSel());
			List<Polygon> listPoly4_2 = PolygonWorker.useNearestNeighborTypification(listPoly4, parameter.getEnv(), parameter.getMaxElementsTyp());
			listPoly4_2 = PolygonWorker.mergeOverlaps(listPoly4_2);
			jsonString= GeoJsonWriter.getJsonString(listPoly4_2, treeW.getName(), treeW.getType());
		}
		return jsonString;	
	}
}
