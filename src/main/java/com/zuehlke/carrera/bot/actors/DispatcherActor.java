package com.zuehlke.carrera.bot.actors;

import akka.actor.UntypedActor;

public class DispatcherActor extends UntypedActor {

  private static String STOP_MGS = "stop";
  
  @Override
  public void onReceive(Object msg) throws Exception {
    
    if (STOP_MGS.equals(msg)) {
      getContext().stop(getSelf());  
    } else {
      unhandled(msg);
    }
    
  }

}
