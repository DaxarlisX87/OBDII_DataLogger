package com.example.datalogger_android;

public class DrivingEvent {
	
	private double time, latitude, longitude;
	String type;
	
	public DrivingEvent() {
		time = 0;
		latitude = 0;
		longitude = 0;
	}
	public DrivingEvent(double time, double latitude, double longitude, String type) {
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.type = type;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String toString() {
		return type + ", " + time + ", " + latitude + ", " + longitude; 
	}
	

}
