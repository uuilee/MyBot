package com.zuehlke.carrera.bot.strategy;

import java.util.List;

import com.zuehlke.carrera.bot.model.Track;
import com.zuehlke.carrera.bot.model.TrackSection;

public class StandardDeviationStrategy {

  private List<TrackSection> refSections;
  private TrackSection refSection;
  private int refSectionNum;
  private Track currTrack;
  private TrackSection currSection;
  
  private long lastTimeStamp;
  
  public StandardDeviationStrategy(Track currTrack, TrackSection currSection, List<TrackSection> refSections) {
    this.currTrack = currTrack;
    this.currSection = currSection;
    this.refSections = refSections;
    
  }
  
}
