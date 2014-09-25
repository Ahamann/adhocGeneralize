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

public class Cluster {
	
	List<Cluster> list;
	int step;	
	Cluster childA;
	Cluster childB;
	Polygon structure;
	Envelope extent;
	
	
	public static List <Cluster> clusterH () throws JsonParseException, JsonMappingException, IOException{
//		GeometryFactory geometryFactory = new GeometryFactory();
//		Coordinate[] coord = {new Coordinate(1,1),new Coordinate(1,2),new Coordinate(1,4),new Coordinate(1,7),new Coordinate(1,1)};
//		LinearRing shell = geometryFactory.createLinearRing(coord);
//		Polygon tempPoly = geometryFactory.createPolygon(shell, null);
//		Polygon a= tempPoly;
//		
//		if(a==tempPoly)System.out.println("Nu");
	
//	Polygon[] poly = Container.getsavedTrees().get(0).getPolygons();
//	List<Polygon> list = Arrays.asList(poly);
//	STRtree tree = new STRtree();
//	for(int i =0; i<list.size();i++){
//		tree.insert(list.get(i).getEnvelopeInternal(), i);
//	}
	
	//Envelope env = new Envelope(4.756050109863281, 45.920587344733654, 5.404586791992187, 46.157718401427566);//(0,44,10,48);//(5.095574855804443, 45.94613670749096, 5.1361083984375, 45.960980373948985);  //
	
	Envelope env = Container.getsavedTrees().get(0).getEnvelope();
	STRtree tree = Container.getsavedTrees().get(0).getTree(env);//getTree();
	
	List <Cluster> clusterList = new ArrayList<Cluster>();
	int step=0;
	DistancePolygons dist = new DistancePolygons(0); //it takes some time to process with weighting
	System.out.println(tree.size());
	 
	 
	 
	 while(tree.size()>1){
		 System.out.println(tree.size()+" treesize");
		 Object[] nearest = tree.nearestNeighbour(dist);
		 Polygon a = (Polygon) nearest[0];
		 Polygon b = (Polygon) nearest[1];
		 if(a.getArea()<b.getArea()){
			 Polygon c = b; //temp save biggest
			 b =  a; // b = smallest
			 a = c; // a = biggest
		 }
		 Polygon newPolygon = PolygonWorker.clusterPolygons(a,b);
		 tree.remove(((Polygon) nearest[0]).getEnvelopeInternal(), ((Polygon) nearest[0]));
		 tree.remove(((Polygon) nearest[1]).getEnvelopeInternal(), ((Polygon) nearest[1]));
		 @SuppressWarnings("unchecked")
		List<Polygon> polygonListTemp = tree.query(env);
		 tree = new STRtree();
		 	//add array to STRtree
		 	for (int i = 0; i<polygonListTemp.size();i++){
		 		tree.insert(polygonListTemp.get(i).getEnvelopeInternal(),polygonListTemp.get(i)); //save indexes to delete them
		 	}
		 	tree.insert(newPolygon.getEnvelopeInternal(),newPolygon);
		 	tree.build();
		 
		 	Cluster newCluster = new Cluster();
		 	boolean foundA =false;
		 	boolean foundB =false;
		 	
		 	//Envelope need to be the total envelope of both objects to get the objects properly
//		 	Envelope totalEnv = a.getEnvelopeInternal();
//		 	totalEnv.expandToInclude(b.getEnvelopeInternal().getMinX(), a.getEnvelopeInternal().getMinY());
//		 	totalEnv.expandToInclude(b.getEnvelopeInternal().getMinX(), a.getEnvelopeInternal().getMaxY());
//		 	totalEnv.expandToInclude(b.getEnvelopeInternal().getMaxX(), a.getEnvelopeInternal().getMaxY());
//		 	totalEnv.expandToInclude(b.getEnvelopeInternal().getMaxX(), a.getEnvelopeInternal().getMinY());
//		 	
//		 	totalEnv.expandToInclude(newPolygon.getEnvelopeInternal().getMinX(), newPolygon.getEnvelopeInternal().getMinY());
//		 	totalEnv.expandToInclude(newPolygon.getEnvelopeInternal().getMinX(), newPolygon.getEnvelopeInternal().getMaxY());
//		 	totalEnv.expandToInclude(newPolygon.getEnvelopeInternal().getMaxX(), newPolygon.getEnvelopeInternal().getMaxY());
//		 	totalEnv.expandToInclude(newPolygon.getEnvelopeInternal().getMaxX(), newPolygon.getEnvelopeInternal().getMinY());
		 	
		 	//totalEnv.expandToInclude(b.getEnvelopeInternal());
		 	//totalEnv.expandToInclude(newPolygon.getEnvelopeInternal());
		 	
		 	System.out.println(clusterList.size()+" size");
		 	for(int j =0; j<clusterList.size();j++){
		 		//look if polygon A or B is already a cluster - if yes, put it as child to new cluster and remove old cluster - otherwise create new cluster for a and b
		 		if(clusterList.get(j).getStructure()==a){
		 			System.out.println("A ALT");
		 			newCluster.setChildA(clusterList.get(j));
		 			foundA=true;
		 			clusterList.remove(j);j--;
		 		}
		 		
		 		if(j>=0){
		 			if(clusterList.get(j).getStructure()==b){
		 				System.out.println("B ALT");
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
		 		System.out.println("A NEU");
		 		Cluster newA = new Cluster();
		 		newA.setExtent(a.getEnvelopeInternal());
		 		newA.setStructure(a);
		 		newCluster.setChildA(newA);
		 	}
		 	//A isnt an existing cluster - create new
		 	if(!foundB){
		 		System.out.println("B NEU");
		 		Cluster newB = new Cluster();
		 		newB.setExtent(b.getEnvelopeInternal());
		 		newB.setStructure(b);
		 		newCluster.setChildB(newB);
		 	}
		 	Envelope newE = newCluster.getChildA().getExtent();
		 	newE.expandToInclude(newCluster.getChildB().getExtent());
		 	
 			newCluster.setExtent(newE);
 			newCluster.setStructure(newPolygon);
 			newCluster.setStep(step);step++;
		 	clusterList.add(newCluster);	 	
	 }
	 
	
	 System.out.println(clusterList.size());
	 //clusterList.get(0).setExtent(maxExtent);
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
