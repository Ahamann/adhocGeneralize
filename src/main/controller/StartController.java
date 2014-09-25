package main.controller;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

import main.helper.Watch;
import main.objects.Cluster;
import main.objects.DistancePolygons;
import main.production.Factory;
import main.production.PolygonWorker;
import main.save.Container;


public class StartController implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		Watch time = new Watch();
		time.start();
		System.out.println("create tree... (this could take a while - depending on input filesize)");
		//create tree
		String pathOrig = Container.pathOrig;
		String pathFolder = Container.pathFolder;
		String name = Container.name;
		String type = Container.type;
		Factory.createTree(0, pathOrig, pathFolder, name, type);
		time.stop();
		System.out.println("server startet: "+ time.getElapsedTime() +"ms");
		
		
		
		
		System.out.println("CH-Test");
		try {
			 Container.setCluster(Cluster.clusterH());;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("fail");
		}
		System.out.println("done");
	}
		
		
	
	
	
	
	
	
}
