package main.production.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import main.helper.Watch;
import main.helper.threadReader;

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
	//System.out.println("path : " + path + " reading...");
	String sCurrentLine;
	String sJSON = "";
	BufferedReader bReader = null;
	//System.out.println("Working Directory = " + System.getProperty("user.dir"));
	//C:\\jsonTempFolder\\test.json	
	//C:\\Users\\Ahamann\\Desktop\\MASTER_Topo\\workspace\\git_repo\\Generalize\\master_alpha\\WebContent\\data\\lakesGeo.json
	try{
	FileReader fReader = new FileReader(path);
	
	bReader = new BufferedReader(fReader,16000);

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
	//System.out.println("reading done");
	//System.exit(0);

	return sJSON;	
	}

	
	
	public static String readFile2(String file) throws IOException{
		//System.out.println("path : " + path + " reading...");
		Watch t = new Watch();t.start();
		System.out.println("read......");	
		String sCurrentLine;
		String sJSON = "";
		
		Path path = Paths.get(file);
		List<String> text =Files.readAllLines(path,StandardCharsets.UTF_8);
		
		int size= text.size();
		int i = size/8;

		threadReader r1 = new threadReader(text,0,i);
		threadReader r2 = new threadReader(text,i,i*2);
		threadReader r3 = new threadReader(text,i*2,i*3);
		threadReader r4 = new threadReader(text,i*3,i*4);
		threadReader r5 = new threadReader(text,i*4,i*5);
		threadReader r6 = new threadReader(text,i*5,i*6);
		threadReader r7 = new threadReader(text,i*6,i*7);
		threadReader r8 = new threadReader(text,i*7,text.size());
		
		r1.start();r2.start();r3.start();r4.start();
		r5.start();r6.start();r7.start();r8.start();

		sJSON=r1.getString()+r2.getString()+r3.getString()+r4.getString()+
				r5.getString()+r6.getString()+r7.getString()+r8.getString();

		
		//System.out.println("reading done");
		t.stop();
		System.out.println("stop ...... "+t.getElapsedTime());	
		//System.exit(0);
		return sJSON;	
		}
}
