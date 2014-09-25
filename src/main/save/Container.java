package main.save;

import java.util.ArrayList;
import java.util.List;

import main.objects.Cluster;
import main.production.TreeWorker;



/**
 * Class to save constants, objects and especially TreeWorkers
 * @author Bernd Grafe
 *
 */
public class Container {

	//Constants
	//Path
	public static String pathOrig = "C:\\GenData\\lakes_lyon.geojson";
	//lakes_lyon.geojson - not bad
	//france_water.geojson - too big
	//germany_water.geojson - too big
	//saxony_forest.geojson - too big
	//building_tud.geojson
	//smallSax_forest.geojson
	//sax_water.geojson
	//wald.geojson    //stupid osm polygons are cut off
	//wasser.geojson  //boring ds
	
	
	public static String pathFolder = "C:\\GenData\\rEntries";
	public static String name = "water";
	public static String type = "FeatureCollection";
	public static List<Cluster> cluster ;
	
	//TreeWorkers
	static List<TreeWorker> savedTreeWorker;

	public static List<TreeWorker> getsavedTrees() {
		if(savedTreeWorker==null)savedTreeWorker=new ArrayList<TreeWorker>();
		return savedTreeWorker;
	}

	public static void addTreeWorker(TreeWorker tree) {
		if(savedTreeWorker==null)savedTreeWorker=new ArrayList<TreeWorker>();
		savedTreeWorker.add(tree);
	}
	
	
	public static void setCluster (List<Cluster>c){
		cluster = c;
	}
	public static List<Cluster> getCluster (){
		return cluster;
	}
}
