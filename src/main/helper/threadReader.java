package main.helper;

import java.util.List;

import main.production.io.GeoJsonReader;

public class threadReader implements Runnable{
	List<String> list; int start; int stop;int no;
	String text;
	
	public threadReader(int threadNumber,List<String> list, int start, int stop){
		this.no=threadNumber;
		this.list = list;
		this.start = start;
		this.stop = stop;
		text="";
	}
	
	@Override
	public void run() {
		System.out.println("thread started from "+start+" to "+stop );
		for (int i = start;i<stop;i++){
			text+=list.get(i);
		}
		GeoJsonReader.str[no]=text;
		System.out.println("thread "+no+" done");
		
	}


}
