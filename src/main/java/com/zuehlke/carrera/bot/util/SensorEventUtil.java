package com.zuehlke.carrera.bot.util;

import java.util.List;

import com.zuehlke.carrera.bot.model.SensorEvent;

public class SensorEventUtil {

  
  
  public static float getDistance(List<SensorEvent> a, List<SensorEvent> b) {
    float distanceAccY = 0;
    for (int i = 0; i < a.size(); i++) {
      distanceAccY += Math.abs(a.get(i).getAcc()[1] - b.get(i).getAcc()[1]);
  
    }
    float distanceGyrX = 0;
    for (int i = 0; i < a.size(); i++) {
      distanceGyrX += Math.abs(a.get(i).getGyr()[0] - b.get(i).getGyr()[0]);
    } 
    return distanceAccY + distanceGyrX;
  }
  
}
