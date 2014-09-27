package main.production.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	public static  String[] str;
	public static int check;
	
	/**
	 * read File the slower way
	 * @return String
	 */
	public static String readFile(String path){
	//System.out.println("path : " + path + " reading...");
	String sCurrentLine;
	String sJSON = "";
	BufferedReader bReader = null;
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

	
	/**
	 * read files the faster way . uses nio.file.files and threads to read list<string>
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readFile2(String file) throws IOException{
		//System.out.println("path : " + path + " reading...");
		Watch t = new Watch();t.start();
		System.out.println("read......");	
		String sJSON = "";
		
		Path path = Paths.get(file);
		List<String> text =Files.readAllLines(path,StandardCharsets.UTF_8);
		
		//number of threads
		int threadNumber = 4;
		
		
		int size= text.size();
		str= new String [threadNumber];
		int d = size/threadNumber;
		for (int i=0; i<threadNumber;i++){
			int min = d*i;
			int max = d*(i+1); if(i==threadNumber-1)max=size;
			Thread a = new Thread(new threadReader(i,text,min,max));
			a.start();
			try {
				a.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				a.interrupt();
			}
		}
		for(String jsonPart : str){
			sJSON += jsonPart;
		}

		t.stop();
		System.out.println("stop ...... "+t.getElapsedTime() +"ms");
		//System.exit(0);
		return sJSON;	
		}
}
