package com.csm.rover.simulator.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import com.csm.rover.simulator.rover.RoverObject;
import com.csm.rover.simulator.satellite.SatelliteObject;

public class RunConfiguration implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String fileCode = "yeti";
	
	public boolean mapFromFile;
	public Map<String, String> roverNames;
	public ArrayList<RoverObject> rovers;
	public Map<String, String> satelliteNames;
	public ArrayList<SatelliteObject> satellites;
	public ArrayList<String> tags;
	public File mapFile;
	public double mapRough;
	public int mapSize;
	public int mapDetail;
	public double targetDensity;
	public double hazardDensity;
	public boolean monoTargets;
	public boolean monoHazards;
	public boolean accelerated;
	public int runtime;
	
	public RunConfiguration(Map<String, String> roverNames,
			ArrayList<RoverObject> rovers, Map<String, String> satelliteNames,
			ArrayList<SatelliteObject> satellites, ArrayList<String> tags, File mapFile,
			boolean accelerated, int runtime) {
		mapFromFile = true;
		this.roverNames = roverNames;
		this.rovers = rovers;
		this.satelliteNames = satelliteNames;
		this.satellites = satellites;
		this.tags = tags;
		this.mapFile = mapFile;
		this.accelerated = accelerated;
		this.runtime = runtime;
	}

	public RunConfiguration(Map<String, String> roverNames,
			ArrayList<RoverObject> rovers, Map<String, String> satelliteNames,
			ArrayList<SatelliteObject> satellites, ArrayList<String> tags, double mapRough,
			int mapSize, int mapDetail, double targetDensity,
			double hazardDensity, boolean monoTargets, boolean monoHazards, boolean accelerated, int runtime) {
		mapFromFile = false;
		this.roverNames = roverNames;
		this.rovers = rovers;
		this.satelliteNames = satelliteNames;
		this.satellites = satellites;
		this.tags = tags;
		this.mapRough = mapRough;
		this.mapSize = mapSize;
		this.mapDetail = mapDetail;
		this.targetDensity = targetDensity;
		this.hazardDensity = hazardDensity;
		this.monoTargets = monoTargets;
		this.monoHazards = monoHazards;
		this.accelerated = accelerated;
		this.runtime = runtime;
	}
	
	public RunConfiguration(File save) throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(save.getAbsolutePath()));
		RunConfiguration input = (RunConfiguration) in.readObject();
		if (!this.fileCode.equals(input.fileCode)){
			in.close();
			throw new Exception("Invalid File Version");
		}
		this.mapFromFile = input.mapFromFile;
		this.roverNames = input.roverNames;
		this.rovers = input.rovers;
		this.satelliteNames = input.satelliteNames;
		this.satellites = input.satellites;
		this.tags = input.tags;
		this.mapFile = input.mapFile;
		this.mapRough = input.mapRough;
		this.mapSize = input.mapSize;
		this.mapDetail = input.mapDetail;
		this.targetDensity = input.targetDensity;
		this.hazardDensity = input.hazardDensity;
		this.accelerated = input.accelerated;
		this.runtime = input.runtime;
		in.close();
	}

	public void Save(File file) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
		out.writeObject(this);
		out.close();
	}
	
}