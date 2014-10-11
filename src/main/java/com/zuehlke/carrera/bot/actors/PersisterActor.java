package com.zuehlke.carrera.bot.actors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.service.MyBotService;

public class PersisterActor extends UntypedActor {

  private static final Logger logger = LoggerFactory.getLogger(PersisterActor.class);
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof SensorEvent) {
      String log = "Persist SensorEvent=" + msg;
      MyBotService.currentDebugLog = log;
      logger.info(log);
    }
    
  }

}
