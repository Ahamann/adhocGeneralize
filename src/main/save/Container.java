package main.save;

import java.util.ArrayList;
import java.util.List;

import main.production.TreeWorker;



/**
 * Class to save constants, objects and especially TreeWorkers
 * @author Bernd Grafe
 *
 */
public class Container {

	//Constants
	//Path
	public static String pathOrig = "C:\\GenData\\building_tud.geojson";
	//lakes_lyon.geojson
	//france_water.geojson - too big
	//germany_water.geojson - too big
	//saxony_forest.geojson - too big
	//building_tud.geojson
	//smallSax_forest.geojson
	//sax_water.geojson
	//wald.geojson
	//wasser.geojson
	
	
	public static String pathFolder = "C:\\GenData\\rEntries";
	public static String name = "water";
	public static String type = "FeatureCollection";
	
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
}
