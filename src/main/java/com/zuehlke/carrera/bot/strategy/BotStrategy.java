package com.zuehlke.carrera.bot.strategy;

import com.zuehlke.carrera.bot.model.SensorEvent;

public interface BotStrategy {

  double processSensorEvent(SensorEvent data);
  
}
