package com.github.romualdrousseau.nari;

import java.io.Serializable;

public class GeoPoint implements Serializable
{
	public double latitude;
	public double longitude;
	
	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double distanceInMeter(GeoPoint other) {
		double R = 6371e3;
		double p1 = Math.toRadians(this.latitude);
		double p2 = Math.toRadians(other.latitude);
		double dp = Math.toRadians(other.latitude - this.latitude);
		double ds = Math.toRadians(other.longitude - this.longitude);
		double a = Math.pow(Math.sin(dp / 2), 2) + Math.cos(p1) * Math.cos(p2) * Math.pow(Math.sin(ds / 2), 2);
		double c =  2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}
}
