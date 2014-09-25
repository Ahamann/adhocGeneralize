package main.helper;

import java.util.List;

public class threadReader implements Runnable{
	List<String> list; int start; int stop;
	String text;
	boolean done;
	
	public threadReader(List<String> list, int start, int stop){
		this.list = list;
		this.start = start;
		this.stop = stop;
		text="";
		done=false;
	}
	
	@Override
	public void run() {
		System.out.println("thread started from "+start+" to "+stop );
		// TODO Auto-generated method stub
		
	}
	
	public void start(){
		System.out.println("thread started from "+start+" to "+stop );
		for (int i = start;i<stop;i++){
			text+=list.get(i);
		}
		done=true;
	}
	public String getString(){
		return text;
	}
	public boolean getStatus(){
		return done;
	}

}
