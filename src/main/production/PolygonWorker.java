package main.production;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import main.helper.Watch;
import main.objects.DistancePolygons;
import main.save.TempParameterContainer;

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
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;
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
	 * @return
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
			LinearRing shell = geometryFactory.createLinearRing(tempCoords);
			Polygon tempPoly = geometryFactory.createPolygon(shell, null);
			jsonPolygons[i] = tempPoly;
		}
		return jsonPolygons;
	}
	
	
	/**
	 * select polygon based on extent area
	 * @param polygons
	 * @param env
	 * @param maxSelect
	 * @return
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
	 * @param env
	 * @return
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
	
 
 
 /*
  * termination condition for max polygons/filesize based on transfer rate and average file size per polygon
  * 
  * gprs(54kbps),edge(260kbps).umts(380kbps),hsdpa(>3.2mbps)
  * 
  * max loading time should be 5 sec (reference?)
  * 
  * with: x=2sec/8*380kbps -> x=95kbyte [1byte=8bit][sek=kbyte*8/kbps] / 1sec->45kbyte /4sec->190kbyte
  * 
  * head per file ~ 296 byte - 1Polygon ~ 22 + comma 1
  * 
  * average amount of points per polygon per file => total 1174 files = 863.955 bytes(843kbytes) => 735.91 bytes per file => (-296 header / 23) = 19 Points = 1 Polygon
  * 
  * 2 sec / 95kbyte(97280byte) with 735.91byte per file = 132 Polygons
  * 4 sec / 190kbyte(194560byte) with 735.91byte per file = 264 Polygons
  * 
  * -----------------------
  * 
  * termination condition for max polygons based on töpfers radical law
  * 
  * based on given scale with given amount of polygons
  * 
  * polygons = givenPolygons*SQRT(givenScale/scale) - scale given by actual envelope/bbox
  * 
  * 
  * there is no reference for different scales:
  * 
  * based on dataset with scale 1:10k -> ~ 61 polygons (50-80 polygons depending on bbox)
  * with ~ 365 polygons at 1:25k
  * with 1:25k -> 1:10k - result would be 230
  * with 1:10k (70) -> 1:25 - result would be 110 !!!
  * 
  * 
  * -> aesthetic vs performance
  */
 
 ////new Typification - nearest distances from RTREE -> Clustering - delet old ones - but save centre of gravity and are
 //// new location based on all centres with area weighting + aff. transformation for biggest polygon + growth based on other areas
 ////based on nearest neighbor
 ////based on smallest area
 /**
  * Typification based on nearest neighbor / centre of gravity / area weight / clustering / max amount of objects
  * @param tree
  * @param env
  * @param maxTyp
  * @return
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
		 double dis = (a.distance(b))*100000/ TempParameterContainer.scaleStatic;
			System.out.println((dis*1000)+"mm");
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
	 if (typmode ==1){
	 
	
		 
	 //delete 2 nodes , add 1
	 while(tree.size()>maxTyp){ //delete 2 old polygons, insert new one - in same tree
		 watchnN.start();
		 Object[] nearest = tree.nearestNeighbour(dist);
		 watchnN.stop();
		 Polygon a = (Polygon) nearest[0];
		 Polygon b = (Polygon) nearest[1];
		 double dis = (a.distance(b))*100000/ TempParameterContainer.scaleStatic;
			System.out.println((dis*1000)+"mm");
		//sort polygons based on area
		 if(a.getArea()<b.getArea()){
			 Polygon c = b; //temp save biggest
			 b =  a; // b = smallest
			 a = c; // a = biggest
		 }
		 //cluster a+ b
		 Polygon newPolygon = clusterPolygons(a,b);
		 //remove b (smallest)
		 tree.remove(((Polygon) nearest[1]).getEnvelopeInternal(), ((Polygon) nearest[1]));
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
 
 
 @SuppressWarnings("unchecked")
 public static List<Polygon> unionPolygons (List<Polygon> polygons, Envelope env, Integer steps){
	List<Polygon> polygonList =new ArrayList<Polygon>();
	 Watch watchU = new Watch();
	 watchU.start();
	 STRtree tree = new STRtree();
	 	//add array to STRtree
	 	for (int i = 0; i<polygons.size();i++){
	 		tree.insert(polygons.get(i).getEnvelopeInternal(),polygons.get(i)); //save indexes to delete them
	 		
	 	}
	 	
	 //merging
	 	int treeSize = tree.size();
	 	int max = treeSize - steps;
	 	 DistancePolygons dist = new DistancePolygons(0);
		 while(tree.size()>max){
			 Object[] nearest = tree.nearestNeighbour(dist);
			 Polygon a = (Polygon) nearest[0];
			 Polygon b = (Polygon) nearest[1];
			 double dis = (a.distance(b))*100000/ TempParameterContainer.scaleStatic;
			System.out.println((dis*1000)+"mm");
			 
			 
			 if(a.getArea()<b.getArea()){
				 Polygon c = b; //temp save biggest
				 b =  a; // b = smallest
				 a = c; // a = biggest
			 }

			 GeometryFactory geometryFactory = new GeometryFactory();
			 Geometry newGeom =  a.union(b);
			 newGeom = newGeom.convexHull();
			 Coordinate[] coords= newGeom.getCoordinates();
			 LinearRing shell = geometryFactory.createLinearRing(coords);
			 Polygon newPolygon = geometryFactory.createPolygon(shell, null);
			 
			 
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
		 //System.out.println(remC + " / " + max +" / actual tree size="+tree.size());
		 if(treeSize == tree.size()){
			 System.out.println("nN error");
			 break;
		 }else treeSize = tree.size();
		 
		 }
		 polygonList = tree.query(env);
		 watchU.stop();
		 System.out.println("union for "+steps +" polygons done in:"+watchU.getElapsedTime());
		return polygonList;
	 
	 
	 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 /**
  * polygon clustering based on centroid and area
  * @param a
  * @param b
  * @return
  */
 private static Polygon clusterPolygons(Polygon a, Polygon b){	 
	
		 //get ratio
		 double  translationTolerance = 1; // 1 = normal - the bigger the number the bigger the shift of the polygon
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
		 Polygon newPolygon = 	 (Polygon) at.transform(a);
	 return newPolygon;
 }
 
 /**
  * Typification based on nearest neighbor / centre of gravity / area weight / clustering
  * @param polygons
  * @return
  */
 public static List<Polygon> useNearestNeighborTypification(Polygon[] polygons, Envelope env, Integer maxTyp, Integer typmode,double weight){
	 List<Polygon>  polygonList= new ArrayList<Polygon>();
	 STRtree tree = new STRtree();
	 	//add array to STRtree
	 	for (int i = 0; i<polygons.length;i++){
	 		tree.insert(polygons[i].getEnvelopeInternal(),polygons[i]); //save indexes to delete them
	 		
	 	}
	 	polygonList = useNearestNeighborTypification(tree, env, maxTyp, typmode,weight);
	 return polygonList;
 }
 
 /**
  * Typification based on nearest neighbor / centre of gravity / area weight / clustering
  * @param polygons
  * @param env
  * @param zoom
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
  * @param biggestFirst
  * @return
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
  * merge polygon when overlaps occurs
  * @param polygons
  * @return
  */
 public static List<Polygon> mergeOverlaps (List<Polygon> polygons){
	 int length = polygons.size();
	 int intersectC = 0;
	 System.out.println("check for overlaps - pol size = "+ length);
	 for (int i = 0; i<length-1;i++){
		 for(int j = 0; j<length;j++){
			 if(i!=j && polygons.get(i).intersects(polygons.get(j))){
				 intersectC ++;
				 polygons.set(i,(Polygon) polygons.get(i).union(polygons.get(j)));
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
  * @return
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
//		
//		double sideA = b.x- a.x;
//		double sideB = b.y- a.y;
//		
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
}



