package com.zuehlke.carrera.bot.service;

import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.zuehlke.carrera.bot.actors.DispatcherActor;
import com.zuehlke.carrera.bot.actors.PersisterActor;
import com.zuehlke.carrera.bot.model.SectionType;
import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.model.SpeedControl;
import com.zuehlke.carrera.bot.model.Track;
import com.zuehlke.carrera.bot.model.TrackSection;

/**
 * Contains the primary Bot AI. Created by paba on 10/5/14.
 */
@Service
public class MyBotService {

  public static String currentDebugLog;

  private ActorSystem actorSystem;
  private ActorRef mainActor;
  private ActorRef persisterActor;

  private List<TrackSection> referenceTracks;
  private TrackSection referenceTrackSection;
  private int referenceSectionNum;
  private Track currentTrack;
  private TrackSection currentTrackSection;

  private long lastTimeStamp;

  private static final String baseUrl = "http://relay2.beta.swisscloud.io";
  private static final String teamId = "cloudracers"; // TODO Put here your team id
  private static final String accessCode = "1337toor"; // TODO Put here your team access code

  private final Client client;
  private final WebTarget relayRestApi;

  private final float MAX_Y_ACCELERATION = 40;
  private final float MAX_Y_ACC_CHANGE = 30;
  private final float SENSITIVIY = 10;
  private final int REACTION_TIME = 2;


  /**
   * Creates a new MyBotService
   */
  @Autowired
  public MyBotService(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
    mainActor = actorSystem.actorOf(Props.create(DispatcherActor.class), "dispatcherActor");
    persisterActor = actorSystem.actorOf(Props.create(PersisterActor.class), "persisterActor");

    client = ClientBuilder.newClient();
    relayRestApi = client.target(baseUrl).path("/ws/rest");
  }


  /**
   * Occurs when a race starts.
   */
  public void start() {
    // TODO load reference track from database
    referenceTracks = (new Track()).getSections();
    referenceSectionNum = 0;
    referenceTrackSection = referenceTracks.get(referenceSectionNum);
    currentTrack = new Track();
    currentTrackSection = currentTrack.nextUnknownSection();

    // TODO Maybe send initial velocity here...
    lastTimeStamp = 0;
    // TODO Fetch last events from db
    sendSpeedControl(100);
  }

  /**
   * Occurs when the bot receives sensor data from the car or the race-track.
   * 
   * @param data
   */
  public void handleSensorEvent(SensorEvent data) {

    switch (data.getType()) {
      case CAR_SENSOR_DATA:
        persisterActor.tell(data, null);
        if (data.getTimeStamp() > lastTimeStamp) {
          lastTimeStamp = data.getTimeStamp();
          float accY = data.getAcc()[1];
          float prevAccY = currentTrackSection.getLastAccY();
          float accChange = prevAccY - accY;
          if (Math.abs(prevAccY) < SENSITIVIY) {
            // coming from a straight
            if (accChange > MAX_Y_ACC_CHANGE) {
              // going into a right turn
              currentTrackSection = currentTrack.nextRightSection();
              referenceTrackSection =
                  referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
            } else if (accChange < -MAX_Y_ACC_CHANGE) {
              // going into a left turn
              currentTrackSection = currentTrack.nextLeftSection();
              referenceTrackSection =
                  referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
            }
          } else {
            // coming from a turn
            if (Math.abs(accY) < SENSITIVIY) {
              // going into a straight
              currentTrackSection = currentTrack.nextStraightSection();
              referenceTrackSection =
                  referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
            } else {
              if (prevAccY > SENSITIVIY) {
                // coming from a right turn
                if (accChange < -2 * MAX_Y_ACC_CHANGE) {
                  // going into a left turn
                  currentTrackSection = currentTrack.nextLeftSection();
                  referenceTrackSection =
                      referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
                }
              } else if (prevAccY < -SENSITIVIY) {
                // coming from a left turn
                if (accChange > 2 * MAX_Y_ACC_CHANGE) {
                  // going into a right turn
                  currentTrackSection = currentTrack.nextRightSection();
                  referenceTrackSection =
                      referenceTracks.get((referenceSectionNum + 1) % referenceTracks.size());
                }
              }

            }
          }

          if (referenceTrackSection.getEvents().size() - currentTrackSection.getEvents().size() < REACTION_TIME) {
            if (SectionType.STRAIGHT.equals(referenceTrackSection.getType())) {
              sendSpeedControl(200);
            } else {
              sendSpeedControl(100);
            }
          }
          currentTrackSection.getEvents().add(data);

          // Sensor data from the mounted car sensor
          // Simple, synchronous Bot implementation
          // if (data.getAcc()[1] > MAX_Y_ACCELERATION) {
          // sendSpeedControl(45);
          // } else {
          // sendSpeedControl(85);
          // }
        }

        break;
      case ROUND_PASSED:
        // A round has been passed - generated event from the light barrier
        // TODO Handle round passed event...
        referenceTracks = currentTrack.getSections();
        currentTrack = new Track();

        break;
    }
  }

  /**
   * Sends the given power to the race-track using the rest API
   * 
   * @param power Power value in the range of [0 - 250]
   */
  public void sendSpeedControl(double power) {
    SpeedControl control = new SpeedControl(power, teamId, accessCode, new Date().getTime());
    try {
      Response response =
          relayRestApi.path("relay/speed").request()
              .post(Entity.entity(control, MediaType.APPLICATION_JSON));
    } catch (Exception e) {
      e.printStackTrace(); // TODO better error handling
    }
  }



}
