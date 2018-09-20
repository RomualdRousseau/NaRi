package com.github.romualdrousseau.nari;

import java.util.Date;
import java.io.Serializable;

public class GeoData implements Serializable
{
	public static final GeoData Null =  new GeoData();

	public String types;

	public String formattedAddress;
	
	public String postalCode;
	public String countryRegion;
	public String adminDistrict;
	public String locality;
	
	public GeoPoint point = new GeoPoint(0.0, 0.0);

	public Date timestamp = new java.util.Date();
}
