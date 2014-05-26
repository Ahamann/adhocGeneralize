package main.production;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

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
	 * @return
	 */
	public static List<Polygon> useSelection(Polygon[] polygons, Envelope env){
		List<Polygon>  polygonList= new ArrayList<Polygon>();
		double height = env.getHeight();
		double width = env.getWidth();
		double area = height*width;
		double threshold = area/100*0.005 ; //threshold to deselect

		for(int i=0; i<polygons.length;i++){
			if(polygons[i].getArea()>threshold) polygonList.add(polygons[i]);
		}

		return polygonList;
	}
	
 public static List<Polygon> useTypification(Polygon[] polygons, Envelope env){
	 	List<Polygon>  polygonList= new ArrayList<Polygon>();
	 	STRtree tree = new STRtree();
	 	//sort array
	 	polygons = bubbleSort(polygons);
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
	 			//set items to null
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
	
 
 private static Polygon[] bubbleSort(Polygon[] polygons){
	 Polygon temp;
		for(int i=1; i<polygons.length; i++) {
			for(int j=0; j<polygons.length-i; j++) {
				if(polygons[j].getArea()<polygons[j+1].getArea()) {  //change if biggest or smallest should be "bubbled"
					temp=polygons[j];
					polygons[j]=polygons[j+1];
					polygons[j+1]=temp;
				}
				
			}
		}
		return polygons;
 }
 
 
 
 
 
 
 
 
 
}
