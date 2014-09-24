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
	public static String getJson(String modeS, String minxS, String minyS, String maxxS, String maxyS, String scaleS, String maxTypifyS, String fixElementsS, String minAreaS, String minDistanceS,String speedS,String typmodeS, String weightS) throws JsonParseException, JsonMappingException, IOException{
		int mode = 0;
		double scale = 0; 
		double minx =-180;
		double miny =-180;
		double maxx =180;
		double maxy =180;
		String jsonString = "";	
		TempParameterContainer parameter;
		
		int maxTyp = 0;
		int maxElements = 0;
		double minArea = 0; // example: 500 m² = 0,0000000005
		double minDist = 0; // example:	200 m = 0.002
		double speed = 320;
		int typmode = 1;  //set modus of typify 0= recreate tree after nn with new polygon - 1 = delete 2 nodes and insert new one after nn
		double weight = 2;
		//new parameter 
		//maxTypifyS
		//fixElementsS
		//minAreaS
		//minDistanceS
		int x =0;
		//set parameters
		try{
			//mandatory input
			mode = Integer.parseInt(modeS);x++;
			scale= Double.parseDouble(scaleS);x++;
			minx = Double.parseDouble(minxS);x++;
			miny = Double.parseDouble(minyS);x++;
			maxx = Double.parseDouble(maxxS);x++;
			maxy = Double.parseDouble(maxyS);x++;
			//optional input
			if(!maxTypifyS.isEmpty())maxTyp = Integer.parseInt(maxTypifyS);x++;
			if(!fixElementsS.isEmpty())maxElements = Integer.parseInt(fixElementsS);x++;
			if(!minAreaS.isEmpty())minArea = Double.parseDouble(minAreaS);x++;
			if(!minDistanceS.isEmpty())minDist = Double.parseDouble(minDistanceS);	x++;
			if(!speedS.isEmpty())speed = Double.parseDouble(speedS);	x++;
			if(!typmodeS.isEmpty())typmode = Integer.parseInt(typmodeS);	x++;
			if(!weightS.isEmpty())weight = Double.parseDouble(weightS);	x++;
		}catch(Exception e){
			System.out.println("could not parse parameter input: "+e.getMessage() + " Pos="+x);
		}
		
		//save parameters
		parameter = new TempParameterContainer(mode, scale, minx, miny,maxx,maxy);
		if(maxTyp!=0)parameter.setTypElements(maxTyp);
		if(maxElements!=0)parameter.setMaxElements(maxElements);
		if(speed!=0)parameter.setSpeed(speed);
		if(minArea!=0)parameter.setMinArea(minArea);
		if(minDist!=0)parameter.setMinDist(minDist);
		if(maxElements!=0)parameter.setMaxElements(maxElements);
		
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
			List<Polygon> listPoly3 = PolygonWorker.useNearestNeighborTypification(requestTree, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode, weight);
			jsonString= GeoJsonWriter.getJsonString(listPoly3, treeW.getName(), treeW.getType());
			break;
		case 4:
			//Typification nN with pre selection and overlaps merge
			Polygon[] arrayPoly4 = treeW.getPolygons(parameter.getEnv());
			List<Polygon> listPoly4 = PolygonWorker.useSelection(arrayPoly4, parameter.getEnv(), parameter.getMaxElementsSel());
			List<Polygon> listPoly4_2 = PolygonWorker.useNearestNeighborTypification(listPoly4, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
			listPoly4_2 = PolygonWorker.mergeOverlaps(listPoly4_2);
			jsonString= GeoJsonWriter.getJsonString(listPoly4_2, treeW.getName(), treeW.getType());
			break;
		case 5:
			//Typification nN with pre selection , overlaps merge and min area selection
			Polygon[] arrayPoly5 = treeW.getPolygons(parameter.getEnv());
			
//			double size=0;
//			for (int a = 0; a<arrayPoly5.length;a++){
//				size +=arrayPoly5[a].getCoordinates().length;
//			}
//			System.out.println("amount of points per polygon:" + size/arrayPoly5.length);
			
			List<Polygon> listPoly5 = PolygonWorker.useSelection(arrayPoly5, parameter.getEnv(), parameter.getMaxElementsSel());
			List<Polygon> listPoly5_2 = PolygonWorker.useNearestNeighborTypification(listPoly5, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
			listPoly5_2 = PolygonWorker.mergeOverlaps(listPoly5_2);
			listPoly5_2 = PolygonWorker.useAreaSelection(listPoly5_2, parameter.getMinArea());			
//			double size2=0;
//			for (int a = 0; a<listPoly5_2.size();a++){
//				size2 +=listPoly5_2.get(a).getCoordinates().length;
//			}
//			System.out.println("amount of points per polygon:" + size2/listPoly5_2.size());
			jsonString= GeoJsonWriter.getJsonString(listPoly5_2, treeW.getName(), treeW.getType());
			double size= jsonString.length()/1024;
			System.out.println("json length ~ " + size +"kb /// estimated download time with "+parameter.getSpeed() +"kbps = "+size*8/parameter.getSpeed() );
			break;
		
		case 6: //get min Rectangle Diameter
		Polygon[] arrayPoly6 = treeW.getPolygons(parameter.getEnv());
		List<Polygon> listPoly6 = PolygonWorker.useSelection(arrayPoly6, parameter.getEnv(), parameter.getMaxElementsSel());
		List<Polygon> listPoly6_2 = PolygonWorker.useNearestNeighborTypification(listPoly6, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
		listPoly6_2 = PolygonWorker.mergeOverlaps(listPoly6_2);
		listPoly6_2 = PolygonWorker.useAreaSelection(listPoly6_2, parameter.getMinArea());
		listPoly6_2 = PolygonWorker.giveDiameter(listPoly6_2);
		jsonString= GeoJsonWriter.getJsonString(listPoly6_2, treeW.getName(), treeW.getType());
		break;
		case 7: //select, union, typify, merge overlaps, deselect small
			
			
			Polygon[] arrayPoly7 = treeW.getPolygons(parameter.getEnv());
			int a = (int)(arrayPoly7.length- parameter.getMaxElementsSel())/100;
			if(a<0)a=0;
			List<Polygon> listPoly7 = PolygonWorker.useSelection(arrayPoly7, parameter.getEnv(), parameter.getMaxElementsSel()+a);
			listPoly7 = PolygonWorker.unionPolygons(listPoly7, parameter.getEnv(), a);
			List<Polygon> listPoly7_2 = PolygonWorker.useNearestNeighborTypification(listPoly7, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
			listPoly7_2 = PolygonWorker.mergeOverlaps(listPoly7_2);
			listPoly7_2 = PolygonWorker.useAreaSelection(listPoly7_2, parameter.getMinArea());
			jsonString= GeoJsonWriter.getJsonString(listPoly7_2, treeW.getName(), treeW.getType());
			double size7= jsonString.length()/1024;
			System.out.println("json length ~ " + size7 +"kb /// estimated download time with "+parameter.getSpeed() +"kbps = "+size7*8/parameter.getSpeed() );
			break;
	
	}
		
		return jsonString;	
	}
}
