package com.zuehlke.carrera.bot.model;

public class handleJsonData {

	public static void main(String[] args) {

		getJsonData getter = new getJsonData();
		SensorEvent[] events = getJsonData.getJsonData("/home/benjamin/workspace/carrera.mybot/sample-data/equal.velocity/73165.json");
		
		//System.out.println("lala");
	}

}
