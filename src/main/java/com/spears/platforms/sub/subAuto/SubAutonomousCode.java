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

package com.spears.platforms.sub.subAuto;

import com.spears.objects.io.DatedFileAppenderImpl;

import com.spears.environments.PlatformEnvironment;
import com.spears.environments.sub.AquaticEnvironment;
import com.spears.objects.util.DecimalPoint3D;

import com.spears.platforms.PlatformAutonomousCodeModel;
import com.spears.platforms.PlatformState;
import com.spears.platforms.annotations.AutonomousCodeModel;
import com.spears.platforms.sub.SubProp;
import com.spears.wrapper.Globals;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@AutonomousCodeModel(type = "Sub", name = "Default", parameters = {})
public abstract class SubAutonomousCode extends PlatformAutonomousCodeModel {
    private static final Logger LOG = LogManager.getLogger(SubAutonomousCode.class);

    protected AquaticEnvironment environment;

    private String name;
    private String subName;

    private File logFile;

    public SubAutonomousCode(String name, String sub){
        super("Sub");
        this.name = name;
        subName = sub;
    }
    @Override
    public String nextCommand(long millitime, final PlatformState state){
        if (!state.getType().equals("Sub")){
            throw new IllegalArgumentException("The provided state is not a SubState");
        }
        return doNextCommand(millitime, new DecimalPoint3D(state.<Double>get("x"), state.<Double>get("y"), state.<Double>get("z")),
                new double[]{ state.<Double>get("pitch"), state.<Double>get("yaw"), state.<Double>get("roll") },
                getAutonomousParameters(state));
    }

    protected abstract String doNextCommand(long milliTime, DecimalPoint3D location,
                                            double[] orientation, Map<String, Double> params);

    @Override
    public void setEnvironment(PlatformEnvironment environment){
        if (environment.getType().equals(platform_type)){
            this.environment = (AquaticEnvironment)environment;
        }
        else {
            throw new IllegalArgumentException("The given platform has the wrong type: " + environment.getType());
        }
    }

    public void setSubName(String name){
        subName = name;
    }

    public String getName(){
        return name;
    }

    private boolean tried = false;
    protected void writeToLog(String message){
        try {
            BufferedWriter write = new BufferedWriter(new FileWriter(logFile, true));
            write.write(message + "\t\t" + new DateTime().toString(DateTimeFormat.forPattern("[MM/dd/yyyy hh:mm:ss.")) + (Globals.getInstance().timeMillis() %1000) + "]\r\n");
            write.flush();
            write.close();
        }
        catch (NullPointerException e){
            if (!tried){
                tried = true;
                logFile = new File(generateFilepath());
                logFile.getParentFile().mkdirs();
                LOG.log(Level.INFO, "Writing sub {}'s autonomous log file to: {}", subName, logFile.getAbsolutePath());
                writeToLog(message);
            }
            else {
                LOG.log(Level.ERROR, "Sub " + subName + "'s autonomous log file failed to initialize.", e);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private Map<String, Double> getAutonomousParameters(PlatformState state){
        String[] fromLists = new String[] { "prop_speed", "motor_current", "motor_power" };
        Map<String, Double> params = new TreeMap<>();
        params.put("speed_x", state.<Double[]>get("speed")[0]);
        params.put("speed_y", state.<Double[]>get("speed")[1]);
        params.put("speed_z", state.<Double[]>get("speed")[2]);
        params.put("angular_speed_p", state.<Double[]>get("angular_speed")[0]);
        params.put("angular_speed_w", state.<Double[]>get("angular_speed")[1]);
        params.put("angular_speed_r", state.<Double[]>get("angular_speed")[2]);
        for (String param : fromLists){
            Double[] list = state.get(param);
            for (int i = 0; i < list.length; i++){
                for (SubProp prop : SubProp.values()){
                    if (prop.getValue() == i && prop.name().length() < 4){
                        params.put(param+"_"+prop.name(), list[i]);
                        break;
                    }
                }
            }
        }
        return params;
    }

    private String generateFilepath(){
        DateTime date = new DateTime();
        return String.format("%s/%s_%s.log", DatedFileAppenderImpl.Log_File_Name, subName, date.toString(DateTimeFormat.forPattern("MM-dd-yyyy_HH.mm")));
    }

}