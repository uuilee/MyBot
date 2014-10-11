package com.zuehlke.carrera.bot.model;

import java.util.ArrayList;
import java.util.List;

public class TrackSection {
  
  private List<SensorEvent> events;
  private SectionType type;
  private double power;
  
  public TrackSection(SectionType type, double power) {
    this.events = new ArrayList<SensorEvent>();
    this.power = power;
    this.type = type;
  }
  
  public List<SensorEvent> getEvents() {
    return events;
  }
  
  public SectionType getType() {
    return type;
  }

  public void setType(SectionType type) {
    this.type = type;
  }

  public double getPower() {
    return power;
  }

  public void setPower(double power) {
    this.power = power;
  }

  public float getLastAccY() {
    return events.get(events.size() - 1).getAcc()[1];
  }
}
