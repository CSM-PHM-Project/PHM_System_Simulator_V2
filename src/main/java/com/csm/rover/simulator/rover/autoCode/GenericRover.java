package com.csm.rover.simulator.rover.autoCode;

import com.csm.rover.simulator.objects.DecimalPoint;

import java.util.Map;

public class GenericRover extends RoverAutonomusCode {

	private static final long serialVersionUID = -5883548370057346938L;
	
	private long lastActionTime = 0;
	private int action = 0;
	private int seconds = 1;
	private int power = 250;
	
	public GenericRover(String roverName, int sec){
		super("Generic", roverName);
		seconds = sec;
	}
	
	public GenericRover(GenericRover in) {
		super(in);
		this.lastActionTime = in.lastActionTime;
		this.action = in.action;
		this.seconds = in.seconds;
	}


	public String nextCommand(
			long milliTime,
			DecimalPoint location,
			double direction,
			Map<String, Double> parameters
	){
		if (milliTime-lastActionTime > 1000*seconds){
			lastActionTime = milliTime;
			action++;
			if (action%11 == 0){
				power -= 50;
				return "chngmtr0" + power;
			}
			if (action%5 < 2){
				return "move";
			}
			else if (action%5 < 5){
				return "turnFR";
			}
			else {
				return "";
			}
		}
		else {
			return "";
		}
	}

	public GenericRover clone(){
		return new GenericRover(this);
	}
	
}