package com.zuehlke.carrera.bot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.zuehlke.carrera.bot.model.SensorEvent;

public class SensorEventBuffer extends LinkedList<SensorEvent> {

  private static final long serialVersionUID = 1L;
  
  private int capacity;

  public SensorEventBuffer(int capacity) {
    this.capacity = capacity;
  }

  @Override
  public boolean offerFirst(SensorEvent e) {
    if (size() >= capacity) {
      removeLast();
    }
    super.offerFirst(e);
    return true;
  }
  
  public Float getMedianYAcc(float normValue) {
    // TODO: Find a nicer way to do this
    ArrayList<SensorEvent> sortedList = new ArrayList<>(this);
    Collections.sort(sortedList, new Comparator<SensorEvent>() {
      @Override
      public int compare(SensorEvent o1, SensorEvent o2) {
        return (int) Math.ceil(o1.getAcc()[1] - o2.getAcc()[1]);
      }
    });
    return sortedList.get((int)Math.floor(capacity / 2)).getAcc()[1]/normValue;
  }
  
  public int getCapacity() {
    return capacity;
  }
  
}
