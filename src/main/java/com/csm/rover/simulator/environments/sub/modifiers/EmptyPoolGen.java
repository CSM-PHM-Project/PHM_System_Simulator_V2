package com.csm.rover.simulator.environments.sub.modifiers;

import com.csm.rover.simulator.environments.EnvironmentModifier;
import com.csm.rover.simulator.environments.annotations.Modifier;
import com.csm.rover.simulator.environments.sub.AquaticMap;
import com.csm.rover.simulator.objects.ArrayGrid3D;
import com.csm.rover.simulator.objects.FloatArrayArrayArrayGrid;

import java.util.Map;

@Modifier(name="Empty Pool", type="Sub", parameters={"size", "detail"}, generator=true)
public class EmptyPoolGen extends EnvironmentModifier<AquaticMap> {

    public EmptyPoolGen() {
        super("Sub", true);
    }

    @Override
    protected AquaticMap doModify(AquaticMap map, Map<String, Double> params) {
        int size = params.get("size").intValue();
        int detail = params.get("detail").intValue();
        int true_size = size*detail;

        ArrayGrid3D<Float> densityMap = new FloatArrayArrayArrayGrid();
        densityMap.fillToSize(true_size,true_size,true_size);
        return new AquaticMap(size, detail, densityMap);
    }

}