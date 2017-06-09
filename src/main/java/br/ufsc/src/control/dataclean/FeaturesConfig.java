package br.ufsc.src.control.dataclean;

import java.util.ArrayList;

public class FeaturesConfig {
	
	public static boolean hasDistance = false;
	public static boolean hasSpeed = false;;
	public static boolean hasDuration = false;;
	
	public static boolean argKM = false;
	public static boolean argMeters = false;
	
	public static boolean argKMPerHour = false;
	public static boolean argMetersPerSecond = false;
	
	public static boolean argSeconds = false;
	public static boolean argMinutes = false;
	public static boolean argHours = false;
	
	private int numThreads = 1;
	private boolean writeOriginal = false;
	
	public FeaturesConfig() {}
	
	public static void clear() {
		hasDistance = false;
		hasSpeed = false;;
		hasDuration = false;;
		
		argKM = false;
		argMeters = false;
		
		argKMPerHour = false;
		argMetersPerSecond = false;
		
		argSeconds = false;
		argMinutes = false;
		argHours = false;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		if(numThreads < 1) return;
		this.numThreads = numThreads;
	}

	public boolean isWriteOriginal() {
		return writeOriginal;
	}

	public void setWriteOriginal(boolean writeOriginal) {
		this.writeOriginal = writeOriginal;
	}
}
