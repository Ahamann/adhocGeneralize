package main.production;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import main.helper.Watch;
import main.objects.Cluster;
import main.objects.DistancePolygons;
import main.production.io.GeoJsonWriter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationFactory;
/**
 * Collection of static Methods to manipulate, read etc. Polygons
 * @author Bernd Grafe
 *
 */
public class PolygonWorker {

	/**
	 * reads json strings and returns polygons
	 * json node -> feature node -> coord values -> Coordinate -> Coordinate[] -> LinearRing -> Polygon -> Polygon[]
	 * 
	 * @param jsonString
	 * @return array of polygons
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static Polygon[] json2polygons(String jsonString) throws JsonParseException, JsonMappingException, IOException{
		//String name;
		Polygon[] jsonPolygons;
		ObjectMapper m = new ObjectMapper();
		JsonNode fullNode = null; //node of full json document
		JsonNode featureNode = null; //node of features []
		GeometryFactory geometryFactory = new GeometryFactory(); //factory to create geometries
				
		//read json string as tree structure
		fullNode = m.readTree(jsonString);
		featureNode = fullNode.path("features");
		//name = fullNode.path("name").asText();
		//outer loop for features/polygons
		jsonPolygons = new Polygon[featureNode.size()];
		for (int i= 0; i< featureNode.size() ; i++){ //TODO: length of loop
			JsonNode coordNode;
			coordNode = featureNode.get(i).findValue("coordinates").get(0);
			if(coordNode.size()==1)coordNode=coordNode.get(0); //go deeper if multipolygon
			//inner loop for coordinates of feature/polygon
			Coordinate[] tempCoords = new Coordinate[coordNode.size()];
			for (int j = 0; j< coordNode.size();j++){
					//if ( coordNode.get(j)!=null){
					double x = coordNode.get(j).get(0).asDouble();
					double y = coordNode.get(j).get(1).asDouble();
					Coordinate tempCoord = new Coordinate(x,y);
					tempCoords[j] = tempCoord;
					//}		
			}
			try{
				LinearRing shell = geometryFactory.createLinearRing(tempCoords);
				Polygon tempPoly = geometryFactory.createPolygon(shell, null);
				jsonPolygons[i] = tempPoly;
			}catch(Exception e){
				System.out.println("strange polygon was found - skip");
			}
			
		}
		return jsonPolygons;
	}
	
	
	/**
	 * select polygon based on extent area and max elements
	 * @param polygons
	 * @param env Extent
	 * @param maxSelect max Elements to Select
	 * @return list of selected polygons
	 */
	public static List<Polygon> useSelection(Polygon[] polygons, Envelope env, int maxSelect){
		List<Polygon>  polygonList= new ArrayList<Polygon>();
		System.out.println("polyLength: "+polygons.length + " /max Polygons : "+ maxSelect);
			if( maxSelect < polygons.length){
				System.out.println("show only "+maxSelect+" biggest  / sort...");
				Polygon[] sortedSmall = bubbleSort(polygons, false);
				System.out.println("sort done, add...");
				for(int i = sortedSmall.length - maxSelect; i<sortedSmall.length;i++ ){
					polygonList.add(sortedSmall[i]);
				}
			 }else {
				 for(int j = 0; j< polygons.length;j++){
					 polygonList.add(polygons[j]);
				 }
			 }
		return polygonList;
	}
	
	
	/**
	 * bigger gets bigger typification - sort polygons, biggest first - search surroundings for  biggest polygons
	 * increase (buffer) and delete smaller ones 
	 * @param polygons
	 * @param env Extent
	 * @return list of strange typified polygons
	 * @deprecated
	 */
 public static List<Polygon> useTypification(Polygon[] polygons, Envelope env){
	 	List<Polygon>  polygonList= new ArrayList<Polygon>();
	 	STRtree tree = new STRtree();
	 	//sort array
	 	polygons = bubbleSort(polygons, true);
	 	//add array to STRtree
	 	for (int i = 0; i<polygons.length;i++){
	 		tree.insert(polygons[i].getEnvelopeInternal(),i); //save indexes to delete them
	 	}
	 	//nearest neighbour
	 	//GeometryItemDistance a = new GeometryItemDistance() ;
	 	//Polygon poly =(Polygon) tree.nearestNeighbour(polygons[0].getEnvelopeInternal(), polygons[0], a);
 		//polygonList.add(poly);
	 	GeometryFactory geometryFactory = new GeometryFactory();
	 	//envelope expansion
	 	for(int j= 0; j<polygons.length;j++){
	 		if(polygons[j]!=null){
	 			//expand
	 			Envelope envTemp = polygons[j].getEnvelopeInternal();
	 			double expander = (env.getWidth()+env.getHeight());
	 			double cornerOld = envTemp.getMaxX();
	 			envTemp.expandBy(envTemp.getWidth()*expander, envTemp.getHeight()*expander); //get Envelope and expand by 2% to get surrounded polygons
	 			//search for polygons to delete 
	 			@SuppressWarnings("unchecked")
	 			List<Integer> listTemp = tree.query(envTemp);
	 			//set items to null - get ids
	 			for(int k=0;k<listTemp.size();k++){
	 				if(listTemp.get(k)!=j){
	 					polygons[listTemp.get(k)]=null;
	 				}
	 			}
	 			//buffer polygon
	 			if (listTemp.size()>0){
	 				double cornerNew = envTemp.getMaxX();
	 				double distance = (cornerNew-cornerOld)/10;
	 				Geometry geo = polygons[j].buffer(distance);
	 				Coordinate[] coords = geo.getCoordinates();
	 				//CREATE NEW POLYGON
	 				try{
	 				LinearRing shell = geometryFactory.createLinearRing(coords);
	 				Polygon tempPoly = geometryFactory.createPolygon(shell, null);
	 				polygonList.add(tempPoly);
	 				//CATCH - reconstruct coordinates if coords arent closed
	 				}catch(Exception e){
	 					int length = geo.getCoordinates().length;

	 					Coordinate[] closed = new Coordinate[length+1];
	 					for(int m=0;m<length;m++){
	 						closed[m]=geo.getCoordinates()[m];
	 					}
	 					closed[closed.length-1]=geo.getCoordinates()[0];
	 					LinearRing shell = geometryFactory.createLinearRing(closed);
		 				Polygon tempPoly = geometryFactory.createPolygon(shell, null);
		 				polygonList.add(tempPoly);
	 				}
	 			}
	 		}
	 	}	
	 	System.out.println("output poly " +polygonList.size());
	 	return polygonList;	 
 }
	

 /**
  * Typification based on nearest neighbor (str-tree) with or without orientation weighting / centre of gravity / area weight / clustering / max amount of objects
  * @param tree STRtree with polygons
  * @param env	Extent
  * @param maxTyp max elements to typify
  * @param typmode 0=create new tree after(recommended, especially with weighting) nN; 1=replace polygons in old str-tree 
  * @param weight weight distance:orientation with 1:x for nearest neighbor
  * @return list of typified polygons
  */
 @SuppressWarnings("unchecked")
public static List<Polygon> useNearestNeighborTypification(STRtree tree, Envelope env, Integer maxTyp, Integer typmode, double weight){
	 Watch watchTotal = new Watch();
	 Watch watchnN = new Watch();
	 watchTotal.start();
	 List<Polygon>  polygonList= new ArrayList<Polygon>();
	 //nearest neighbor - alternative distance matrix
	 //GeometryItemDistance dist = new GeometryItemDistance() ;
	 DistancePolygons dist = new DistancePolygons(weight);
	 //Loop for max amount of polygons
	 int remC = 0;
	 int max = tree.size()-maxTyp;
	 System.out.println("Tree size: "+ tree.size());
	 System.out.println("reduce to : "+ maxTyp);
		int treeSize=  tree.size();
	 
	 if (typmode==0){ // nN -> delete 2 old polygons, create new one - insert all poylgons in new tree
	 while(tree.size()>maxTyp){
		 watchnN.start();
		 Object[] nearest = tree.nearestNeighbour(dist);
		 watchnN.stop();
		 Polygon a = (Polygon) nearest[0];
		 Polygon b = (Polygon) nearest[1];
		 //double dis = (a.distance(b))*100000/ TempParameterContainer.scaleStatic;
		//	System.out.println((dis*1000)+"mm");
		 if(a.getArea()<b.getArea()){
			 Polygon c = b; //temp save biggest
			 b =  a; // b = smallest
			 a = c; // a = biggest
		 }
		 Polygon newPolygon = clusterPolygons(a,b);
		 tree.remove(((Polygon) nearest[0]).getEnvelopeInternal(), ((Polygon) nearest[0]));
		 tree.remove(((Polygon) nearest[1]).getEnvelopeInternal(), ((Polygon) nearest[1]));
		 List<Polygon> polygonListTemp = tree.query(env);
		 tree = new STRtree();
		 	//add array to STRtree
		 	for (int i = 0; i<polygonListTemp.size();i++){
		 		tree.insert(polygonListTemp.get(i).getEnvelopeInternal(),polygonListTemp.get(i)); //save indexes to delete them
		 	}
		 	tree.insert(newPolygon.getEnvelopeInternal(),newPolygon);
		 	tree.build();
	 remC++;
	 System.out.println(remC + " / " + max +" / actual tree size="+tree.size());
	 if(treeSize == tree.size()){
		 System.out.println("nN error");
		 break;
	 }else treeSize = tree.size();
	 }
	 }
	 
	 if (typmode ==1){	 //delete 2 nodes , add 1
	 while(tree.size()>maxTyp){ //delete 2 old polygons, insert new one - in same tree
		 watchnN.start();
		 Object[] nearest = tree.nearestNeighbour(dist);
		 watchnN.stop();
		 Polygon a = (Polygon) nearest[0];
		 Polygon b = (Polygon) nearest[1];
		 //double dis = (a.distance(b))*100000/ TempParameterContainer.scaleStatic;
		//	System.out.println((dis*1000)+"mm");
		//sort polygons based on area
		 if(a.getArea()<b.getArea()){
			 Polygon c = b; //temp save biggest
			 b =  a; // b = smallest
			 a = c; // a = biggest
		 }
		 //cluster a+ b
		 Polygon newPolygon = clusterPolygons(a,b);
		 //remove b (smallest)
		if(! tree.remove(((Polygon) nearest[1]).getEnvelopeInternal(), ((Polygon) nearest[1]))) System.out.println("remove error");
		 //replace a (biggest) with newPolygon
		 tree.replace(((Polygon) nearest[0]).getEnvelopeInternal(), ((Polygon) nearest[0]), newPolygon.getEnvelopeInternal(), newPolygon); 
		 remC++;
		 System.out.println(remC + " / " + max +" / actual tree size="+tree.size());
		 if(treeSize == tree.size()){
			 System.out.println("nN error");
			 break;
		 }else treeSize = tree.size();
	 }
	 }

	 System.out.println(remC + " removed");
	 polygonList = tree.query(env);
	 //save deleted or replaced polygons to see the difference
	 watchTotal.stop();
	 System.out.println("total calc time for typification (in ms): "+ watchTotal.getElapsedTime());
	 System.out.println("time only for nearest neighbour calc (in ms): " + watchnN.getTotalElapsedTime());
	 return polygonList;
 }
 
 
 /**
  * merge polygons based on max steps and min distance in map[m] + scale
  * @param polygons
  * @param env
  * @param steps
  * @param scale
  * @param distTol
  * @return merged polygons
  */
 @SuppressWarnings("unchecked")
 public static List<Polygon> unionPolygons (List<Polygon> polygons, Envelope env, Integer steps,double scale, double distTol){
	List<Polygon> polygonList =new ArrayList<Polygon>();
	 Watch watchU = new Watch();
	 watchU.start();
	 STRtree tree = new STRtree();
	 	//add array to STRtree
	 	for (int i = 0; i<polygons.size();i++){
	 		tree.insert(polygons.get(i).getEnvelopeInternal(),polygons.get(i)); //save indexes to delete them	
	 	}
	 //merging
	 	System.out.println("max steps = " + steps);
	 	int treeSize = tree.size();
	 	int max = treeSize - steps;
	 	int actualStep=0;
	 	 DistancePolygons dist = new DistancePolygons(0);
		 while(tree.size()>max){
			 Object[] nearest = tree.nearestNeighbour(dist);
			 Polygon a = (Polygon) nearest[0];
			 Polygon b = (Polygon) nearest[1];
			 double distance = a.distance(b);
			 double distanceTolerance = distTol * scale / 100000; //0.0005
			 if (distanceTolerance>distance){
				 System.out.println("max union tolerance reached.");
				 break;
			 }
			 a = (Polygon) a.buffer(distance);
			 b = (Polygon) b.buffer(distance);
			 Polygon c = (Polygon) a.union(b);
			 c= (Polygon) c.buffer(-distance);
			 //double dis = (a.distance(b))*100000/ TempParameterContainer.scaleStatic;
			//System.out.println((dis*1000)+"mm");
		
			 //old convex hull
//			 GeometryFactory geometryFactory = new GeometryFactory();
//			 Geometry newGeom =  a.union(b);
//			 newGeom = newGeom.convexHull();
//			 Coordinate[] coords= newGeom.getCoordinates();
//			 LinearRing shell = geometryFactory.createLinearRing(coords);
//			 Polygon newPolygon = geometryFactory.createPolygon(shell, null);
			 
			 //remove in tree, create new one with merged polygon
			 tree.remove(((Polygon) nearest[0]).getEnvelopeInternal(), ((Polygon) nearest[0]));
			 tree.remove(((Polygon) nearest[1]).getEnvelopeInternal(), ((Polygon) nearest[1]));
			 List<Polygon> polygonListTemp = tree.query(env);
			 tree = new STRtree();
			 	//add array to STRtree
			 	for (int i = 0; i<polygonListTemp.size();i++){
			 		tree.insert(polygonListTemp.get(i).getEnvelopeInternal(),polygonListTemp.get(i)); //save indexes to delete them
			 	}
			 	tree.insert(c.getEnvelopeInternal(),c);
			 	tree.build();
		 //System.out.println(remC + " / " + max +" / actual tree size="+tree.size());
			 	actualStep++;
		 if(treeSize == tree.size()){
			 System.out.println("nN error");
			 break;
		 }else treeSize = tree.size();
		 
		 }
		 polygonList = tree.query(env);
		 watchU.stop();
		 System.out.println("union for "+actualStep +" polygons done in:"+watchU.getElapsedTime());
		return polygonList;
 }
 

 
 /**
  * polygon clustering based on centroid and area - affine transformation = typification of 2 polygons
  * @param a bigger Polygon (area)
  * @param b smaller Polygon (area)
  * @return clustered/typified polygon
  */
 public static Polygon clusterPolygons(Polygon a, Polygon b){	 
		 //get ratio
		 double  translationTolerance = 0.6; // 1 = normal - the bigger the number the bigger the shift of the polygon
		 double ratio = a.getArea() / b.getArea();
		 ratio= ratio/translationTolerance;
		 //get centroid - centre of gravity
		 Point centroidA = a.getCentroid();
		 Point centroidB = b.getCentroid();
		 double distX = centroidA.getX()-centroidB.getX(); //dist = distance to new point based on ratio, not only based on mean of 2 centroids
		 double distY = centroidA.getY()-centroidB.getY();
		 distX = -distX / 2 / (ratio);///translationTolerance);  
		 distY = -distY / 2 / (ratio);///translationTolerance);
		 //create new Polygon based on biggest polygon, transform polygon with affine transformation
		 //the difference between centroids (and ratio - so bigger polygons wont move as much as smaller ones) is used to get new position
		 //set parameters for transformation
		 Coordinate srcMin = new Coordinate(a.getEnvelopeInternal().getMinX(),a.getEnvelopeInternal().getMinY());
		 Coordinate srcMax = new Coordinate(a.getEnvelopeInternal().getMaxX(),a.getEnvelopeInternal().getMaxY());
		 Coordinate srcCentroid = centroidA.getCoordinate();
		 Coordinate destMin = new Coordinate(srcMin.x+distX,srcMin.y+distY);
		 Coordinate destMax = new Coordinate(srcMax.x+distX,srcMax.y+distY);
		 Coordinate destCentroid = new Coordinate(srcCentroid.x+distX,srcCentroid.y+distY);
		 //set affine transformation with parameters - based on ratio(coming from area differne) and distance(coming from centroid and ratio)
		 //Min for scale
		 Coordinate finalMin = new Coordinate( destCentroid.x - (destCentroid.x-destMin.x) - (destCentroid.x-destMin.x)/ratio,  destCentroid.y - (destCentroid.y-destMin.y) - (destCentroid.y-destMin.y)/ratio);
		 //Max for scale
		 Coordinate finalMax = new Coordinate( destCentroid.x + (destMax.x-destCentroid.x) + (destMax.x-destCentroid.x)/ratio,  destCentroid.y + (destMax.y-destCentroid.y) + (destMax.y-destCentroid.y)/ratio);
		 //set at
		 AffineTransformation at = AffineTransformationFactory.createFromControlVectors(srcMin,srcMax,srcCentroid,finalMin,finalMax,destCentroid);
		 //tranform and create new Polygon
		 Polygon newPolygon = null;
		 try{
			  newPolygon = 	 (Polygon) at.transform(a);
		 }catch(Exception e){
			return a;
		 }	
	 return newPolygon;
 }
 

 /**
  * calls equivalent method but with Polygon[] input
  * @param polygons Array of Polygons
  * @param env
  * @param maxTyp
  * @param typmode
  * @param weight
  * @return
  */
 public static List<Polygon> useNearestNeighborTypification(Polygon[] polygons, Envelope env, Integer maxTyp, Integer typmode,double weight){
	 List<Polygon>  polygonList= new ArrayList<Polygon>();
	 STRtree tree = new STRtree();
	 	//add array to STRtree
	 	for (int i = 0; i<polygons.length;i++){
	 		tree.insert(polygons[i].getEnvelopeInternal(),polygons[i]); //save indexes to delete them
	 		
	 	}
	 	tree.build();
	 	polygonList = useNearestNeighborTypification(tree, env, maxTyp, typmode,weight);
	 return polygonList;
 }
 

 /**
  * calls equivalent method but with List<Polygon> input
  * @param polygons
  * @param env
  * @param maxTyp
  * @param typmode
  * @param weight
  * @return
  */
 public static List<Polygon> useNearestNeighborTypification(List<Polygon> polygons, Envelope env, Integer maxTyp, Integer typmode,double weight){
	 List<Polygon>  polygonList= new ArrayList<Polygon>();
	 Polygon[] array = polygons.toArray(new Polygon[polygons.size()]);
	 polygonList = useNearestNeighborTypification(array, env, maxTyp, typmode,weight);
	 return polygonList;
 }
 
 
 /**
  * sort an array of polygons, decreasing or increasing
  * @param polygons
  * @param biggestFirst true=sort decreasing, false=increasing
  * @return sorted polygons
  */
 private static Polygon[] bubbleSort(Polygon[] polygons, boolean biggestFirst){
	 Polygon temp;
		for(int i=1; i<polygons.length; i++) {
			for(int j=0; j<polygons.length-i; j++) {
				if (biggestFirst){
				if(polygons[j].getArea()<polygons[j+1].getArea()) {  //change if biggest or smallest should be "bubbled"
					temp=polygons[j];
					polygons[j]=polygons[j+1];
					polygons[j+1]=temp;
				}
				}else{
					if(polygons[j].getArea()>polygons[j+1].getArea()) {  //change if biggest or smallest should be "bubbled"
						temp=polygons[j];
						polygons[j]=polygons[j+1];
						polygons[j+1]=temp;
					}	
				}
				
			}
		}
		return polygons;
 }
 
 /**
  * merge polygons when overlaps occurs
  * @param polygons
  * @return merged polygons
  */
 public static List<Polygon> mergeOverlaps (List<Polygon> polygons){
	 int length = polygons.size();
	 int intersectC = 0;
	 System.out.println("check for overlaps - pol size = "+ length);
	 for (int i = 0; i<length-1;i++){
		 for(int j = 0; j<length;j++){
			 if(i!=j && polygons.get(i).intersects(polygons.get(j))){
				 intersectC ++;
				 try{
				 polygons.set(i,(Polygon) polygons.get(i).union(polygons.get(j)));
				 }catch(Exception e){
					System.out.println("skiped merge MP"); 
				 }
				 polygons.remove(j);
				 length --;
				 j--;
			 }
		 }
	 }
	 System.out.println("intersection ->Union : " +  intersectC);
	 return polygons;
	 
 }
 
 
 /**
  * returns polygon list with polygons bigger than min area
  * @param polygons
  * @param minArea
  * @return min area selected polygons
  */
 public static List<Polygon> useAreaSelection(List<Polygon> polygons, double minArea){
		List<Polygon>  polygonList= new ArrayList<Polygon>();
		System.out.println("tree size: " + polygons.size());
		for(int i=0; i<polygons.size();i++){
			if(polygons.get(i).getArea()>minArea) polygonList.add(polygons.get(i));
		}
		System.out.println("new size because of area =" +polygonList.size());
		return polygonList;
	}
 
 
 /**
  * returns diameter and supported segment as polygons
  * @param polygons
  * @return diameter and supp. segments
  */
 public static List<Polygon> giveDiameter (List<Polygon> polygons){
	List<Polygon>  polygonList= new ArrayList<Polygon>();
 	GeometryFactory geometryFactory = new GeometryFactory();
	
 	for (int i = 0; i< polygons.size();i++){
	
 		for(int a=0;a<2;a++){
 		MinimumDiameter minD = 	new MinimumDiameter(polygons.get(i));
 		Coordinate[]  coords = null;
 				
 		if(a==0)coords= minD.getSupportingSegment() .getCoordinates();  //getDiameter() //getMinimumRectangle() 
 		if(a==1)coords= minD.getDiameter() .getCoordinates(); 
 		
 		int length = coords.length;

			Coordinate[] closed = new Coordinate[length+2];
			for(int m=0;m<length;m++){
				closed[m]=coords[m];
			}
			closed[closed.length-2]=coords[0];
			closed[closed.length-1]=coords[0];
 		
 		
	//Geometry geo = minD.getMinimumRectangle();	
		//Coordinate[] coords = geo.getCoordinates();
		//CREATE NEW POLYGON
	
		LinearRing shell = geometryFactory.createLinearRing(closed);
		Polygon tempPoly = geometryFactory.createPolygon(shell, null);
		//polygonList.add(polygons.get(i));
		polygonList.add(tempPoly);
		
 		}
//		Coordinate a = coords[0];
//		Coordinate b = coords[1];
//		double sideA = b.x- a.x;
//		double sideB = b.y- a.y;
//		//double sideC = Math.sqrt(Math.pow(sideA,2) + Math.pow(sideB,2));
//		//double alpha = Math.acos(  (Math.pow(sideB,2)+Math.pow(sideC,2)-Math.pow(sideA,2)) / 2 * sideB * sideC ) ;
//		//double alphaDegree = alpha * 180 / Math.PI;
//		double m = sideB / sideA;
//		double alpha = Math.atan(m);
//		double alphaDegree = alpha * 180 / Math.PI;
//		System.out.println("ANGLE = " + alpha + "   /   "+alphaDegree ); // + clockwise - counter clockwise
	}	
	return polygonList;
 }
 
 
 
 
 /**
  * sorts decreasingly cluster based on step number
  * @param cluster list of cluster
  * @return sorted list of cluster
  */
 public static List<Cluster> bubbleSortCluster(List<Cluster> cluster){
	 Cluster temp;
		for(int i=1; i<cluster.size(); i++) {
			for(int j=0; j<cluster.size()-i; j++) {
				
				if(cluster.get(j).getStep()<cluster.get(j+1).getStep()) {  //change if biggest or smallest should be "bubbled"
					temp=cluster.get(j);
					cluster.set(j, cluster.get(j+1));
					cluster.set(j+1, temp);
				}
				
				
			}
		}
		return cluster;
 }
 
 /**
  * Simplifies Polygons if transfer time is above 5 seconds recursevly
  * @param poly
  * @param jsonString to check length for transfer
  * @param scale scale to calculate time
  * @param speed in kbps to calculate time
  * @param name of geoJSON to create new String
  * @param type of geoJSON to create new String
  * @return GeoJSON STring
  * @throws IOException
  */
 public static String simplifyBasedOnString (List<Polygon> poly, String jsonString, double scale, double speed, String name, String type) throws IOException{
	 double startTol = 0.0005; //0.0005 is normal // 0.01 is a lot
	 int steps = 1;
	 List<Polygon> polygons = poly;
	 String newJSON = "";
	 double transfer= jsonString.length()/1024*8/speed;
		System.out.println("time="+transfer +" with "+polygons.size() +" polygons");
		if(transfer<=5)return jsonString;
			while(transfer>5){
				newJSON = "";
				double distanceTolerance = (startTol*steps) * scale / 100000; //5mm  //0.0005
				for(int b=0;b<polygons.size();b++){
					polygons.set(b, (Polygon) TopologyPreservingSimplifier.simplify(polygons.get(b), distanceTolerance));				
				}
				newJSON = GeoJsonWriter.getJsonString(polygons, name, type);
				transfer= newJSON.length()/1024*8/speed;
				System.out.println(transfer);
				steps++;
			}
			System.out.println(steps-1+" times simplified");
	 return newJSON;
	 
	 
 }
 
}



