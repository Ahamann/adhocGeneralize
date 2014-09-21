package main.production.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * static methods to read files (JSON)
 * @author Bernd Grafe
 *
 */
public class GeoJsonReader {
	
	
	/**
	 * read File
	 * @return String
	 */
	public static String readFile(String path){
	//System.out.println("path : " + path);
		
	String sCurrentLine;
	String sJSON = "";
	BufferedReader bReader = null;
	//System.out.println("Working Directory = " + System.getProperty("user.dir"));
	//C:\\jsonTempFolder\\test.json	
	//C:\\Users\\Ahamann\\Desktop\\MASTER_Topo\\workspace\\git_repo\\Generalize\\master_alpha\\WebContent\\data\\lakesGeo.json
	try{
	FileReader fReader = new FileReader(path);
	
	bReader = new BufferedReader(fReader);

	while ((sCurrentLine = bReader.readLine()) != null) {
		sJSON += sCurrentLine;
	} 
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		try {
			if (bReader != null)bReader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	return sJSON;	
	}

}
