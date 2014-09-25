package main.save;

import com.vividsolutions.jts.geom.Envelope;

/**
 * class to provide, manage and manipulate parameters
 * @author Bernd Grafe
 *
 */
public class TempParameterContainer {

	//input necessary-mandatory 
	int mode;		//presets no.
	double scale;     	//Zoomlevel
	double minX;	//min X		
	double minY;	//min Y
	double maxX;	//max X
	double maxY;	//max Y
	//automatically but changeable
	double speed; 	//connection speed kbps
	double time; //s
	//double ratio;	//ratio Selection / Typification
	int maxTyp;		//better than ratio? max amount of typifications
	double minDist; //min distance
	double minArea; //min Area
	int maxElementsTotal; 	//max Elements 
	int maxElementsSel;		//max E. for Selection
	int maxElementsTyp;		//max E. for Typification
	Envelope env; 			//extent
	//int scale;				//scale for zoomlevel
	int fixScale;	//radical law, given scale
	int fixCount;	//radical law, given amount of elements
	
	public static double scaleStatic;
	
	/**
	 * create parameter container with mandatory inputs
	 * @param m
	 * @param s
	 * @param mix
	 * @param miy
	 * @param max
	 * @param may
	 */
	public TempParameterContainer(int m, double s, double mix, double miy, double max, double may){
		mode= m;
		scale=s;
		minX=mix;
		minY=miy;
		maxX=max;
		maxY=may;
		scaleStatic=scale;
		env=new Envelope(minX,maxX,minY,maxY);
		
		speed = 0;
		maxTyp = 30;   // about 180ms per nN search
		time = 5;
		
		//min Distance not implemented
		minDist = 0; //not used
		
		//min Area calc based on % - NOW based on map length max 1mm
		
		//double height = env.getHeight();
		//double width = env.getWidth();
		//double area = height*width;
		//minArea = area/100*0.005 ; //threshold to deselect / 0.005% of Extent
		//set min area  0,16mm² or 0,5mm² - with 0,4mm or 0,7mm edge length sounds quiet small
		double minMapLength = 0.001; // in m//= 1mm
		double realLength = minMapLength * scale / 100000; //in m , translated to coord unit which is 1 = 100km  //realcoords - 1,0 = 100km -> 1m = 0,00001  //100m -> 0,001
		minArea = realLength * realLength;
		
		
		//double dist = 0.003 * scale / 100000;
		//System.out.println(dist+"=max dist in m");
		
		//based on bertins generalisation - input for radical law (töpfer)
		fixScale = 1000000;
		fixCount = 135;
		
		calcRLaw ();
		System.out.println("scale: "+scale);
		
		
	} 

	/**
	 * calculate max amount of elements based on given scale and objects
	 */
	private void calcRLaw (){
		maxElementsTotal = (int) (fixCount * Math.pow((fixScale)/(scale), 0.5)); //0,5 is normal square root
		maxElementsTyp = maxElementsTotal;
		maxElementsSel = maxElementsTyp + maxTyp; //select / typify until max number - but maximum maxTyp-times typify	
	}
	
	/**
	 * set max number of elements based on speed/transfer rate - based on average data file size and file size for polygons
	 * @param s
	 */
	public void setSpeed(double s){
		speed = s; //in kb/s
		//* with: x=2sec/8*380kbps -> x=95kbyte [1byte=8bit][sek=kbyte*8/kbps] / 1sec->45kbyte /4sec->190kbyte
		
		double maxDataSize = time / 8 * speed;  //1byte = 8 bit   //in kbyte
		
		//calculate max polygons based on normal data file - estimated
		// header+body without polygon ~ 300 byte 
		// polygons -> 1 Point = 24 Byte (with 7 decimal places) + 1 Byte comma    = 25 Bytes
		//there are 1174 files (tree entries) with total 843kBytes -> 735.91 bytes per file with 300bytes header = 434.91bytes for polygons -> divided by 25 bytes = 17,4 ~ 18 Point per Polygon
		// 18 Points per Polygon * 25 Bytes = 450 Bytes per Polygon
		// head+body = 300bytes   / 1 polygon = 450 bytes
		maxDataSize = maxDataSize*1024; //kbyte in byte
		
		//new calc - head = 55bytes // polygon = 62 bytes (name etc) + points + 176 bytes (properties)
		System.out.println("ds max="+maxDataSize +"byte");
		//int maxCountSpeed = (int) ((maxDataSize - 55)/(450+62+176)); //this is only valid for normal polygons, not generalized
		
		//generalized polygons are bigger the bigger the scale
		//200000 -> 48
		//100000 -> 44
		//50000 -> 40
		//25000 -> 30
		int header = 55;
		int polyHeader = 65;
	    int polyProp = 180;
	    int pointsinPoly = 18; //normal // with 25 bytes per point
	    
	    if( scale > 25000){
	    	pointsinPoly = 30;
	    } 
	    if( scale > 50000){
	    	pointsinPoly = 40;
	    } 
	    if( scale > 100000){
	    	pointsinPoly = 44;
	    }  
	    if( scale > 200000){
	    	pointsinPoly = 50;
	    }
	    
		int maxCountSpeed = (int) ((maxDataSize - header)/(polyHeader + (pointsinPoly*25)+ polyProp)); 
		System.out.println("scale: "+scale+" -> set points per polygon to :"+pointsinPoly);
		
		if(maxCountSpeed<maxElementsTotal){
			maxElementsTyp=maxCountSpeed;
			maxElementsSel=maxElementsTyp+maxTyp;
		}		
		
		System.out.println("Töpfer max= "+maxElementsTotal +" / max with "+speed+"kbps = " +maxCountSpeed);
	}
	
	
	/**
	 * set amount of max objects for a given scale to calculate max for other scales
	 * @param fixCount
	 * @param fixScale
	 */
	public void setFixCountScale(int fixCount,int fixScale) {
		this.fixCount = fixCount;
		this.fixScale = fixScale;
		calcRLaw ();
	}

	/**
	 * set maximum amount of objects shown 
	 * @param max
	 */
	public void setMaxElements(int max){
		maxElementsTotal = max;
		maxElementsTyp = maxElementsTotal;
		maxElementsSel = maxElementsTyp + maxTyp;
	}
	
	
	/**
	 * set maximum for typification steps
	 * @param maxTyp
	 */
	public void setTypElements(int maxTyp) {
		this.maxTyp = maxTyp;
		calcRLaw ();
	}
	
	// generic Setters and Getters
	
	public void setScale(double scale) {
		this.scale = scale;
	}

	
	public void setMaxElementsTyp(int maxElementsTyp) {
		this.maxElementsTyp = maxElementsTyp;
	}
	
	
	
	public double getTime(){
		return time;
	}
	
	public void setTime(double time){
		this.time=time;
	}
	
	public void setMinArea(double minArea) {
		double minMapLength =minArea;// 0.001; // in m//= 1mm
		double realLength = minMapLength * scale / 100000; //in m , translated to coord unit which is 1 = 100km  //realcoords - 1,0 = 100km -> 1m = 0,00001  //100m -> 0,001
		this.minArea = realLength * realLength;
	}
	
	
	public int getMode() {
		return mode;
	}


	public void setMode(int mode) {
		this.mode = mode;
	}


	//public int getZoom() {
	//	return zoom;
	//}


	//public void setZoom(int zoom) {
	//	this.zoom = zoom;
	//}


	public double getMinX() {
		return minX;
	}


	public void setMinX(double minX) {
		this.minX = minX;
	}


	public double getMinY() {
		return minY;
	}


	public void setMinY(double minY) {
		this.minY = minY;
	}


	public double getMaxX() {
		return maxX;
	}


	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}


	public double getMaxY() {
		return maxY;
	}


	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}


	public double getSpeed() {
		return speed;
	}


	//public void setSpeed(double speed) {
	//	this.speed = speed;
	//}


	public int getTypElements() {
		return maxTyp;
	}




	public double getMinDist() {
		return minDist;
	}


	public void setMinDist(double minDist) {
		this.minDist = minDist;
	}


	public double getMinArea() {
		return minArea;
	}




	public int getMaxElementsTotal() {
		return maxElementsTotal;
	}


	public void setMaxElementsTotal(int maxElementsTotal) {
		this.maxElementsTotal = maxElementsTotal;
	}


	public int getMaxElementsSel() {
		return maxElementsSel;
	}


	public void setMaxElementsSel(int maxElementsSel) {
		this.maxElementsSel = maxElementsSel;
	}


	public int getMaxElementsTyp() {
		return maxElementsTyp;
	}





	public Envelope getEnv() {
		return env;
	}


	public void setEnv(Envelope env) {
		this.env = env;
	}


	public double getScale() {
		return scale;
	}


	public int getFixScale() {
		return fixScale;
	}



	public int getFixCount() {
		return fixCount;
	}



	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
