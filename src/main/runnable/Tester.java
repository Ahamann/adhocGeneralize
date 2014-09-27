package main.runnable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

import main.production.Factory;
import main.production.PolygonWorker;
import main.production.TreeWorker;
import main.production.io.GeoJsonWriter;
import main.save.Container;

/**
 * Test class to prepare back end data structure
 * @author Bernd Grafe
 *
 */
public class Tester {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		
//		String pathOrig = Container.pathOrig;
//		String pathFolder = Container.pathFolder;
//		String name = Container.name;
//		String type = Container.type;
//		int mode = 0; // normal
//		//create Tree
//		Factory.createTree(mode, pathOrig, pathFolder, name, type);
//		//test Tree
//		List<TreeWorker> list = Container.getsavedTrees();
//		
//		System.out.printf("saved "+list.size()+ " treeWorker");
//		System.out.printf("Json String for first Worker:");
//		Polygon[] polys= list.get(0).getPolygons();
//		String jsonString;		
//		jsonString= GeoJsonWriter.getJsonString(polys, list.get(0).getName(), list.get(0).getType());
//		System.out.println(jsonString);
		
		//x,x,y,y
		
		STRtree tree = new STRtree();
		List<Coordinate> coords = new ArrayList<Coordinate>();
		GeometryFactory geometryFactory = new GeometryFactory();
		
		//create points
		for(int i = 0; i<10;i++){
			//double x = Math.random()*100;
			//double y = Math.random()*100;
			Coordinate coord = new Coordinate(i*i,i*i);	
			coords.add(coord);
			

	
		}
		//shuffle points
		Collections.shuffle(coords);
		
		
		//add to tree
		for( int f=0; f<coords.size();f++){
			Point p = geometryFactory.createPoint(coords.get(f));
			tree.insert(p.getEnvelopeInternal(),p);
			System.out.println(p.getX()+ "   "+ p.getY());
		}
		//show points in tree
		for(int i = 0; i<tree.itemsTree().size();i++){
			System.out.println(tree.itemsTree().get(i));
		}
		//nN
		GeometryItemDistance dist = new GeometryItemDistance() ;
		Object[] nearest = tree.nearestNeighbour(dist);
		Point a = (Point) nearest[0];
		Point b = (Point) nearest[1];
		//create new Point
		Point x = geometryFactory.createPoint(new Coordinate(a.getX()+1,a.getY()+1)); 
		//tree size comparison + replace
		System.out.println(tree.size());
		tree.replace(a.getEnvelopeInternal(), a, x.getEnvelopeInternal(), x);
		System.out.println(tree.size()); 
		//show points again
		for(int i = 0; i<tree.itemsTree().size();i++){
			System.out.println(tree.itemsTree().get(i));
		}
		
		System.out.println("+++++");
		
		
		Object[] nearest2 = tree.nearestNeighbour(dist);
		Point c = (Point) nearest2[0];
		Point d = (Point) nearest2[1];
		//create new Point
		Point y = geometryFactory.createPoint(new Coordinate(d.getX()+1,d.getY()+1)); 
		tree.replace(d.getEnvelopeInternal(), d, y.getEnvelopeInternal(), y);
		//show points again
		for(int i = 0; i<tree.itemsTree().size();i++){
			System.out.println(tree.itemsTree().get(i));
		}
	}
}
