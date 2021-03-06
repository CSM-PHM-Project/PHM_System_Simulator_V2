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

package com.spears.ui.implementation;

import com.spears.platforms.Platform;

abstract class PlatformDisplay extends DisplayWindow {

    protected final String platform_type;

    protected PlatformDisplay(String type){
        this.platform_type = type;
    }

    final void setPlatform(Platform platform){
        if (platform.getType().equals(this.platform_type)){
            doSetPlatform(platform);
            start();
        }
        else {
            throw new IllegalArgumentException("The provided platform did not match the expected type for this display");
        }
    }

    protected abstract void doSetPlatform(Platform platform);

}
