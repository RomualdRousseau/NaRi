package com.github.romualdrousseau.nari;

import java.util.List;
import java.util.function.Consumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URISyntaxException;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class AppTest
{
	static final String MY_KEY = "AIzaSyAl8rftE96nl5cGZmD06-XKoP7mpYiQfRI";
	static final boolean isInChina = false;

	public void printGeoData(GeoData d) {
		System.out.println("Types: " + d.types);
		System.out.println("Country: " + d.countryRegion);
		System.out.println("District: " + d.adminDistrict);
		System.out.println("Locality: " + d.locality);
		System.out.println("Postal Code: " + d.postalCode);
		System.out.println("Formatted Address: " + d.formattedAddress);
		System.out.println("Geo: (" + d.point.latitude + ", " + d.point.longitude + ")");
	}

	@Test
	public void testGeoPoint() {
		GeoPoint p1 =  new GeoPoint(37.8182428, 128.8592719); 
		GeoPoint p2 =  new GeoPoint(37.3520869, 127.1233369); 
		assertEquals(Math.round(p1.distanceInMeter(p2)), 161505);
	}

	@Test
	public void testGeoLocationNoCache() throws Exception {
		GeoLocation gl = new GeoLocation(MY_KEY, "kr", "ko");
		if(isInChina) {
			gl.setApiUrl("https://maps.google.cn/maps/api/geocode/json");
			gl.useProxy("172.20.241.25", 8080);
		}
		else {
			gl.setApiUrl("https://maps.google.com/maps/api/geocode/json");
		}

		GeoData d = gl.query("강원도 강릉시 경강로 2109");

		assertThat(d, is(not(GeoData.Null)));
		assertThat(d.types, is("[\"street_address\"]"));
		assertThat(d.countryRegion, is("대한민국"));
		assertThat(d.adminDistrict, is("강원도"));
		assertThat(d.locality, is("강릉시"));
		assertThat(d.postalCode, is("210-080"));
		assertThat(d.formattedAddress, is("대한민국 강원도 강릉시 임당동 경강로 2109"));
		assertThat(d.point.latitude, is(37.7556564));
		assertThat(d.point.longitude, is(128.8971525));
	}

	@Test
	public void testGeoLocationWithCache() throws Exception {
		GeoLocation gl = new GeoLocation(MY_KEY, "kr", "ko");
		if(isInChina) {
			gl.setApiUrl("https://maps.google.cn/maps/api/geocode/json");
			gl.useProxy("172.20.241.25", 8080);
		}
		else {
			gl.setApiUrl("https://maps.google.com/maps/api/geocode/json");
		}
		gl.useCache("src/test/resources/google_cache.dat");
		gl.clearCache();

		GeoData d = gl.query("강원도 강릉시 경강로 2109");

		assertThat(d, is(not(GeoData.Null)));
		assertThat(d.types, is("[\"street_address\"]"));
		assertThat(d.countryRegion, is("대한민국"));
		assertThat(d.adminDistrict, is("강원도"));
		assertThat(d.locality, is("강릉시"));
		assertThat(d.postalCode, is("210-080"));
		assertThat(d.formattedAddress, is("대한민국 강원도 강릉시 임당동 경강로 2109"));
		assertThat(d.point.latitude, is(37.7556564));
		assertThat(d.point.longitude, is(128.8971525));
	}
}
