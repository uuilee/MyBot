package com.zuehlke.carrera.bot.strategy;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.bot.model.SensorEvent;



public class IncreasingConstants implements BotStrategy {

	  private static final Logger logger = LoggerFactory.getLogger(DynamicPatternRecognitionStrategy.class);

	  private int count;
	  private int countLastRound;
	  private int currentRound; 
	  private double currentPower;

	  public IncreasingConstants() {
	    this.count = 0;
	    this.countLastRound = 0;
	    this.currentPower = 130;
	    this.currentRound = 0; 

	  }
	  
	  @Override
	  public double processSensorEvent(SensorEvent data) {
	    switch (data.getType()) {
	      case CAR_SENSOR_DATA:        
	        count++;
	        return currentPower+currentRound*5;
	                
	      case ROUND_PASSED:
	        countLastRound = count;
	        count = 0;
	        currentRound++;

	        return currentPower;
	      default:
	        return currentPower;
	    }
	  }
	}
