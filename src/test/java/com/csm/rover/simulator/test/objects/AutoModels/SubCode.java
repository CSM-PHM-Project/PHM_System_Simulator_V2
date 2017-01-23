package com.csm.rover.simulator.test.objects.AutoModels;

import com.csm.rover.simulator.platforms.PlatformState;
import com.csm.rover.simulator.platforms.annotations.AutonomousCodeModel;

import java.util.Map;

@AutonomousCodeModel(type = "Sub", name = "Test Sub", parameters = {"param1", "param2"})
public class SubCode extends AbstractSubCode {

    public SubCode(){

    }

    @Override
    public void constructParameters(Map<String, Double> params) {

    }

    @Override
    public String nextCommand(long milliTime, PlatformState state) {
        return null;
    }

}
