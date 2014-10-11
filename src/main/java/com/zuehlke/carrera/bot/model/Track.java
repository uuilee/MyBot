package com.zuehlke.carrera.bot.model;

import java.util.ArrayList;
import java.util.List;

public class Track {

  private List<TrackSection> sections = new ArrayList<TrackSection>();
  private double initialPower = 100;

  public Track() {}
  
  public TrackSection nextUnknownSection() {
    TrackSection trackSection = new TrackSection(SectionType.TURN, initialPower);
    sections.add(trackSection);
    return trackSection;
  }
  
  public TrackSection nextUnknownSection() {
    TrackSection trackSection = new TrackSection(SectionType.UNKNOWN, initialPower);
    sections.add(trackSection);
    return trackSection;
  }
  
  public TrackSection nextStraightSection() {
    TrackSection trackSection = new TrackSection(SectionType.STRAIGHT, initialPower);
    sections.add(trackSection);
    return trackSection;
  }

  public TrackSection nextLeftSection() {
    TrackSection trackSection = new TrackSection(SectionType.LEFT, initialPower);
    sections.add(trackSection);
    return trackSection;
  }

  public TrackSection nextRightSection() {
    TrackSection trackSection = new TrackSection(SectionType.RIGHT, initialPower);
    sections.add(trackSection);
    return trackSection;
  }

  public List<TrackSection> getSections() {
    return sections;
  }

}
