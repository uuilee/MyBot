package com.zuehlke.carrera.bot.actors;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

import com.zuehlke.carrera.bot.model.SensorEvent;

public class PersisterActor extends UntypedActor {

  private static final Logger logger = LoggerFactory.getLogger(PersisterActor.class);
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof SensorEvent) {
      String log = "Persist SensorEvent=" + msg;
      logger.info(log);
    } else if (msg instanceof List) {
      // TODO store list to DB
    }
    
  }

}
