package com.zuehlke.carrera.bot.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorSystem;

import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.strategy.BotStrategy;
import com.zuehlke.carrera.bot.strategy.DynamicPatternRecognitionStrategy;
import com.zuehlke.carrera.bot.strategy.IncreasingConstants;


@Service
public class MyBotService {

  private static final Logger logger = LoggerFactory.getLogger(MyBotService.class);

  private static final String baseUrl = "http://relay2.beta.swisscloud.io";
  private static final String teamId = "cloudracers"; // TODO Put here your team id
  private static final String accessCode = "1337toor"; // TODO Put here your team access code

  private final Client client;
  private final WebTarget relayRestApi;

  BotStrategy strategy;
  
  @Autowired
  public MyBotService(ActorSystem actorSystem) {
    client = ClientBuilder.newClient();
    relayRestApi = client.target(baseUrl).path("/ws/rest");
    strategy = new IncreasingConstants();
  }

  /**
   * Occurs when a race starts.
   */
  public void start() {
    logger.info("Start Bot Service.");
  }

  /**
   * Occurs when the bot receives sensor data from the car or the race-track.
   * 
   * @param data
   */
  public double handleSensorEvent(SensorEvent data) {
    logger.info("Received sensor data={}", data);
    return strategy.processSensorEvent(data);

  }
}
