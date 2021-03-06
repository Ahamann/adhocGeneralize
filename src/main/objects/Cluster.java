package main.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.production.PolygonWorker;
import main.save.Container;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * A CLuster - Cluster Hierarchy binary construction to save the typification to use on the fly.
 * it has 2 children cluster
 *
 * @author Bernd Grafe
 *
 */
public class Cluster {
	List<Cluster> list;
	int step;	
	Cluster childA;
	Cluster childB;
	Polygon structure;
	Envelope extent;
	
	/**
	 * compute clustering based on str-tree nearest neighbor
	 * @return List of 1 Cluster - with every other cluster as children
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static List <Cluster> clusterH () throws JsonParseException, JsonMappingException, IOException{
		Envelope env = Container.getsavedTrees().get(0).getEnvelope();
		STRtree tree = Container.getsavedTrees().get(0).getTree(env);//getTree();

		List <Cluster> clusterList = new ArrayList<Cluster>();
		int step=0;
		int size = tree.size();
		DistancePolygons dist = new DistancePolygons(0); //it takes some time to process with weighting
		System.out.println(tree.size());
		int oldsize =0;
	 
		while(tree.size()>1){
			//just progress info
			if(tree.size()==oldsize)System.out.println("stuck at "+tree.size()); 
			else oldsize = tree.size();
			if(tree.size()==size/100*25)System.out.println("75%");
			if(tree.size()==size/100*50)System.out.println("50%");
			if(tree.size()==size/100*75)System.out.println("25%");
		 
			//get nearest neighbor
			//System.out.println(tree.size()+" treesize");
			Object[] nearest = tree.nearestNeighbour(dist);
			Polygon a = (Polygon) nearest[0];
			Polygon b = (Polygon) nearest[1];
			if(a.getArea()<b.getArea()){
				Polygon c = b; //temp save biggest
				b =  a; // b = smallest
				a = c; // a = biggest
			}
			//cluster / typify and remove from tree
			Polygon newPolygon = PolygonWorker.clusterPolygons(a,b);
			tree.remove(((Polygon) nearest[0]).getEnvelopeInternal(), ((Polygon) nearest[0]));
			tree.remove(((Polygon) nearest[1]).getEnvelopeInternal(), ((Polygon) nearest[1]));
			//create new tree with new cluster
			@SuppressWarnings("unchecked")
			List<Polygon> polygonListTemp = tree.query(env);
			tree = new STRtree();
		 	//add array to STRtree
		 	for (int i = 0; i<polygonListTemp.size();i++){
		 		tree.insert(polygonListTemp.get(i).getEnvelopeInternal(),polygonListTemp.get(i)); //save indexes to delete them
		 	}
		 	tree.insert(newPolygon.getEnvelopeInternal(),newPolygon);
		 	tree.build();
		 	
		 	//add 2 nearest polygons and cluster to cluster hierarchy
		 	Cluster newCluster = new Cluster();
		 	boolean foundA =false;
		 	boolean foundB =false;
		 	
		 	System.out.println("tree ="+tree.size()+" cluster="+clusterList.size());
		 	for(int j =0; j<clusterList.size();j++){
		 		//look if polygon A or B is already a cluster - if yes, put it as child to new cluster and remove old cluster - otherwise create new cluster for a and b
		 		if(clusterList.get(j).getStructure()==a){
		 			//System.out.println("A ALT");
		 			newCluster.setChildA(clusterList.get(j));
		 			foundA=true;
		 			clusterList.remove(j);j--;
		 		}
		 		if(j>=0){
		 			if(clusterList.get(j).getStructure()==b){
		 				//System.out.println("B ALT");
			 			newCluster.setChildB(clusterList.get(j));
			 			foundB=true;
			 			clusterList.remove(j);j--;
			 		}
		 		}
		 		if(foundA==true && foundB==true){
		 			break; //if true both were cluster, and are now in new cluster
		 		}
		 	}
		 	
		 	//A isnt an existing cluster - create new
		 	if(!foundA){
		 		//System.out.println("A NEU");
		 		Cluster newA = new Cluster();
		 		newA.setExtent(a.getEnvelopeInternal());
		 		newA.setStructure(a);
		 		newCluster.setChildA(newA);
		 	}
		 	//A isnt an existing cluster - create new
		 	if(!foundB){
		 		//System.out.println("B NEU");
		 		Cluster newB = new Cluster();
		 		newB.setExtent(b.getEnvelopeInternal());
		 		newB.setStructure(b);
		 		newCluster.setChildB(newB);
		 	}
		 	//expand envelope, to select elements on map properly
		 	Envelope newE = newCluster.getChildA().getExtent();
		 	newE.expandToInclude(newCluster.getChildB().getExtent());
		 	//add polygon cluster, save step for recreation
 			newCluster.setExtent(newE);
 			newCluster.setStructure(newPolygon);
 			newCluster.setStep(step);step++;
		 	clusterList.add(newCluster);	 	
	 }
	 return clusterList;
}
	
	public List<Cluster> getList() {
		return list;
	}
	public void setList(List<Cluster> list) {
		this.list = list;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public Cluster getChildA() {
		return childA;
	}
	public void setChildA(Cluster childA) {
		this.childA = childA;
	}
	public Cluster getChildB() {
		return childB;
	}
	public void setChildB(Cluster childB) {
		this.childB = childB;
	}
	public Polygon getStructure() {
		return structure;
	}
	public void setStructure(Polygon structure) {
		this.structure = structure;
	}
	public Envelope getExtent() {
		return extent;
	}
	public void setExtent(Envelope extent) {
		this.extent = extent;
	}
	

}
