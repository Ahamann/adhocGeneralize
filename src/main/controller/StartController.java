package main.controller;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import main.production.Factory;
import main.save.Container;


public class StartController implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("create tree");
		//create tree
		String pathOrig = Container.pathOrig;
		String pathFolder = Container.pathFolder;
		String name = Container.name;
		String type = Container.type;
		Factory.createTree(0, pathOrig, pathFolder, name, type);
	}
		
		
	
}
