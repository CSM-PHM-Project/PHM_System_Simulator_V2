package com.csm.rover.simulator.test;

import static org.junit.Assert.*;

import java.awt.Point;

import com.csm.rover.simulator.map.TerrainMap;

import org.junit.BeforeClass;
import org.junit.Test;

public class TargetsAndHazards {

	static TerrainMap map;
	
	@BeforeClass
	public static void setup(){
		map = new TerrainMap();
		map.generateLandscape(7, 3, 0.01);
	}
	
	@Test
	public void TargetPlacement() {
		map.setTargets(new Point[]{ new Point(3, 5) });
		assertTrue(map.isPointOnTarget(null));
	}

}
