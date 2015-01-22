package objects;

public class ThreadItem {
	
	private String name;
	private int delay;
	private long next;
	
	private boolean permission;
	private boolean running;
	private boolean complete;
	
	public static int offset = 0;

	public ThreadItem(String name, int delay, long start) {
		this.name = name;
		this.delay = delay;
		next = start + delay + offset;
		offset++;
	}

	public String getName() {
		return name;
	}

	public int getDelay() {
		return delay;
	}

	public void advance(){
		//System.out.println(next + " -> " + (next+delay));
		next += delay;
	}
	
	public long getNext() {
		return next;
	}
	
	public void grantPermission(){
		permission = true;
		running = true;
		complete = false;
	}
	
	public void revokePermission(){
		permission = false;
	}
	
	public void markFinished(){
		complete = true;
	}

	public boolean hasPermission() {
		return permission;
	}
	
	public boolean isFinished(){
		if (running){
			return complete;
		}
		else {
			return true;
		}
	}
	
	public void reset(){
		running = false;
		complete = false;
	}
	
}