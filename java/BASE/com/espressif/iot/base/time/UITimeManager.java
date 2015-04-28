package com.espressif.iot.base.time;

public class UITimeManager {
	
	private static UITimeManager instance = new UITimeManager();
	/**
	 * whether the time stored here is valid
	 */
	private boolean mIsTimeValid = false;
	
	private long mUTCTimeLongFromServer;
	
	private long mUTCTimeLongFromSystem;
	
	/**
	 * set whether the time stored in local is valid
	 * @param isTimeValid	whether the time stored in local is valid
	 */
	synchronized void setIsTimeValid(boolean isTimeValid){
		this.mIsTimeValid = isTimeValid;
	}
	
	public static UITimeManager getInstance(){
		return instance;
	}
	
	public synchronized long getUTCTimeLong() {
		if (mIsTimeValid) {
			return System.currentTimeMillis() - mUTCTimeLongFromSystem
					+ mUTCTimeLongFromServer;
		} else {
			return Long.MIN_VALUE;
		}
	}
	
	public synchronized void setServerLocalTimeLong(long utcTimeLongFromServer){
		this.mUTCTimeLongFromSystem = System.currentTimeMillis();
		this.mUTCTimeLongFromServer = utcTimeLongFromServer;
		this.mIsTimeValid = true;
	}
}
