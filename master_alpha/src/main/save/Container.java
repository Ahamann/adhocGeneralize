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
	public static String pathOrig = "C:\\Users\\Ahamann\\Desktop\\MASTER_Topo\\workspace\\git_repo\\Generalize\\master_alpha\\WebContent\\data\\lakesGeo.json";
	public static String pathFolder = "C:\\jsonTempFolder\\test";
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
