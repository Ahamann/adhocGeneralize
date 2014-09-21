package main.production;

import java.io.IOException;




/**
 * Factory Class - depending on mode, create trees and save them to container
 * This class is supposed to prepare the data before a request can appear
 * @author Bernd Grafe
 *
 */
public class Factory {


	/**
	 * create a single TreeWorker depending on mode - 0=normal
	 * @param mode
	 * @param path
	 * @param folder
	 * @param name
	 * @param type
	 */
	public static void createTree(int mode, String path, String folder, String name, String type){
		String[] paths = {path};
		String[] folders = {folder};
		String[] names = {name};
		createTree(mode, paths, folders, names, type);
	}
	
	/**
	 * create several TreeWorker
	 * @param mode
	 * @param paths
	 * @param folders
	 * @param names
	 * @param type
	 */
	public static void createTree(int mode, String[] paths, String[] folders, String[] names, String type){
		if(paths.length!=folders.length || paths.length!=names.length  )return;
		switch(mode){
		//normal mode - create TreeWorker with jsonTree and save it in container
		case 0:
			addTrees(paths, folders, names, type);
			break;

		}
	}

	/**
	 * create and add TreeWorker to container
	 * @param paths
	 * @param folders
	 * @param name
	 * @param type
	 */
	private static void addTrees(String[] paths, String[] folders, String[] name, String type){

		for(int i=0;i<paths.length;i++){
			try {
				@SuppressWarnings("unused")
				TreeWorker tree = new TreeWorker( paths[i],  folders[i],  name[i],  type, true, true);//save=true ,it saves itself
			} catch (IOException e) {
				System.out.println("Couldnt create TreeWorker for i="+i +" with:");
				e.printStackTrace();
			}
		}
		
		
		
		
	}
	
	
		
}
