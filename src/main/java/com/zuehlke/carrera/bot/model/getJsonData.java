package com.zuehlke.carrera.bot.model;

import org.json.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;


public class getJsonData {

	public static SensorEvent[] getJsonData(String filePath) {
		
		SensorEvent[] events = null;
		
		try {		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		JSONTokener to = new JSONTokener(br);
		JSONObject root = new JSONObject(to);
		

	 
	 
			JSONArray speedControls = (JSONArray) root.get("speedControls");
			JSONArray sensorEvents = (JSONArray) root.get("sensorEvents");
	 
			long[] timestamps = new long[sensorEvents.length()];
			float[][] acc = new float[sensorEvents.length()][3];
			float[][] gyr = new float[sensorEvents.length()][3];
			events = new SensorEvent[sensorEvents.length()];

			
			// handle round events!
			for(int i = 0;i < sensorEvents.length();i++){
			    String type = sensorEvents.getJSONObject(i).getString("type");
			    
			    //System.out.println(type);
			    
			    if(type.equals("CAR_SENSOR_DATA")){
			    	timestamps[i] = sensorEvents.getJSONObject(i).getLong("timeStamp");
			    	JSONArray accData = sensorEvents.getJSONObject(i).getJSONArray("acc");
			    	acc[i][0] = (float)accData.getDouble(0);
			    	acc[i][1] = (float)accData.getDouble(1);
			    	acc[i][2] = (float)accData.getDouble(2);

			    	
			    	JSONArray gyrData = sensorEvents.getJSONObject(i).getJSONArray("gyr");
			    	gyr[i][0] = (float)gyrData.getDouble(0);
			    	gyr[i][1] = (float)gyrData.getDouble(1);
			    	gyr[i][2] = (float)gyrData.getDouble(2);
			    	
			        events[i] = new SensorEvent(acc[i],gyr[i],gyr[i], timestamps[i]);

			    }
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return events; 
	 

	    }

}
