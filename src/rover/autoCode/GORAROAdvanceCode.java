package rover.autoCode;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import objects.DecimalPoint;
import objects.List;
import rover.RoverAutonomusCode;
import wrapper.Access;
import wrapper.Admin;
import wrapper.Globals;

public class GORAROAdvanceCode extends RoverAutonomusCode {

	private static final double ANGLE_ERROR = Math.PI/16.0;
	private static final double RECALC_TIME = 2000; //ms
	
	private int score = 0;
	private int state = 1;
	
	private int histories = 3;
	private int sampleDirections = 16;
	private double sampleRadius = 2;
	
	double[][] potentials;
	
	private double averagingRadius = 25;
	private double averagingAngle = Math.PI / 6.0; //rad
	private double averagingPStep = Math.PI / 45.0;
	private double averagingRStep = 0.1;
	
	private double targetDirection = 0;
	private long lastOptTime = 0;
	
	private DecimalPoint lastLoc = new DecimalPoint(0, 0);
	private long timeAtPoint = 0;
	private boolean begun = false;
	private static final double STALL_RADIUS = 5;
	private static final int STALL_TIME = 60000;
	private static final double RUN_ROTATION = 5*Math.PI/16.0;
	private static final int RUN_TIME = 15000;
	
	private Set<Point> visitedScience = new HashSet<Point>();
	
	private double[] mentality = new double[] { 10000, 3000, 1200, 500, 50 };
	private String mentalityStr;
	private boolean runyet = false;
	
	public GORAROAdvanceCode(double[] attitudes){
		super("GORARO Adv.", "GORARO");
		potentials = new double[histories][sampleDirections];
		String print = "";
		for (int i = 0; i < mentality.length; i++){
			print += attitudes[i] + "; ";
			mentality[i] = attitudes[i];
		}
		mentalityStr = "Using Mentality: {" + print + "}";
	}
	
	public GORAROAdvanceCode(GORAROAdvanceCode org) {
		super(org);
		score = org.score;
		state = org.state;
		this.sampleDirections = org.sampleDirections;
		this.sampleRadius = org.sampleRadius;
		this.averagingRadius = org.averagingRadius;
		this.averagingAngle = org.averagingAngle;
		this.mentality = org.mentality;
		this.targetDirection = org.targetDirection;
		this.lastOptTime = org.lastOptTime;
		this.mentalityStr = org.mentalityStr;
		this.runyet = org.runyet;
		potentials = new double[histories][sampleDirections];
		this.targetDirection = org.targetDirection;
		this.lastOptTime = org.lastOptTime;		
		this.lastLoc = org.lastLoc;
		this.timeAtPoint = org.timeAtPoint;
		this.begun = org.begun;
	}

	@Override
	public String nextCommand(long milliTime, DecimalPoint location,
			double direction, double acceleration, double angular_acceleration,
			double wheel_speed_FL, double wheel_speed_FR,
			double wheel_speed_BL, double wheel_speed_BR,
			double motor_current_FL, double motor_current_FR,
			double motor_current_BL, double motor_current_BR,
			double motor_temp_FL, double motor_temp_FR, double motor_temp_BL,
			double motor_temp_BR, double battery_voltage,
			double battery_current, double battery_temp, double battery_charge) 
	{
		if (!runyet){
			writeToLog(mentalityStr);
			writeToLog("time\tX\tY\tZ\tscore\tcharge\tstate\thazard");
			runyet = true;
		}
		writeToLog(milliTime + "\t" + formatDouble(location.getX()) + "\t" + formatDouble(location.getY()) + "\t" + formatDouble(Access.getMapHeightatPoint(location)) + "\t" + score + "\t" + formatDouble(battery_charge) + "\t" + state + "\t" + Access.getHazardValue(location));
		direction = (direction + 2*Math.PI) % (2*Math.PI);
		if (hasUnvisitedScience(location)){
			score += Access.getTargetValue(location);
			Point mapLoc = Admin.GUI.TerrainPnl.HeightMap.getMapSquare(location);
			visitedScience.add(new Point(mapLoc.x/3, mapLoc.y/3));
			for (int x = 0; x < histories; x++){
				for (int y = 0; y < sampleDirections; y++){
					potentials[x][y] = 0;
				}
			}
			//System.out.println("Score = " + score);
		}
		int[] sciences = new int[sampleDirections];
		int[] hazards = new int[sampleDirections];
		switch (state){
		case 1:
			for (int i = 1; i < histories; i++){
				for (int j = 0; j < sampleDirections; j++){
					potentials[i-1][j] = potentials[i][j];
				}
			}
			if (Math.sqrt(Math.pow(location.getX()-lastLoc.getX(), 2) + Math.pow(location.getY()-lastLoc.getY(), 2)) < STALL_RADIUS){
				if (milliTime-timeAtPoint > STALL_TIME){
					if (begun){
						state = 4;
						targetDirection = (direction - RUN_ROTATION + Math.PI*2) % (2*Math.PI);
						return "spin_cw";
					}
					else {
						timeAtPoint = milliTime;
						begun = true;
					}
				}
			}
			else {
				
				lastLoc = location.clone();
				timeAtPoint = milliTime;
			}
			double maxPotential = Integer.MIN_VALUE;
			double maxDirection = 0;
			for (int i = 0; i < sampleDirections; i++){
				double theta = 2*Math.PI*i/(double)sampleDirections;
				double deltaX = sampleRadius * Math.cos(theta);
				double deltaY = sampleRadius * Math.sin(theta);
				DecimalPoint examine = location.offset(deltaX, deltaY);
				
				//if there is a scientific value at the point raise priority
				int science = 0;
				for (int radius = 0; radius < sampleRadius; radius++){
					if (hasUnvisitedScience(location.offset(radius*Math.cos(theta), radius*Math.sin(theta)))){
						science = Access.getTargetValue(location.offset(radius*Math.cos(theta), radius*Math.sin(theta)));
						break;
					}
				}
				
				//if there is a hazard at the point get less excited
				int hazard = 0;
				if (Access.isInHazard(examine)){
					hazard = Access.getHazardValue(location.offset(sampleRadius*Math.cos(theta), sampleRadius*Math.sin(theta)));
				}
				
				//Calculate the density of science targets in a wedge away from the rover 
				double scienceArea = 0;
				for (double radius = sampleRadius; radius <= averagingRadius; radius += averagingRStep){
					for (double phi = -averagingAngle/2.0; phi <= averagingAngle/2.0; phi += averagingPStep){
						if (hasUnvisitedScience(location.offset(radius*Math.cos(theta+phi), radius*Math.sin(theta+phi)))){
							scienceArea += sampleRadius/radius;
							sciences[i] += Access.getTargetValue(location.offset(radius*Math.cos(theta+phi), radius*Math.sin(theta+phi)));
						}
					}
				}
				scienceArea /= averagingRadius*averagingAngle;
				
				//Calculate the density of hazards in a wedge away from the rover 
				double hazardArea = 0;
				for (double radius = sampleRadius; radius <= averagingRadius; radius += averagingRStep){
					for (double phi = -averagingAngle/2.0; phi <= averagingAngle/2.0; phi += averagingPStep){
						if (Access.isInHazard(location.offset(radius*Math.cos(theta+phi), radius*Math.sin(theta+phi)))){
							hazardArea += sampleRadius/radius;
							hazards[i] += Access.getHazardValue(location.offset(radius*Math.cos(theta+phi), radius*Math.sin(theta+phi)));
						}
					}
				}
				hazardArea /= averagingRadius*averagingAngle;
				
				//work required to move the same translational distance increases proportional to the tangent of the slope
				double heightDif = Access.getMapHeightatPoint(examine)-Access.getMapHeightatPoint(location);
				double energyCost;
				if (Math.abs(Math.atan(heightDif/sampleRadius)) > 0.104719){
					energyCost = 100;
				}
				else {
					energyCost = Math.atan(heightDif/sampleRadius)/0.104719*10;
				}
				
				//calculate the potential of the point
				potentials[histories-1][i] = mentality[0]*science - mentality[1]*hazard + mentality[2]*scienceArea - mentality[3]*hazardArea - mentality[4]*energyCost;
				double average = 0;
				for (int j = 0; j < histories; j++){
					average += potentials[j][i];
				}
				average /= (double)histories;
				if (average > maxPotential){
					maxPotential = average;
					maxDirection = theta;
				}
			}
			lastOptTime = milliTime;
			//if (true){
			//	for (double pot : potentials){
			//		System.out.print(pot + "\t");
			//	}
			//	System.out.println();
			//}
			
//			System.out.println("\n\n\n\n\n\n");
//			System.out.println("\t\t" + formatDouble(potentials[histories-1][4])+"("+sciences[4]+", "+hazards[4]+")");
//			System.out.println("\t" + formatDouble(potentials[histories-1][6])+"("+sciences[6]+", "+hazards[6]+")" + "\t\t" + formatDouble(potentials[histories-1][2])+"("+sciences[2]+", "+hazards[2]+")");
//			System.out.println(formatDouble(potentials[histories-1][8])+"("+sciences[8]+", "+hazards[8]+")" + "\t\t\t\t" + formatDouble(potentials[histories-1][0])+"("+sciences[0]+", "+hazards[0]+")");
//			System.out.println("\t" + formatDouble(potentials[histories-1][10])+"("+sciences[10]+", "+hazards[10]+")" + "\t\t" + formatDouble(potentials[histories-1][14])+"("+sciences[14]+", "+hazards[14]+")");
//			System.out.println("\t\t" + formatDouble(potentials[histories-1][12])+"("+sciences[12]+", "+hazards[12]+")");
		
			targetDirection = maxDirection;
			state++;
			if (Globals.subtractAngles(direction, targetDirection) < 0){
				return "spin_cw";
			}
			else {
				return "spin_ccw";
			}
		case 2:
			//System.out.println(direction + " - " + targetDirection);
			if (Math.abs(direction-targetDirection) < ANGLE_ERROR){
				state++;
				return "move"; 
			}
			else {
				if (Globals.subtractAngles(direction, targetDirection) < 0){
					return "spin_cw";
				}
				else {
					return "spin_ccw";
				}
			}
		case 3:
			if (milliTime-lastOptTime > RECALC_TIME){
				state = 1;
			}
			return "";
		case 4:
			if (Globals.subtractAngles(targetDirection, direction) > 0){
				return "";
			}
			else {
				state = 5;
				lastOptTime = milliTime;
				return "move";
			}
		case 5:
			if (milliTime-lastOptTime > RUN_TIME){
				lastOptTime = (long)(milliTime - RECALC_TIME);
				state = 1;
			}
			return "";
		default:
			state = 1;
			return "";
		}
	}

	private boolean hasUnvisitedScience(DecimalPoint loc){
		if (Access.isAtTarget(loc)){
			Point mapLoc = Admin.GUI.TerrainPnl.HeightMap.getMapSquare(loc);
			return !visitedScience.contains(new Point(mapLoc.x/3, mapLoc.y/3));
		}
		return false;
	}
	
	@Override
	public GORAROAdvanceCode clone(){
		return new GORAROAdvanceCode(this);
	}
	
	private String formatDouble(double in){ 
		String out = "";
		if (Math.abs(in) < Integer.MAX_VALUE/1000){
			if (in < 0){
				in *= -1;
				out = "-";
			}
			int whole = (int)in;
			out += whole;
			int part = (int)((in * 1000) - whole*1000);
			if (part == 0){
				out += ".000";
			}
			else if (part < 10){
				out += "." + part + "00";
			}
			else if (part < 100){
				out += "." + part + "0";
			}
			else {
				out += "." + part;
			}
		}
		else {
			out = (int)in + "";
		}
		return out;
	}

}
