package com.insulinMeter.Patient;

public class InsulinDataModel {
	private long timestamp;
	private double sugarLevel;
	private double InsulinInjected;
	private double GLucoseINjected;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getInsulinInjected() {
		return InsulinInjected;
	}

	public void setInsulinInjected(double insulinInjected) {
		InsulinInjected = insulinInjected;
	}

	public double getGLucoseINjected() {
		return GLucoseINjected;
	}

	public void setGLucoseINjected(double gLucoseINjected) {
		GLucoseINjected = gLucoseINjected;
	}

	public double getSugarLevel() {
		return sugarLevel;
	}

	public void setSugarLevel(double sugarLevel) {
		this.sugarLevel = sugarLevel;
	}
}
