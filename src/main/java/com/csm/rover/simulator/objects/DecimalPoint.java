package com.csm.rover.simulator.objects;

import java.awt.Point;
import java.io.Serializable;

public class DecimalPoint implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	private double x, y, z;
	
	public DecimalPoint(){
		x = 0;
		y = 0;
		z = 0;
	}
	
	public DecimalPoint(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public DecimalPoint(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public DecimalPoint(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public DecimalPoint(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	public double getZ(){
		return z;
	}
	
	public int roundX(){
		return (int)Math.round(x);
	}
	
	public int roundY(){
		return (int)Math.round(y);
	}
	
	public void offsetThis(double x, double y){
		this.x += x;
		this.y += y;
	}
	
	public DecimalPoint offset(double x, double y){
		return new DecimalPoint(this.x+x, this.y+y);
	}
	
	public DecimalPoint offset(DecimalPoint pnt){
		return new DecimalPoint(this.x+pnt.x, this.y+pnt.y);
	}
	
	public DecimalPoint scale(double factor){
		return new DecimalPoint(x*factor, y*factor);
	}
	
	public Point toPoint(){
		return new Point(roundX(), roundY());
	}
	
	@Override
	public String toString(){
		return (Math.round(x*1000)/1000.0) + ", " + (Math.round(y*1000)/1000.0);
	}
	
	@Override
	public DecimalPoint clone(){
		return new DecimalPoint(x, y);
	}
	
}
