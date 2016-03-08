package com.csm.rover.simulator.platforms.satellite;

import com.csm.rover.simulator.platforms.PlatformPhysicsModel;
import com.csm.rover.simulator.platforms.PlatformState;
import com.csm.rover.simulator.platforms.annotations.PhysicsModel;

import java.io.Serializable;
import java.util.Map;

@PhysicsModel(type="Satellite", name="Default", parameters={})
public class SatelliteParametersList extends PlatformPhysicsModel implements Serializable {

    public SatelliteParametersList(){
        super("Satellite");
    }

	private static final long serialVersionUID = 1L;

    @Override
    public void constructParameters(Map<String, Double> params){

    }

    @Override
    public void start() {

    }

    @Override
    public void updatePhysics() {

    }

    @Override
    public void setPlatformName(String name) {

    }

    @Override
    public void initializeState(PlatformState state) {

    }

    @Override
    public PlatformState getState() {
        return null;
    }

}
