package com.csm.rover.simulator.rover.autoCode;

import com.csm.rover.simulator.objects.DecimalPoint;
import com.csm.rover.simulator.wrapper.Globals;

import java.util.Map;

public class MER extends RoverAutonomousCode {
	
	private static final long serialVersionUID = 1195207024217443714L;

	private static final double ANGLE_ERROR = Math.PI/16.0;
	
	private int completed = 0;
	private DecimalPoint[] targets;
	private int score = 0;

	public MER(DecimalPoint[] targets){
		super("MER", "MER");
		this.targets = targets;
	}
	
	private MER(MER origin) {
		super(origin);
		this.completed = origin.completed;
		this.targets = origin.targets;
		this.score = origin.score;
	}

	@Override
	public String nextCommand(long milliTime, DecimalPoint location,
			double direction, Map<String, Double> parameters) {
		super.writeToLog(milliTime + "\t" + location.getX() + "\t" + location.getY() + "\t" + MAP.getHeightAt(location) + "\t" + score + "\t" + parameters.get("battery_charge") + "\t" + (completed == targets.length));
		direction = (direction + 2*Math.PI) % (2*Math.PI);
		if (completed == targets.length){
			return "stop";
		}
		if (Math.abs(targets[completed].getX() - location.getX()) < 0.25 && Math.abs(targets[completed].getY() - location.getY()) < 0.25){
			score += MAP.getTargetValueAt(location);
			completed++;
		}
		double targetAngle = Math.atan((targets[completed].getY() - location.getY()) / (double)(targets[completed].getX() - location.getX()));
		if (targets[completed].getX() < location.getX()){
			targetAngle += Math.PI;
		}
		targetAngle = (targetAngle + 2*Math.PI) % (2*Math.PI);
		//System.out.println(targetAngle + "\t" + (targets[completed].getY() - location.getY()) + "\t" + (targets[completed].getX() - location.getX()));
		if (Math.abs(Globals.getInstance().subtractAngles(targetAngle, direction)) < ANGLE_ERROR){
			return "move";
		}
		else {
			if (Globals.getInstance().subtractAngles(targetAngle, direction) > 0){
				return "spin_cw";
			}
			else {
				return "spin_ccw";
			}
		}
	}

	@Override
	public RoverAutonomousCode clone() {
		return new MER(this);
	}

}
