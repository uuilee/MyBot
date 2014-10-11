package com.zuehlke.carrera.bot.strategy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.bot.model.Logic;
import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.model.Track;
import com.zuehlke.carrera.bot.model.TrackSection;
import com.zuehlke.carrera.bot.service.MyBotService;
import com.zuehlke.carrera.bot.util.SensorEventBuffer;

public class FixedPatternRecognitionStrategy implements BotStrategy {

  private static final Logger logger = LoggerFactory.getLogger(FixedPatternRecognitionStrategy.class);

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
  private final int REACTION_TIME = 10;

  public FixedPatternRecognitionStrategy(Track currTrack, TrackSection currSection,
      List<TrackSection> refSections) {
    this.currTrack = currTrack;
    this.currSection = currSection;
    this.refSections = refSections;
    currentRound = 0;
    currSectionIndex = 0;
    lastTimeStamp = 0;
    currentPower = 120;
  }

  @Override
  public double processSensorEvent(SensorEvent data) {
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
