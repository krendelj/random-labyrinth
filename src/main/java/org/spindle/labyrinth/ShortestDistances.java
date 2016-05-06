package org.spindle.labyrinth;

import java.util.Map;
import java.util.HashMap;

public class ShortestDistances {

    public HashMap<Cross, Float> distances;

    public void calculate(Labyrinth labyrinth) 
	throws InvalidOperationException {
	distances = new HashMap<Cross, Float>();
	HashMap<Cross, Float> currentDists = new HashMap<Cross, Float>();

	updateCurrentDists(labyrinth.drifts.get(0), 0f, 0f, currentDists);
	do {
	    Cross currentCross = null;
	    float currentDist = -1f;
	    boolean first = true;
	    for (Map.Entry<Cross, Float> entry : currentDists.entrySet()) {
		if (first || entry.getValue() < currentDist) {
		    currentCross = entry.getKey();
		    currentDist = entry.getValue();
		    first = false;
		}
	    }
	    distances.put(currentCross, currentDist);
	    currentDists.remove(currentCross);
	    updateCurrentDists(currentCross.drift1, 
			       currentCross.vectorFactor1,
			       currentDist,
			       currentDists);
	    updateCurrentDists(currentCross.drift2, 
			       currentCross.vectorFactor2,
			       currentDist,
			       currentDists);
	}
	while (currentDists.size() > 0);
    }

    public DriftPoint getFarthestPoint(Labyrinth labyrinth)
	throws InvalidOperationException {
	boolean firstd = true;
	float distance = -1f;
	Drift drift = null;
	for (Drift d : labyrinth.drifts) {
	    Cross fcross = null;
	    boolean firstc = true;
	    for (Cross c : d.crosses) {
		if (firstc || c.getVectorFactor(d) 
		    > fcross.getVectorFactor(d)) {
		    fcross = c;
		    firstc = false;
		}
	    }
	    float ddistance = (d.vectorFactor - fcross.getVectorFactor(d))
		* d.vector.getLength()
		+ distances.get(fcross);
	    if (firstd || ddistance > distance) {
	        distance = ddistance;
		drift = d;
		firstd = false;
	    }
	}
        return new DriftPoint(drift, drift.vectorFactor);
    }

    private void updateCurrentDists(Drift drift, float vectorFactor,
				    float currentDist,
				    HashMap<Cross, Float> currentDists)
	throws InvalidOperationException {
	for (Cross c: drift.crosses) {
	    if (distances.containsKey(c)) {
		continue;
	    }
	    if (Settings.floatsEqual(vectorFactor,
				       c.getVectorFactor(drift))) {
		currentDists.put(c, currentDist);
	    }
	    else {
		float dist = currentDist
		    + Math.abs(c.getVectorFactor(drift)
			       - vectorFactor)
		    * drift.vector.getLength();
		if (!currentDists.containsKey(c)
		    || currentDists.get(c) > dist) {
		    currentDists.put(c, dist);
		}
	    }
	}
    }
}
