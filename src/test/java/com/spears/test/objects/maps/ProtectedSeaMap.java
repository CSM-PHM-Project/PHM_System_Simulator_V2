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

package com.spears.test.objects.maps;

import com.spears.environments.EnvironmentMap;
import com.spears.environments.annotations.Map;

@Map(type = "Sub")
public class ProtectedSeaMap extends EnvironmentMap {

    protected ProtectedSeaMap() {
        super("Sub");
    }

    @Override
    protected boolean isEqual(EnvironmentMap other) {
        return this == other;
    }
}