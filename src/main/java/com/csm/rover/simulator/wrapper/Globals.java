package com.csm.rover.simulator.wrapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.csm.rover.simulator.objects.FreeThread;
import com.csm.rover.simulator.objects.ThreadItem;
import com.csm.rover.simulator.objects.SynchronousThread;
import com.csm.rover.simulator.visual.AccelPopUp;

//TODO make into singleton
public class Globals {

	public static String versionNumber = "2.2.1";
	
	private static Map<String, Queue<Byte>> SerialBuffers = new HashMap<String, Queue<Byte>>(); // the buffer for messages
	
	private static final double time_accelerant = 10;
	private static double timeScale = 1.0;
	public static long TimeMillis = 0;
	
	private static Random rnd = new Random();
	
	private static boolean begun = false;
	private static Map<String, ThreadItem> threads = new ConcurrentHashMap<String, ThreadItem>();
	private static boolean milliDone = false;
	
	private static int exitTime = -1;
	private static AccelPopUp informer;
	
	public static void startTime(boolean accel){
		begun = true;
		if (accel){
			timeScale = time_accelerant;
		}
		Access.INTERFACE.clock.start();
		ThreadItem.offset = 0;
		new FreeThread(0, new Runnable(){
			public void run(){
				long end = System.nanoTime() + (long)(1000000 / getTimeScale());
				while (System.nanoTime() < end) {}
				threadCheckIn("milli-clock");
			}
		}, SynchronousThread.FOREVER, "milli-clock", true);
	}
	
	public static void writeToSerial(char write, String from){
		writeToSerial((byte)write, from);
	}
	
	@SuppressWarnings("unchecked")
	public static void initializeLists(ArrayList<String> IDs){
        for (String id : IDs){
            SerialBuffers.put(id, new LinkedList<Byte>());
        }
	}
	
	public static void writeToSerial(byte write, String from){ // writes the character to the other 2 buffers
		for (String id : SerialBuffers.keySet()){
			if (!id.equals(from)){
				if (SerialBuffers.get(id).size() < 64){
					SerialBuffers.get(id).add(write);
				}
				else {
					writeToLogFile(id, "Failed to receive: " + (char)write + ", full buffer.");
				}
			}
		}
		Access.CODE.updateSerialDisplays();
	}
	
	public static int RFAvailable(String which){ // Returns the number of chars waiting
		for (String id : SerialBuffers.keySet()){
			if (id.equals(which)){
				return SerialBuffers.get(id).size();
			}
		}
		return -1;
	}
	
	public static byte ReadSerial(String which){ // Returns the first waiting character
		byte out = '\0';
		for (String id : SerialBuffers.keySet()){
			if (id.equals(which)){
				out = SerialBuffers.get(id).poll();
				break;
			}
		}
		Access.CODE.updateSerialDisplays();
		return out;
	}
	
	public static byte PeekSerial(String which){  // get first waiting character without changing availability
		byte out = '\0';
		for (String id : SerialBuffers.keySet()){
			if (id.equals(which)){
				out = SerialBuffers.get(id).peek();
				break;
			}
		}
		return out;
	}
	
	public static double getTimeAccelerant() {
		return time_accelerant;
	}

	public static double getTimeScale(){
		return timeScale;
	}
	
	public static void reportError(String obj, String method, Exception error){
		writeToLogFile("ERROR - " + Thread.currentThread().getName() + ": " + obj + ": " + method, error.toString());
		System.err.println(Thread.currentThread().getName());
		if (error != null){
			error.printStackTrace();
		}
	}
	
	public synchronized static void writeToLogFile(String from, String write){ // writes the message to the log generated by the interface
		Access.INTERFACE.writeToLog(from, write);
	}
	
	public static double addErrorToMeasurement(double measurement, double percentError){ // takes in a measurement an adds error by the given percent
		double deviation = rnd.nextDouble()*(measurement*percentError);
		if (rnd.nextBoolean()){
			measurement += deviation;
		}
		else {
			measurement -= deviation;
		}
		return Math.round(measurement*1000)/1000;
	}
	
	public static double addErrorToMeasurement(double measurement, double lowDeviation, double highDeviation){ // takes in a measurement and varies it within measure-lowDev to measure+highDev
		double deviation = rnd.nextDouble()*(lowDeviation + highDeviation) - lowDeviation;
		return Math.round((measurement + deviation)*1000)/1000;
	}
	
	@SuppressWarnings("unused")
	private static void printBuffers(){
        for (Map.Entry<String, Queue<Byte>> entry : SerialBuffers.entrySet()){
            writeToLogFile("Timing", entry.getKey() + ": " + entry.getValue().toString());
        }
	}
	
	//NOT A TRUE SUBTRACTION: if the second angle is clockwise of the first, returns the negative number of units between, positive if second is to ccw
	public static double subtractAngles(double first, double second){
		double one = first - second;
		double two = ((first+Math.PI)%(2*Math.PI)) - ((second+Math.PI)%(2*Math.PI));
		if (Math.abs(one-two) < 0.00001){
			return -1*one;
		}
		if (Math.abs(one) > Math.abs(two)){
			return -1*two;
		}
		else {
			return -1*one;
		}
	}
	
	//NOT A TRUE SUBTRACTION: if the second angle is clockwise of the first, returns the negative number of units between, positive if second is to ccw
	public static double subtractAnglesDeg(double first, double second){
		double one = first - second;
		double two = ((first+180)%360) - ((second+180)%360);
		if (Math.abs(one-two) < 0.00001){
			return -1*one;
		}
		if (Math.abs(one) > Math.abs(two)){
			return -1*two;
		}
		else {
			return -1*one;
		}
	}
	
	static ArrayList<Queue<Byte>> getSerialQueues(){
		try {
            ArrayList<Queue<Byte>> out = new ArrayList<Queue<Byte>>(SerialBuffers.size());
            for (Map.Entry<String, Queue<Byte>> entry : SerialBuffers.entrySet()){
                out.add(entry.getValue());
            }
            return out;
		}
		catch (Exception e){
            reportError("Globals", "getSerialQueues", e);
			e.printStackTrace();
			return null;
		}		
	}
	
	public static void setUpAcceleratedRun(int runtime){
		exitTime = runtime;
		informer = new AccelPopUp(exitTime, (int) (exitTime/time_accelerant/60000));
		new FreeThread(1000, new Runnable(){
			public void run(){
				informer.update((int)TimeMillis);
				if (TimeMillis >= exitTime){
					Admin.GUI.exit();
				}
			}
		}, FreeThread.FOREVER, "accel-pop-up");
	}
	
	public static void registerNewThread(String name, int delay, SynchronousThread thread){
		threads.put(name, new ThreadItem(name, delay, TimeMillis, thread));
	}
	
	public static void checkOutThread(String name){
		if (!name.contains("delay")){
			System.err.println(name + " out.");
		}
		threads.remove(name);
		threadCheckIn(name);
	}
	
	public static void threadCheckIn(String name){
		try {
			threads.get(name).markFinished();
			threads.get(name).advance();
		} catch (NullPointerException e) { System.err.println("In thread " + name); e.printStackTrace(); }
		if (name.equals("milli-clock") || milliDone){
			for (Object o : threads.keySet()){
				String key = (String) o;
				if (threads.containsKey(key)){
					if (key.equals("milli-clock")){
						continue;
					}
					if (!threads.get(key).isFinished()){
						milliDone = true;
						threads.get(key).shakeThread();
						return;
					}
				}
			}
			milliDone = false;
			TimeMillis++;
			for (Object o : threads.keySet()){
				try {
					String key = (String) o;
					threads.get(key).reset();
					if (threads.get(key).getNext() <= TimeMillis){
						threads.get(key).grantPermission();
					}
				}
				catch (NullPointerException e){ e.printStackTrace(); }
			}
		}
	}
	
	public static String delayThread(String name, int time){
		try {
			if (threads.get(name) == null){
				throw new Exception();
			}
			String rtnname = name +"-delay";
			ThreadItem.offset = 0;
			registerNewThread(rtnname, time, null);
			threads.get(name).suspend();
			return rtnname;
		}
		catch (Exception e){
			try{
				Thread.sleep((long)(time/getTimeScale()), (int)((time/getTimeScale()-time/getTimeScale())*1000000));
			} catch (Exception ex) {
				Globals.reportError("Globals", "delayThread", ex);
			}
			return "pass";
		}
	} 
	
	public static void threadDelayComplete(String name){
		checkOutThread(name+"-delay");
		threads.get(name).unSuspend();
	}
	
	public static void threadIsRunning(String name){
		try {
			threads.get(name).setRunning(true);
		} catch (NullPointerException e) { e.printStackTrace(); }
	}
	
	public static boolean getThreadRunPermission(String name){
		try {
			if (threads.get(name).hasPermission() && begun){
				threads.get(name).revokePermission();
				return true;
			}
		}
		catch (NullPointerException e){
			e.printStackTrace();
		}
		return false;
	}
	
}