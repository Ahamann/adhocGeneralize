package main.controller;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import main.helper.Watch;
import main.objects.Cluster;
import main.production.Factory;
import main.save.Container;

/**
 * ServletContextListener - fired on start up, converts input geoJSON to str-tree - and creates cluster hierarchiy
 * @author Bernd Grafe
 *
 */
public class StartController implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}


	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		Watch time = new Watch();
		time.start();
		System.out.println("Server startet - create tree... (this could take a while - depending on input filesize)");
		//create tree
		String pathOrig = Container.pathOrig;
		String pathFolder = Container.pathFolder;
		String name = Container.name;
		String type = Container.type;
		Factory.createTree(0, pathOrig, pathFolder, name, type);
		System.out.println("tree created in : "+ time.getElapsedTime() +"ms");
		//create cluster hierarchy
		System.out.println("create cluster... for mode 9");
		try {
			 Container.setCluster(Cluster.clusterH());;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("cluster fail");
		}
		System.out.println("server fully loaded in : "+ time.getElapsedTime() +"ms");
		time.stop();
	}
		
		
	
	
	
	
	
	
}
