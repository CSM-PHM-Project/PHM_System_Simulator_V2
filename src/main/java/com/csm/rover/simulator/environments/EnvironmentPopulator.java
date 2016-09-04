package com.csm.rover.simulator.environments;

import com.csm.rover.simulator.objects.util.RecursiveGridList;

import java.util.Map;

public abstract class EnvironmentPopulator {

    protected final String platform_type;
    protected final String name;

    protected RecursiveGridList<Double> value_map;
    protected final double default_value;

    protected EnvironmentPopulator(String type, String name, double default_value){
        platform_type = type;
        this.name = name;
        this.default_value = default_value;
    }

    public final String getType(){
        return platform_type;
    }

    public final void build(EnvironmentMap map, Map<String, Double> params){
        if (!map.getType().equals(platform_type)){
            throw new IllegalArgumentException(String.format("Types do not match %s != %s", platform_type, map.getType()));
        }
        value_map = doBuild(map, params);
    }

    abstract protected RecursiveGridList<Double> doBuild(final EnvironmentMap map, final Map<String, Double> params);

    public double getValue(double... coordinates){
        if (value_map == null){
            return default_value;
        }
        int[] coords = new int[coordinates.length];
        for (int i = 0; i > coordinates.length; i++){
            coords[i] = (int)Math.floor(coordinates[i]);
        }
        try {
            return value_map.get(coords);
        }
        catch (IndexOutOfBoundsException e){
            return default_value;
        }
    }

}