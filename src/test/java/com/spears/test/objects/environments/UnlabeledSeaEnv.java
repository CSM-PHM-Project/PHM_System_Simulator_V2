/*
 * SPEARS: Simulated Physics and Environment for Autonomous Risk Studies
 * Copyright (C) 2017  Colorado School of Mines
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.spears.test.objects.environments;

import com.spears.environments.PlatformEnvironment;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UnlabeledSeaEnv extends PlatformEnvironment {

    @JsonProperty("ID")
    private final String ID;

    public UnlabeledSeaEnv() {
        super("Sub");
        ID = toString();
    }

    @JsonCreator
    public UnlabeledSeaEnv(@JsonProperty("ID") String id){
        super("Sub");
        this.ID = id;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public boolean equals(Object o){
        return o instanceof UnlabeledSeaEnv &&
                this.ID.equals(((UnlabeledSeaEnv)o).ID);
    }

}
