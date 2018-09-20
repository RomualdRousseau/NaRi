package com.github.romualdrousseau.nari;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

import org.json.simple.*;
import org.json.simple.parser.*;

public class GeoLocation {

	public GeoLocation(String apiKey) {
		m_apiKey = apiKey;
        m_userRegion = "us";
		m_culture = "en";
        m_cache =  GeoCache.getInstance();
        m_proxy = null;
	}

	public GeoLocation(String apiKey, String userRegion, String culture) {
		m_apiKey = apiKey;
        m_userRegion = userRegion;
		m_culture = culture;
        m_cache =  GeoCache.getInstance();
        m_proxy = null;
	}

    public void setApiUrl(String url) {
        API_URL = url;
    }

    public void clearCache() {
        m_cache.clear();
    }

    public void useCache(String cacheFilePath) {
        m_cache.setPersistent(cacheFilePath);
    }

    public void dontUseCache() {
        m_cache.setPersistent(null);
    }

    public void useProxy(String hostname, int port) {
        m_proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
    }

    public void dontUseProxy() {
        m_proxy = null;
    }

    public GeoData query(String query) throws IOException, ParseException, InterruptedException, GeoException {
        return this.query(query, null);
    }

    public GeoData query(String query, GeoFilter filter) throws IOException, ParseException, InterruptedException, GeoException {
        GeoData data = null;

        if(query == null || query.equals("")) {
            return GeoData.Null;
        }

        data = m_cache.get(query);
        if(data != null && data != GeoData.Null) {
            return data;
        }

        URL url = new URL(API_URL + "?address=" + URLEncoder.encode(query, "UTF-8") + "&language=" + m_culture + "&region=" + m_userRegion + "&key=" + m_apiKey);

        for(int i = 0; data == null && i < 3; i++) {
            data = queryOnce(url, filter);
        } 

        if(data != null && data != GeoData.Null) {
            m_cache.put(query, data);
            return data;
        }
        else {
            return GeoData.Null;
        }
    }

    private GeoData queryOnce(URL url, GeoFilter filter) throws ParseException, InterruptedException, GeoException {
        try {
            URLConnection conn = (m_proxy == null) ? url.openConnection() : url.openConnection(m_proxy);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(reader);

            String status = (String) response.get("status");
            if(status.equals("OK")) {
                return parseResults((JSONArray) response.get("results"), filter);
            }
            else if(status.equals("ZERO_RESULTS")) {
                return GeoData.Null;
            }
            else if(status.equals("OVER_QUERY_LIMIT")) {
                return delayAndReturn(null);
            }
            else {
                throw new GeoException(status);
            }     
        }
        catch(java.net.SocketException x) {
            return GeoData.Null;   
        }
        catch(java.net.UnknownHostException x) {
            return delayAndReturn(null);
        }
        catch(java.io.IOException x) {
            System.out.println(x);
            return delayAndReturn(null);
        }
    }

    private GeoData parseResults(JSONArray results, GeoFilter filter) {
        GeoData dataToReturn = GeoData.Null;

        for(int i = 0; (dataToReturn == GeoData.Null) && (i < results.size()); i++) {
            JSONObject result = (JSONObject) results.get(i);
            GeoData data = parseOneResult(result);
            if(filter == null || filter.match(data)) {
                dataToReturn = data;
            }
        }

        return dataToReturn; 
    }

    private GeoData parseOneResult(JSONObject result) {
        GeoData data = new GeoData();

        // get location types
        JSONArray types = (JSONArray) result.get("types");
        data.types = types.toString();

        // get location formatted address
        data.formattedAddress = (String) result.get("formatted_address");

        // get location address components and do some locality adjustements
        String sublocality = null;
        JSONArray addressComponents = (JSONArray) result.get("address_components");
        for(int j = 0; j < addressComponents.size(); j++) {
            JSONObject component = (JSONObject) addressComponents.get(j);
            JSONArray subtypes = (JSONArray) component.get("types");    
            if(subtypes.contains("country")) {
                data.countryRegion = (String) component.get("long_name");
            }
            else if(subtypes.contains("sublocality_level_1")) {
                sublocality = (String) component.get("long_name");
            }
            else if(subtypes.contains("locality")) {
                data.locality = (String) component.get("long_name");
            }
            else if(subtypes.contains("administrative_area_level_1")) {
                data.adminDistrict = (String) component.get("long_name");
            }
            else if(subtypes.contains("postal_code")) {
                data.postalCode = (String) component.get("long_name");
            }
        }
        if(data.locality == null) {
            data.locality = sublocality;
        }
        if(data.adminDistrict == null) {
            data.adminDistrict = data.locality;
            data.locality = sublocality;
        }

        // get location geopoint
        JSONObject geometry = (JSONObject) result.get("geometry");
        JSONObject location = (JSONObject) geometry.get("location");
        data.point = new GeoPoint((double) location.get("lat"), (double) location.get("lng"));

        return data;
    }

    private GeoData delayAndReturn(GeoData d) throws InterruptedException {
        Thread.sleep((RANDOM.nextInt(9) + 1) * 100);
        return d;
    }

    private String m_apiKey; 
    private String m_userRegion;
    private String m_culture;
    private GeoCache m_cache;
    private Proxy m_proxy;

    private static String API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static Random RANDOM = new Random();
}
