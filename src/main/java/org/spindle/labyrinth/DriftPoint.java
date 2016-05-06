package org.spindle.labyrinth;

public class DriftPoint {
    
    public Drift drift;

    public float vectorFactor;

    public DriftPoint(Drift drift, float vectorFactor) {
	this.drift = drift;
	this.vectorFactor = vectorFactor;
    }
}
