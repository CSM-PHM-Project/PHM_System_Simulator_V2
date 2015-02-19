package rover;

import java.io.Serializable;

import objects.DecimalPoint;
import wrapper.Access;

public class RoverPhysicsModel implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	public static final int FL = 0, BL = 1, BR = 2, FR = 3; // identifiers on wheels

	public final double time_step = 0.01; // time step of physics, in seconds
	
	private final double wheel_radius = 0.0476; //m
	private final double wheel_mass = 0.064; //kg
	private final double rover_width = 0.438; //m
	private final double rover_length = 0.229; //m
	private final double motor_arm = Math.sqrt(Math.pow(rover_width, 2) + Math.pow(rover_length, 2)) / 2.0; //m
	private final double rover_mass = 2.266; //kg
	private final double rover_inertia = 0.1025; //kg*m^2
	private final double wheel_inertia = 0.5 * wheel_mass * Math.pow(wheel_radius, 2); //kg*m^2
	
	private final double motor_energy_transform = 0.035;
	private final double motor_voltage_transform = 0.571;
	private final double motor_resistance = 3; //Ohm
	private final double motor_inductance = 0.11455; //H
	private final double friction_axle = 0.0002621;
	private final double friction_gr = .65;
	private final double friction_s = 1.2;
	private final double gamma = Math.atan(1/rover_width);
	
	private final double R_cp0 = 0.07; //Ohm
	private final double R_cp1 = 0.01; //Ohm
	private final double R_cp2 = 3;
	private final double capacitance_battery = 12000; //F
	private final double capacitance_cp = 0.2; //F
	private final double resistance_s = 0.01; //Ohm
	private final double resistance_parasite = 100000000; //Ohm
	private final double battery_max_charge = 140000; //C
	
	private final double battery_heat_transfer = 10; //J/s/*c
	private final double battery_thermal_cap = 170; //J/K
	
	private final double winding_heat_transfer = 2; //J/s/*c
	private final double winding_thermal_cap = 1.7; //J/*c
	private final double motor_surface_heat_transfer = 0.9; //J/s/*c
	private final double motor_thermal_cap = 0.8; //J/*c	
	
	private double fric_gr_all = 0;
	private double[] slip = { 0, 0, 0, 0 };		
	
	private int[] motor_power = new int[] {  250, 250, 250, 250 }; // assigned motor powers
	private int[] motor_states = new int[] { 0, 0, 0, 0 }; // assigned motor states
	private double[] wheel_speed = { 0, 0, 0, 0 }; //rad/s	
	private double[] motor_current = { 0, 0, 0, 0 }; //A
	
	private double battery_charge; //C
	private double battery_cp_charge = 0; //C
	private double battery_voltage = 12; //V
	private double battery_current = 0; //A
	private double SOC = 1;
	
	private double battery_temperature = 30; //*c
	private double[] winding_temp = { 30, 30, 30, 30 }; //*c
	private double[] motor_temp = { 30, 30, 30, 30 }; //*c
	
	private DecimalPoint location; //m x m from center of map
	private double direction; //rad off of positive X
	private double speed = 0; //m/s
	private double angular_velocity = 0; //rad/s
	private double acceleration = 0; //m/s^2
	private double angular_acceleration = 0; //rad/s^2
	private double slip_acceleration = 0; //m/s^2
	private double slip_velocity = 0; //m/s
	
	public RoverPhysicsModel() {}
	
	public void excecute(String name) throws Exception {
		// Motor Currents, based on voltage
		motor_current[FL] += ( motor_power[FL]*motor_states[FL]/255.0*battery_voltage - motor_voltage_transform*wheel_speed[FL] - motor_current[FL]*motor_resistance) / motor_inductance * time_step;
		motor_current[FR] += ( motor_power[FR]*motor_states[FR]/255.0*battery_voltage - motor_voltage_transform*wheel_speed[FR] - motor_current[FR]*motor_resistance) / motor_inductance * time_step;
		motor_current[BL] += ( motor_power[BL]*motor_states[BL]/255.0*battery_voltage - motor_voltage_transform*wheel_speed[BL] - motor_current[BL]*motor_resistance) / motor_inductance * time_step;
		motor_current[BR] += ( motor_power[BR]*motor_states[BR]/255.0*battery_voltage - motor_voltage_transform*wheel_speed[BR] - motor_current[BR]*motor_resistance) / motor_inductance * time_step;
		// min currents at 0, motor cannot generate current
		/*if (motor_current[FL]*motor_states[FL] <= 0){
			motor_current[FL] = 0;
		}
		if (motor_current[FR]*motor_states[FR] <= 0){
			motor_current[FR] = 0;
		}
		if (motor_current[BL]*motor_states[BL] <= 0){
			motor_current[BL] = 0;
		}
		if (motor_current[BR]*motor_states[BR] <= 0){
			motor_current[BR] = 0;
		}*/
		// angular motor speeds, based on torques
		wheel_speed[FL] += 1/wheel_inertia * ( motor_energy_transform*motor_current[FL] - wheel_radius*slip[FL] + wheel_radius*fric_gr_all*Math.cos(gamma) - friction_axle*wheel_speed[FL]/wheel_radius) * time_step;
		wheel_speed[FR] += 1/wheel_inertia * ( motor_energy_transform*motor_current[FR] - wheel_radius*slip[FR] - wheel_radius*fric_gr_all*Math.cos(gamma) - friction_axle*wheel_speed[FR]/wheel_radius) * time_step;
		wheel_speed[BL] += 1/wheel_inertia * ( motor_energy_transform*motor_current[BL] - wheel_radius*slip[BL] + wheel_radius*fric_gr_all*Math.cos(gamma) - friction_axle*wheel_speed[BL]/wheel_radius) * time_step;
		wheel_speed[BR] += 1/wheel_inertia * ( motor_energy_transform*motor_current[BR] - wheel_radius*slip[BR] - wheel_radius*fric_gr_all*Math.cos(gamma) - friction_axle*wheel_speed[BR]/wheel_radius) * time_step;
		// translational friction, approximately the same for all wheels
		fric_gr_all = friction_gr * motor_arm * angular_velocity;
		// Slip forces on wheels, based on speed differences
		slip[FL] = friction_s * (wheel_speed[FL]*wheel_radius - speed);
		slip[FR] = friction_s * (wheel_speed[FR]*wheel_radius - speed);
		slip[BL] = friction_s * (wheel_speed[BL]*wheel_radius - speed);
		slip[BR] = friction_s * (wheel_speed[BR]*wheel_radius - speed);
		// Acceleration changes based on forces
		acceleration = 1/rover_mass*(slip[FL] + slip[BL] + slip[FR] + slip[BR]) - Access.getPlanetParameters().getgrav_accel()*Math.sin(Access.getMapInclineAtPoint(location, direction));
		angular_acceleration = 1/rover_inertia * ((motor_arm*(slip[FR] + slip[BR] - slip[FL] - slip[BL])*Math.cos(gamma) - motor_arm*(4*fric_gr_all)));
		// Speed changes based on Acceleration
		speed += acceleration * time_step;
		angular_velocity += angular_acceleration * time_step;
			//System.out.println(round(wheel_speed[FL]) + " rad/s -> " + round(slip[FL]) + " N");
			//System.out.println(round(wheel_speed[FR]) + " rad/s -> " + round(slip[FR]) + " N -> " + round(acceleration) + " m/s^2 -> " + round(speed) + " m/s");
			//System.out.println(round(wheel_speed[BL]) + " rad/s -> " + round(slip[BL]) + " N -> " + round(angular_acceleration) + " rad/s^2 -> " + round(angular_velocity) + " rad/s");
			//System.out.println(round(wheel_speed[BR]) + " rad/s -> " + round(slip[BR]) + " N");
		// Calculate the amount the rover slips sideways
		slip_acceleration = (-friction_gr*slip_velocity*4 - rover_mass*Access.getPlanetParameters().getgrav_accel()*Math.sin(Access.getMapCrossSlopeAtPoint(location, direction)) / rover_mass);
		slip_velocity += slip_acceleration * time_step;
		// Calculate new location
		location.offsetThis(speed*time_step*Math.cos(direction), speed*time_step*(Math.sin(direction)));
		//TODO													  + here??
		location.offsetThis(slip_velocity*time_step*Math.cos(direction-Math.PI/2.0), slip_velocity*time_step*(Math.sin(direction-Math.PI/2.0)));
		direction += angular_velocity*time_step;
		// report new location to map
		Access.updateRoverLocation(name, location, direction);
		
		//Determining the current of the battery and the change in the stored charge
		if (motor_current[FL]*motor_states[FL] <= 0){
			motor_current[FL] = 0;
		}
		if (motor_current[FR]*motor_states[FR] <= 0){
			motor_current[FR] = 0;
		}
		if (motor_current[BL]*motor_states[BL] <= 0){
			motor_current[BL] = 0;
		}
		if (motor_current[BR]*motor_states[BR] <= 0){
			motor_current[BR] = 0;
		}
		battery_current = Math.abs(motor_current[FL]) + Math.abs(motor_current[FR]) + Math.abs(motor_current[BL]) + Math.abs(motor_current[BR]);
		double battery_change = battery_charge / capacitance_battery / resistance_parasite + battery_current;
		double cp_change = battery_current - (battery_cp_charge / capacitance_cp / resistance_cp());
		battery_charge -= battery_change * time_step;
		battery_cp_charge += cp_change * time_step;
		battery_voltage = battery_charge/capacitance_battery - battery_cp_charge/capacitance_cp - resistance_s*battery_current;
		SOC = 1 - (battery_max_charge - battery_charge) / battery_max_charge;
			//System.out.println("Vb: " + battery_voltage + "\tVm: " + getMotorVoltage(FR) + "\tQcp: " + battery_cp_charge + "\tIb: " + battery_current + "\tIm: " + motor_current[FR]);
		
		//Determining the temperature of the battery
		battery_temperature += ((resistance_parasite*Math.pow(battery_change-battery_current, 2) + resistance_s*Math.pow(battery_current, 2) + getcapacitance_cp()*Math.pow(battery_current-cp_change, 2)) - battery_heat_transfer*(battery_temperature - Access.getMapTemperatureAtPoint(location)) / battery_thermal_cap) * time_step;
		//Determining the temperature of the motor coils
		winding_temp[FL] += ((motor_resistance*Math.pow(motor_current[FL], 2) - winding_heat_transfer*(winding_temp[FL] - motor_temp[FL])) / winding_thermal_cap) * time_step;
		winding_temp[FR] += ((motor_resistance*Math.pow(motor_current[FR], 2) - winding_heat_transfer*(winding_temp[FR] - motor_temp[FR])) / winding_thermal_cap) * time_step;
		winding_temp[BL] += ((motor_resistance*Math.pow(motor_current[BL], 2) - winding_heat_transfer*(winding_temp[BL] - motor_temp[BL])) / winding_thermal_cap) * time_step;
		winding_temp[BR] += ((motor_resistance*Math.pow(motor_current[BR], 2) - winding_heat_transfer*(winding_temp[BR] - motor_temp[BR])) / winding_thermal_cap) * time_step;
		//Determining the surface temperature of the motor
		motor_temp[FL] += ((winding_heat_transfer*(winding_temp[FL] - motor_temp[FL]) - motor_surface_heat_transfer*(motor_temp[FL] - Access.getMapTemperatureAtPoint(location))) / motor_thermal_cap) * time_step;
		motor_temp[FR] += ((winding_heat_transfer*(winding_temp[FR] - motor_temp[FR]) - motor_surface_heat_transfer*(motor_temp[FR] - Access.getMapTemperatureAtPoint(location))) / motor_thermal_cap) * time_step;
		motor_temp[BL] += ((winding_heat_transfer*(winding_temp[BL] - motor_temp[BL]) - motor_surface_heat_transfer*(motor_temp[BL] - Access.getMapTemperatureAtPoint(location))) / motor_thermal_cap) * time_step;
		motor_temp[BR] += ((winding_heat_transfer*(winding_temp[BR] - motor_temp[BR]) - motor_surface_heat_transfer*(motor_temp[BR] - Access.getMapTemperatureAtPoint(location))) / motor_thermal_cap) * time_step;				
	}
	
	@Override
	public RoverPhysicsModel clone(){
		return new RoverPhysicsModel();
	}
	
	private double resistance_cp(){ // get the resistance of the CP resistor as a function of SOC
		return R_cp0+R_cp1*Math.exp(R_cp2*(1-SOC));
	}
	
	public double getwheel_radius(){
		return wheel_radius;
	}

	public double getwheel_mass(){
		return wheel_mass;
	}

	public double getrover_width(){
		return rover_width;
	}

	public double getrover_length(){
		return rover_length;
	}

	public double getmotor_arm(){
		return motor_arm;
	}

	public double getrover_mass(){
		return rover_mass;
	}

	public double getrover_inertia(){
		return rover_inertia;
	}

	public double getwheel_inertia(){
		return wheel_inertia;
	}

	public double getmotor_energy_transform(){
		return motor_energy_transform;
	}

	public double getmotor_voltage_transform(){
		return motor_voltage_transform;
	}

	public double getmotor_resistance(){
		return motor_resistance;
	}

	public double getmotor_inductance(){
		return motor_inductance;
	}

	public double getfriction_axle(){
		return friction_axle;
	}

	public double getfriction_gr(){
		return friction_gr;
	}

	public double getfriction_s(){
		return friction_s;
	}

	public double getgamma(){
		return gamma;
	}

	public double getR_cp0(){
		return R_cp0;
	}

	public double getR_cp1(){
		return R_cp1;
	}

	public double getR_cp2(){
		return R_cp2;
	}

	public double getcapacitance_battery(){
		return capacitance_battery;
	}

	public double getcapacitance_cp(){
		return capacitance_cp;
	}

	public double getresistance_s(){
		return resistance_s;
	}

	public double getresistance_parasite(){
		return resistance_parasite;
	}

	public double getbattery_max_charge(){
		return battery_max_charge;
	}

	public double getbattery_heat_transfer(){
		return battery_heat_transfer;
	}

	public double getbattery_thermal_cap(){
		return battery_thermal_cap;
	}

	public double getwinding_heat_transfer(){
		return  winding_heat_transfer;
	}

	public double getwinding_thermal_cap(){
		return winding_thermal_cap;
	}

	public double getmotor_surface_heat_transfer(){
		return motor_surface_heat_transfer;
	}

	public double getmotor_thermal_cap(){
		return motor_thermal_cap;
	}

	public int[] getMotor_power() {
		return motor_power;
	}
	
	public int getMotor_power(int motor) {
		return motor_power[motor];
	}

	public void setMotor_power(int motor, int motor_power) {
		if (motor_power < 0){
			motor_power = 0;
		}
		else if (motor_power > 255){
			motor_power = 255;
		}
		this.motor_power[motor] = motor_power;
	}

	public int[] getMotor_states() {
		return motor_states;
	}

	public void setMotor_states(int wheel, int motor_states) {
		this.motor_states[wheel] = motor_states;
	}

	public double[] getMotor_current() {
		return motor_current;
	}
	
	public double getMotor_current(int wheel) {
		return motor_current[wheel];
	}

	public DecimalPoint getLocation() {
		return location;
	}

	public void setLocation(DecimalPoint location) {
		this.location = location;
	}

	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public double getFriction_axle() {
		return friction_axle;
	}

	public double getFriction_gr() {
		return friction_gr;
	}

	public double getFriction_s() {
		return friction_s;
	}

	public double[] getWheel_speed() {
		return wheel_speed;
	}
	
	public double getWheel_speed(int wheel) {
		return wheel_speed[wheel];
	}

	public double getSOC() {
		return SOC;
	}

	public double getSpeed() {
		return speed;
	}

	public double getSlip_acceleration() {
		return slip_acceleration;
	}

	public double getSlip_velocity() {
		return slip_velocity;
	}

	public void setBattery_charge(double battery_charge) {
		this.battery_charge = battery_charge;
	}

	public void setBattery_temperature(double battery_temperature) {
		this.battery_temperature = battery_temperature;
	}

	public void setMotor_temp(int motor, double motor_temp) {
		this.motor_temp[motor] = motor_temp;
	}
	
	public double getMotor_temp(int motor){
		return this.motor_temp[motor];
	}

	public double getBattery_voltage() {
		return battery_voltage;
	}

	public double[] getWinding_temp() {
		return winding_temp;
	}

	public void setWinding_temp(double[] winding_temp) {
		this.winding_temp = winding_temp;
	}

	public double[] getMotor_temp() {
		return motor_temp;
	}

	public double getBattery_charge() {
		return battery_charge;
	}

	public double getBattery_current() {
		return battery_current;
	}

	public double getAngular_velocity() {
		return angular_velocity;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public double getAngular_acceleration() {
		return angular_acceleration;
	}

	public void setMotor_power(int[] motor_power) {
		this.motor_power = motor_power;
	}

	public double getBattery_temperature() {
		return battery_temperature;
	}

	public double getBattery_cp_charge() {
		return battery_cp_charge;
	}
	
}
