package com.zuehlke.carrera.bot.strategy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.model.Track;
import com.zuehlke.carrera.bot.model.TrackSection;
import com.zuehlke.carrera.bot.util.SensorEventBuffer;

public class DynamicPatternRecognitionStrategy implements BotStrategy {

  private static final Logger logger = LoggerFactory.getLogger(DynamicPatternRecognitionStrategy.class);

  private List<TrackSection> refSections;
  private TrackSection refSection;
  private Track currTrack;
  private TrackSection currSection;

  private float minYAcc = 0;
  private float maxYAcc = 0;
  
  private SensorEventBuffer sensorEventBuffer = new SensorEventBuffer(15);
  private List<Float> cleanYAccs;

  private int currentRound; // For a simplified solution
  private double currentPower;
  private long lastTimeStamp;
  private long startTime;

  private final int SAMPLE_SIZE = 8;
  private final float MAX_Y_ACC_CHANGE = 0.1F;
  private final float SENSITIVIY = 0.1F;
  private final int REACTION_TIME = 10;

  public DynamicPatternRecognitionStrategy() {
    this.sensorEventBuffer = new SensorEventBuffer(8);
    this.currentPower = 130;
  }

  private float normalize(float value) {
    float normalizedValue = (value - minYAcc) / (maxYAcc - minYAcc);
    logger.info("Value={} normalized to={}", value, normalizedValue);
    return normalizedValue;
  }
  
  @Override
  public double processSensorEvent(SensorEvent data) {
    switch (data.getType()) {
      case CAR_SENSOR_DATA:
        float accY = data.getAcc()[1];
        minYAcc = minYAcc > accY ? accY : minYAcc;
        maxYAcc = maxYAcc < accY ? accY : maxYAcc;
        if (currTrack == null || currSection == null) {
          return currentPower;
        }
        if (data.getTimeStamp() > lastTimeStamp) {
          lastTimeStamp = data.getTimeStamp();
        }
        currSection.getEvents().add(data);
        sensorEventBuffer.push(data);
        if (sensorEventBuffer.size() >= SAMPLE_SIZE) {
          logger.info("sensorEventBuffer.size()={}", sensorEventBuffer.size());
          float normMedianYAcc = normalize(sensorEventBuffer.getMedianYAcc());
          cleanYAccs.add(normMedianYAcc);
          if (cleanYAccs.size() > 2) {
            float prevNormMedianYAcc = cleanYAccs.get(cleanYAccs.size() - 1);
            float yAccChange = prevNormMedianYAcc - normMedianYAcc;
            if (Math.abs(prevNormMedianYAcc) > MAX_Y_ACC_CHANGE && prevNormMedianYAcc < SENSITIVIY) {
              
            }
          }
          return currentPower;
        }
        
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
        // A round has been passed - generated event from the light barrier
        if (currTrack != null) {
          logger.info("Round time={}", System.currentTimeMillis() - startTime);
          startTime = System.currentTimeMillis();
          refSections = currTrack.getSections();
        }
        currTrack = new Track();
        currSection = currTrack.nextStraightSection();
        return currentPower;
      default:
        logger.error("Received invalid data={}", data);
        return currentPower;
    }
  }
}
