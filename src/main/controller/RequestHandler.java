package main.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import main.objects.Cluster;
import main.production.Factory;
import main.production.PolygonWorker;
import main.production.TreeWorker;
import main.production.io.GeoJsonWriter;
import main.save.Container;
import main.save.ParameterContainer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;


/**
 * Class handles front end requests, gets polygons based on extent+zoom+parameters level and generalizes them based on chosen operation.
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
	 * @deprecated
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
	 * get GeoJSON String based on parameters
	 * @param modeS preset modus: 0=all without any generalization / 1=selection - max Elements / 2=old bigger gets bigger typification (not recommended) /3=typification /4=pre-selection,typification, overlaps merge / 5=pre-selection, typification, overlaps merge, min area selection / 6=pre-select, union with max dist, typification, overlaps merge, min area selection / 7 = mode 5, shows diameter and supporting segments / 8 = mode 0, shows diameter and supporting segments / 9 = use pre processed cluster hierarchy
	 * @param minxS minX
	 * @param minyS minY
	 * @param maxxS maxX
	 * @param maxyS maxY
	 * @param scaleS Scale 1:x
	 * @param maxTypifyS default=30; set max Elements that should be typified
	 * @param fixElementsS default=based on scale -> radical law; set total max elements shown
	 * @param minAreaS default= 0.001; = 1 edge on square; minimum area for selection in map[m], not real[m] 
	 * @param minDistanceS default=0.0005; minimum distance for union
	 * @param speedS default=320; transfer rate client-server in kbps
	 * @param typmodeS modus of typification; 0=create new trees (recommended), better with weighting - 1=replace object in str-tree(experimental but slightly faster)
	 * @param weightS default=0; if>0 - orientation of polygons is weighted with distance for nearest neighbor search
	 * @param unionS default=-1, -1=deactivated; >=0 is maximum steps to merge nearest polygons with maximum distance
	 * @param simplifyS default=1 1=activated; 0=deactivated; available for mode 5,6 and 9 - recursively simplifies polygon if transfer time to client is above 5 seconds
	 * @return GeoJSON String
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String getJson(String modeS, String minxS, String minyS, String maxxS, String maxyS, String scaleS, String maxTypifyS, String fixElementsS, String minAreaS, String minDistanceS,String speedS,String typmodeS, String weightS,String unionS,String simplifyS) throws JsonParseException, JsonMappingException, IOException{
		//parameters - description in Javadoc and/or above
		int mode = 0;
		double scale = 0; 
		double minx =-180;
		double miny =-180;
		double maxx =180;
		double maxy =180;
		String jsonString = "";	
		ParameterContainer parameter;
		int maxTyp = 0;
		int maxElements = 0;
		double minArea = 0; // example: 500 m² = 0,0000000005
		double minDist = 0; // example:	200 m = 0.002
		double speed = 320;
		int typmode = 0;  //set modus of typify 0= recreate tree after nn with new polygon - 1 = delete 2 nodes and insert new one after nn
		double weight = 0;
		int union = -1;
		int simplify= 1;
		int x =0;
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
			if(!unionS.isEmpty())union = Integer.parseInt(unionS);	x++;
			if(!simplifyS.isEmpty())simplify = Integer.parseInt(simplifyS);	x++;
		}catch(Exception e){
			System.out.println("could not parse parameter input: "+e.getMessage() + " Pos="+x);
		}
		//save parameters
		parameter = new ParameterContainer(mode, scale, minx, miny,maxx,maxy);
		if(maxTyp!=0)parameter.setTypElements(maxTyp);
		if(maxElements!=0)parameter.setMaxElements(maxElements);
		if(speed!=0)parameter.setSpeed(speed);
		if(minArea!=0)parameter.setMinArea(minArea);
		if(minDist!=0)parameter.setMinDist(minDist);
		if(maxElements!=0)parameter.setMaxElements(maxElements);

		//get TreeWorker - for now there is only 1 on start up
		List<TreeWorker> list = Container.getsavedTrees();
		TreeWorker treeW = list.get(0);

		//switch between generalization modes
		List<Polygon> polygons = null;
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
			@SuppressWarnings("deprecation")
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
			List<Polygon> listPoly5 = PolygonWorker.useSelection(arrayPoly5, parameter.getEnv(), parameter.getMaxElementsSel());
			 polygons = PolygonWorker.useNearestNeighborTypification(listPoly5, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
			 polygons = PolygonWorker.mergeOverlaps(polygons);
			 polygons = PolygonWorker.useAreaSelection(polygons, parameter.getMinArea());			
			jsonString= GeoJsonWriter.getJsonString(polygons, treeW.getName(), treeW.getType());
			double size= jsonString.length()/1024;
			System.out.println("json length ~ " + size +"kb /// estimated download time with "+parameter.getSpeed() +"kbps = "+size*8/parameter.getSpeed() );
			break;
		case 6: //select, union, typify, merge overlaps, deselect small
			Polygon[] arrayPoly6= treeW.getPolygons(parameter.getEnv());
			int a=0;
			if (union==-1)a = (int)(arrayPoly6.length- parameter.getMaxElementsSel())/100; //1% of deselection
			else a=union;
			if(a<0)a=0;
			List<Polygon> listPoly6 = PolygonWorker.useSelection(arrayPoly6, parameter.getEnv(), parameter.getMaxElementsSel()+a);
			listPoly6 = PolygonWorker.unionPolygons(listPoly6, parameter.getEnv(), a, parameter.getScale(),parameter.getMinDist());
			 polygons = PolygonWorker.useNearestNeighborTypification(listPoly6, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
			 polygons = PolygonWorker.mergeOverlaps(polygons);
			 polygons = PolygonWorker.useAreaSelection(polygons, parameter.getMinArea());
			jsonString= GeoJsonWriter.getJsonString(polygons, treeW.getName(), treeW.getType());
			break;
		case 7: //get min rectangle diameter and supported segments based on mode 5
			Polygon[] arrayPoly7 = treeW.getPolygons(parameter.getEnv());
			List<Polygon> listPoly7 = PolygonWorker.useSelection(arrayPoly7, parameter.getEnv(), parameter.getMaxElementsSel());
			List<Polygon> listPoly7_2 = PolygonWorker.useNearestNeighborTypification(listPoly7, parameter.getEnv(), parameter.getMaxElementsTyp(), typmode,weight);
			listPoly7_2 = PolygonWorker.mergeOverlaps(listPoly7_2);
			listPoly7_2 = PolygonWorker.useAreaSelection(listPoly7_2, parameter.getMinArea());
			listPoly7_2 = PolygonWorker.giveDiameter(listPoly7_2);
			jsonString= GeoJsonWriter.getJsonString(listPoly7_2, treeW.getName(), treeW.getType());
		break;
		case 8: //get diameter and supported segments for all polygons
			Polygon[] arrayPoly8 = treeW.getPolygons(parameter.getEnv());
			List<Polygon> listPoly8 = PolygonWorker.giveDiameter(Arrays.asList(arrayPoly8));
			jsonString= GeoJsonWriter.getJsonString(listPoly8, treeW.getName(), treeW.getType());
			break;
		case 9: //use cluster hierarchy which is created on start
			List<Cluster> cluster = new ArrayList<Cluster>();
			cluster.add(Container.getCluster().get(0));
			System.out.println(Container.getCluster().size()+"sizeClusterRoot");
			Envelope tempE= Container.getCluster().get(0).getExtent();
			System.out.println("clusterE: minx="+tempE.getMinX()+ " miny="+tempE.getMinY()+ " maxx="+tempE.getMaxX()+ " maxy="+tempE.getMaxY());
			Envelope aktE= parameter.getEnv();
			System.out.println("searchE: minx="+aktE.getMinX()+ " miny="+aktE.getMinY()+ " maxx="+aktE.getMaxX()+ " maxy="+aktE.getMaxY());
		
			//get cluster based on extent and maximum elements shown
			polygons = new ArrayList<Polygon>();
			int maxPolygons = parameter.getMaxElementsTyp();
			System.out.println(maxPolygons+" maxPolys");
			for(int i=0;i<cluster.size();i++){
				if(cluster.size()>=maxPolygons)break;
				Cluster visit = cluster.get(i);
				if (parameter.getEnv().intersects(visit.getExtent()) || visit.getExtent().intersects(parameter.getEnv())){				
					if(visit.getChildA()!=null && visit.getChildB()!=null){
						cluster.add(visit.getChildA());
						cluster.add(visit.getChildB());
						cluster.remove(i);i--;
						cluster = PolygonWorker.bubbleSortCluster(cluster);
						if(cluster.size()>=maxPolygons)break;
					}
				}else{
					cluster.remove(i);i--; //delete cluster, if it isnt in given extent
				}
			}
			//add polygons from cluster list
			System.out.println(cluster.size()+" size");
			for (int i = 0;i<cluster.size();i++){
				polygons.add(cluster.get(i).getStructure());
			}
			//merge overlapsed polygons and get json string
			polygons = PolygonWorker.mergeOverlaps(polygons); //only really necessary with weighting
			jsonString= GeoJsonWriter.getJsonString(polygons, treeW.getName(), treeW.getType());
		break;
	}
		
		//simplify for 5,6,9
		//simplify if transfer rate is to high
		if((mode==5 || mode==6 || mode==9) && simplify==1 ){
			jsonString = PolygonWorker.simplifyBasedOnString(polygons, jsonString, parameter.getScale(), parameter.getSpeed(), treeW.getName(), treeW.getType());
		}
		double size6= jsonString.length()/1024;
		System.out.println("json length ~ " + size6 +"kb /// estimated download time with "+parameter.getSpeed() +"kbps = "+size6*8/parameter.getSpeed() );
		return jsonString;	
	}
}
