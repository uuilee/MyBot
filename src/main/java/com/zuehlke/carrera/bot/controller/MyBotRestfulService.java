package com.zuehlke.carrera.bot.controller;

import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.service.MyBotService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST API of the Bot Created by P. Buettiker on 10/5/14.
 */
@Controller
@RequestMapping(value = "myBot", produces = "application/json")
public class MyBotRestfulService {

  private static final Logger logger = LoggerFactory.getLogger(MyBotRestfulService.class);

  private final MyBotService myBotService;

  @Autowired
  public MyBotRestfulService(MyBotService myBotService) {
    this.myBotService = myBotService;
  }

  @RequestMapping(value = "ping", method = RequestMethod.GET, produces = "text/plain")
  @ResponseBody
  public String getPing() {
    logger.info("getPing() called.");
    return "success";
  }

  @RequestMapping(value = "start", method = RequestMethod.POST)
  @ResponseBody
  public void start() {
    logger.info("start() called.");
    myBotService.start();
  }

  @RequestMapping(value = "sensor", method = RequestMethod.POST, consumes = "application/json")
  @ResponseBody
  public String handleSensorEvent(@RequestBody SensorEvent data) {
    logger.info("handleSensorEvent() called.");
    return String.valueOf(myBotService.handleSensorEvent(data));
  }

}
