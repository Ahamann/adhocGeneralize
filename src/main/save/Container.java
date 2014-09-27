package main.save;
import java.util.ArrayList;
import java.util.List;
import main.objects.Cluster;
import main.production.TreeWorker;

/**
 * Class to save constants, objects and especially TreeWorkers + ClusterHierarchie
 * check path for different files
 * @author Bernd Grafe
 *
 */
public class Container {

	//Parameters
	//Path
	public static String pathOrig = "C:\\GenData\\lakes_lyon.geojson";        //sweden_lakes.geojson";   //lakes_lyon.geojson
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
	
	//Cluster
	public static void setCluster (List<Cluster>c){
		cluster = c;
	}
	public static List<Cluster> getCluster (){
		return cluster;
	}
}
