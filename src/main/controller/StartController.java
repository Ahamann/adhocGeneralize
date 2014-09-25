package main.controller;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import main.helper.Watch;
import main.production.Factory;
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
	}
		
		
	
}
