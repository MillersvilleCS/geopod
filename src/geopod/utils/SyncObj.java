package geopod.utils;

public class SyncObj {
	private boolean condition;
	private Object obj = new Object ();
	
	public SyncObj () {
		this (false);
	}
	
	public SyncObj (boolean isMutex) {
		condition = isMutex;
	}
	
	public void doWait () {
		synchronized (obj) {
			while (!condition) {
				try {
					obj.wait ();
				} catch (InterruptedException e) {
					e.printStackTrace ();
				}
			}
			condition = false;
		}
	}
	
	public void doNotify () {
		synchronized (obj) {
			condition = true;
			obj.notify ();
		}
	}
	
	// For readability when using as a mutex.
	public void doAcquire () {
		this.doWait ();
	}
	
	public void doRelease () {
		this.doNotify ();
	}
}
