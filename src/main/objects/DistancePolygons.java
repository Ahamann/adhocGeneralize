package main.objects;

import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;

/**
 * Based on ItemDistance Interface to define the calculation of distance for nearest neighbor + weighting with orientation
 * @author Bernd Grafe
 *
 */
public class DistancePolygons implements ItemDistance{
	double weight;
	
	/**
	 * 
	 * @param weight weight for orientation, 1:x-distance:orientation
	 */
	public DistancePolygons(double weight){
		this.weight = weight;
	}

	@Override
	public double distance(ItemBoundable a, ItemBoundable b) {
		
	    Geometry g1 = (Geometry) a.getItem();
	    Geometry g2 = (Geometry) b.getItem();

		//and a lot of faster this way with envelope dist instead of geometry dist
		double distance =  g1.getEnvelopeInternal().centre().distance(g2.getEnvelopeInternal().centre());//g1.distance(g2); 
		double result = distance;
		
		//get orientation
		if(weight>0){
			MinimumDiameter minD1 = 	new MinimumDiameter(g1);
			MinimumDiameter minD2 = 	new MinimumDiameter(g2);
			//angle 1
			Coordinate a1 = minD1.getSupportingSegment().getCoordinates()[0];   //getSupportingSegment getDiameter
			Coordinate b1 = minD1.getSupportingSegment().getCoordinates()[1];   //getSupportingSegment getDiameter
			double sideA1 = b1.x- a1.x;
			double sideB1 = b1.y- a1.y;
			double m1 = sideB1 / sideA1;
			double alpha1 = Math.atan(m1);
			//angle 2
			Coordinate a2 = minD2.getSupportingSegment().getCoordinates()[0];
			Coordinate b2 = minD2.getSupportingSegment().getCoordinates()[1];
			double sideA2 = b2.x- a2.x;
			double sideB2 = b2.y- a2.y;
			double m2 = sideB2 / sideA2;
			double alpha2 = Math.atan(m2);
				
			double delta = alpha1- alpha2;
			if (delta <0)delta=delta*-1;
			//weighting
			result = (distance * 1 + delta * weight) / (1+weight);
		}
		//System.out.println("weight="+weight+" dist="+distance +" result ="+result);
		return result;//distance;
	}
}
