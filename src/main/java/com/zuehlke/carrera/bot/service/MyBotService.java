package com.zuehlke.carrera.bot.service;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorSystem;

import com.zuehlke.carrera.bot.model.Logic;
import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.model.Track;
import com.zuehlke.carrera.bot.model.TrackSection;
import com.zuehlke.carrera.bot.util.SensorEventBuffer;

/**
 * Contains the primary Bot AI. Created by paba on 10/5/14.
 */
@Service
public class MyBotService {

  private static final Logger logger = LoggerFactory.getLogger(MyBotService.class);

  private Logic logic = new Logic();
  
  private List<TrackSection> refSections;
  private TrackSection refSection;
  private Track currTrack;
  private TrackSection currSection;
  private int currSectionIndex;

  private SensorEventBuffer sensorEventBuffer = new SensorEventBuffer(15);
  private List<Float> cleanYAccs;

  private int currentRound; // For a simplified solution
  private double currentPower;
  private long lastTimeStamp;
  private long startTime;

  private final int SAMPLE_SIZE = 15;
  private final float MAX_Y_ACCELERATION = 20;
  private final float MAX_Y_ACC_CHANGE = 30;
  private final float SENSITIVIY = 10;
  private final int REACTION_TIME = 10;

  private static final String baseUrl = "http://relay2.beta.swisscloud.io";
  private static final String teamId = "cloudracers"; // TODO Put here your team id
  private static final String accessCode = "1337toor"; // TODO Put here your team access code

  private final Client client;
  private final WebTarget relayRestApi;

  /**
   * Creates a new MyBotService
   */
  @Autowired
  public MyBotService(ActorSystem actorSystem) {
    client = ClientBuilder.newClient();
    relayRestApi = client.target(baseUrl).path("/ws/rest");
  }


  /**
   * Occurs when a race starts.
   */
  public void start() {
    logger.info("Start Bot Service.");
    currentRound = 0;
    currSectionIndex = 0;
    lastTimeStamp = 0;
    currentPower = 120;
  }

  /**
   * Occurs when the bot receives sensor data from the car or the race-track.
   * 
   * @param data
   */
  public double handleSensorEvent(SensorEvent data) {
    logger.info("Received sensor data={}", data);

    switch (data.getType()) {
      case CAR_SENSOR_DATA:
        if (currTrack == null) { // Round 0
          return currentPower;
        }
        if (data.getTimeStamp() > lastTimeStamp) {
          lastTimeStamp = data.getTimeStamp();
          if (currSection != null) { // Round 1
            currSection.getEvents().add(data);
          }
          sensorEventBuffer.push(data);
          if (sensorEventBuffer.size() >= SAMPLE_SIZE) {
            cleanYAccs.add(sensorEventBuffer.getMedianYAcc());
            // wait for right, left, left then create new section
            if (logic.method(cleanYAccs, currSectionIndex)) {
              currSectionIndex = (currSectionIndex + 1) % 4;
              currSection = currTrack.nextUnknownSection();
              if (currSectionIndex == 0) {
                refSections = currTrack.getSections();
                currTrack = new Track();
                currSection = currTrack.nextUnknownSection();
                currSectionIndex = (currSectionIndex + 1) % 4;
              }
              if (refSections != null) { // Round 2
                refSection = refSections.get(currSectionIndex);
                if (refSection.getEvents().size() - currSection.getEvents().size() < (REACTION_TIME * currSectionIndex)) {
                  return 120;
                }
              }
              if (currSectionIndex == 1) {
                return 180;
              } else {
                return 250;
              }
            }
          }
        }
        // lastTimeStamp = data.getTimeStamp();
        // float accY = data.getAcc()[1];
        // float prevAccY = currentTrackSection.getLastAccY();
        // float accChange = prevAccY - accY;
        // if (Math.abs(prevAccY) < SENSITIVIY) {
        // // coming from a straight
        // if (accChange > MAX_Y_ACC_CHANGE) {
        // // going into a right turn
        // currentTrackSection = currentTrack.nextRightSection();
        // referenceTrackSection =
        // referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
        // } else if (accChange < -MAX_Y_ACC_CHANGE) {
        // // going into a left turn
        // currentTrackSection = currentTrack.nextLeftSection();
        // referenceTrackSection =
        // referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
        // }
        // } else {
        // // coming from a turn
        // if (Math.abs(accY) < SENSITIVIY) {
        // // going into a straight
        // currentTrackSection = currentTrack.nextStraightSection();
        // referenceTrackSection =
        // referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
        // } else {
        // if (prevAccY > SENSITIVIY) {
        // // coming from a right turn
        // if (accChange < -2 * MAX_Y_ACC_CHANGE) {
        // // going into a left turn
        // currentTrackSection = currentTrack.nextLeftSection();
        // referenceTrackSection =
        // referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
        // }
        // } else if (prevAccY < -SENSITIVIY) {
        // // coming from a left turn
        // if (accChange > 2 * MAX_Y_ACC_CHANGE) {
        // // going into a right turn
        // currentTrackSection = currentTrack.nextRightSection();
        // referenceTrackSection =
        // referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
        // }
        // }
        //
        // }
        // }
        //
        // if (referenceTrackSection.getEvents().size() - currentTrackSection.getEvents().size() <
        // REACTION_TIME) {
        // if (SectionType.STRAIGHT.equals(referenceTrackSection.getType())) {
        // sendSpeedControl(200);
        // } else {
        // sendSpeedControl(100);
        // }
        // }
        // currentTrackSection.getEvents().add(data);
        //
        // Sensor data from the mounted car sensor
        // Simple, synchronous Bot implementation

        // break;
      case ROUND_PASSED:
        logger.info("Round {} passed={}", currentRound, data);
        currentRound++;
        // A round has been passed - generated event from the light barrier
        if (currTrack != null) {
          logger.info("Round time={}", System.currentTimeMillis() - startTime);
          startTime = System.currentTimeMillis();
        } else {
          currTrack = new Track();
          currSectionIndex = 2;
        }
        return currentPower;
      default:
        logger.error("Received invalid data={}", data);
        return 100;
    }
  }
}
