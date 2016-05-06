package org.spindle.labyrinth;

import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class Walker {

    public static final float MOVEMENT_STEP_PIXELS = 5f;

    public static final int MOVEMENT_STEP_SECOND_FRACTION = 24;

    public static final float TARGET_HALF_WIDTH_PIXELS = 5f;

    public Drift currentDrift;

    public float currentFactor;

    public ArrayList<Drift> activeDrifts;

    public ArrayList<Drift> driftsRing;

    public Drift movementTargetDrift;
    
    public float movementTargetFactor;

    public Cross movementTransitionCross;

    public Walker() {
	activeDrifts = new ArrayList<Drift>();
    }

    public void init(Labyrinth labyrinth) 
	throws InvalidOperationException {
	driftsRing = (ArrayList<Drift>)labyrinth.drifts.clone();
	currentDrift = labyrinth.drifts.get(0);
	currentFactor = 0f;
	popCurrentDrift();
	findActiveDrifts();
    }

    public boolean processClick(Labyrinth labyrinth, MouseEvent e) 
	throws InvalidOperationException {
	DriftPosition dp = null;
	for (Drift d : driftsRing) {
	    if (activeDrifts.contains(d)) {
		dp = projectOnSegment(e.getX(), e.getY(), d);
		if (dp != null) break;
		dp = connectToDriftEnd(e.getX(), e.getY(), d);
		if (dp != null) break;
		dp = connectToDriftStart(e.getX(), e.getY(), d);
		if (dp != null) break;
	    }
	}
	if (dp == null) return false;
	initMovement(dp.drift, dp.vectorFactor);
	return true;
    }

    public boolean doMovement() 
	throws ShouldNotHappenException, InvalidOperationException {

	float pixelsLeft = MOVEMENT_STEP_PIXELS;
	if (currentDrift != movementTargetDrift) {
	    pixelsLeft = moveOnDrift(pixelsLeft, 
				     movementTransitionCross
				     .getVectorFactor(currentDrift));
	    if (pixelsLeft > 0f) {
		currentFactor = movementTransitionCross
		    .getVectorFactor(movementTransitionCross
				     .getOtherDrift(currentDrift));
		currentDrift = movementTargetDrift;
		popCurrentDrift();
	    }
	}
	if (pixelsLeft > 0f) {
	    pixelsLeft = moveOnDrift(pixelsLeft,
				     movementTargetFactor);
	}
	findActiveDrifts();
	return !Settings.floatsEqual(currentFactor, movementTargetFactor);
    }

    public void paintSelf(Graphics2D g2) {
    	float x = currentDrift.startX
	    + currentFactor * currentDrift.vector.x;
	float y = currentDrift.startY
	    + currentFactor * currentDrift.vector.y;
	g2.fill(new Ellipse2D.Float(x - Drift.halfWidth,
				    y - Drift.halfWidth,
				    2f * Drift.halfWidth,
				    2f * Drift.halfWidth));
    }

    public void paintTarget(Graphics2D g2, DriftPoint target) {
	float x = target.drift.startX
	    + target.drift.vectorFactor * target.drift.vector.x;
	float y = target.drift.startY
	    + target.drift.vectorFactor * target.drift.vector.y;
	g2.draw(new Line2D.Float(x - TARGET_HALF_WIDTH_PIXELS, 
				 y - TARGET_HALF_WIDTH_PIXELS, 
				 x + TARGET_HALF_WIDTH_PIXELS,
				 y + TARGET_HALF_WIDTH_PIXELS));
	g2.draw(new Line2D.Float(x - TARGET_HALF_WIDTH_PIXELS, 
				 y + TARGET_HALF_WIDTH_PIXELS, 
				 x + TARGET_HALF_WIDTH_PIXELS, 
				 y - TARGET_HALF_WIDTH_PIXELS));
    }

    private void popCurrentDrift() {
	driftsRing.remove(currentDrift);
	driftsRing.add(currentDrift);
    }

    private void findActiveDrifts()
	throws InvalidOperationException {
	activeDrifts.clear();
	activeDrifts.add(currentDrift);
	for (Cross c : currentDrift.crosses) {
	    float distance = Math.abs(c.getVectorFactor(currentDrift) 
				      - currentFactor)
		* currentDrift.vector.getLength();
	    if (distance < Drift.halfWidth
		|| Settings.floatsEqual(distance, Drift.halfWidth)) {
		activeDrifts.add(c.getOtherDrift(currentDrift));
	    }
	}
    }

    private DriftPosition projectOnSegment
	(float x, float y, Drift drift) {
	LineSegment perp = new LineSegment();
	perp.startX = x;
	perp.startY = y;
	perp.vector = drift.vector.rotateMinus90();
	LineSegment.LinesCross cr = perp.crossLines(drift);
	if (Math.abs(cr.vectorFactor * perp.vector.getLength()) 
	    > Drift.halfWidth
	    && !Settings.floatsEqual
	    (Math.abs(cr.vectorFactor * perp.vector.getLength()),
	     Drift.halfWidth)) {
	    return null;
	}
	cr = drift.crossLines(perp);
	if (!LineSegment.inSegment(cr.vectorFactor, drift.vectorFactor)) {
	    return null;
	}
	DriftPosition res = new DriftPosition();
	res.drift = drift;
	res.vectorFactor = cr.vectorFactor;
	return res;
    }

    private DriftPosition connectToDriftEnd
	(float x, float y, Drift drift) {
	Vector v = new Vector();
	v.x = x - (drift.startX + drift.vectorFactor * drift.vector.x);
	v.y = y - (drift.startY + drift.vectorFactor * drift.vector.y);
	if (v.getLength() > Drift.halfWidth
	    && !Settings.floatsEqual(v.getLength(), Drift.halfWidth)) {
	    return null;
	}
	DriftPosition res = new DriftPosition();
	res.drift = drift;
	res.vectorFactor = drift.vectorFactor;
	return res;
    }

    private DriftPosition connectToDriftStart
	(float x, float y, Drift drift) {
	Vector v = new Vector();
	v.x = x - drift.startX;
	v.y = y - drift.startY;
	if (v.getLength() > Drift.halfWidth
	    && !Settings.floatsEqual(v.getLength(), Drift.halfWidth)) {
	    return null;
	}
	DriftPosition res = new DriftPosition();
	res.drift = drift;
	res.vectorFactor = 0f;
	return res;
    }

    private void initMovement(Drift drift, float vectorFactor) 
	throws InvalidOperationException {
	movementTargetDrift = drift;
	movementTargetFactor = vectorFactor;
	if (movementTargetDrift != currentDrift) {
	    for (Cross c : currentDrift.crosses) {
		if (c.getOtherDrift(currentDrift) == movementTargetDrift) {
		    movementTransitionCross = c;
		    break;
		}
	    }
	}
    }

    private float moveOnDrift(float pixelsLeft, float targetFactor) {
	float stepFactor = pixelsLeft
	    / (currentDrift.vector.getLength()
	       * currentDrift.vectorFactor);
	float factorLeft = Math.abs(currentFactor - targetFactor);
	if (factorLeft < stepFactor
	    || Settings.floatsEqual(factorLeft, stepFactor)) {
	    currentFactor = targetFactor;
	    pixelsLeft = pixelsLeft -
		    factorLeft * currentDrift.vector.getLength();
	    if (Settings.floatsEqual(pixelsLeft, 0f)) {
		return 0f;
	    }
	    else {
		return pixelsLeft;
	    }
	}
	if (currentFactor > targetFactor) {
	    currentFactor -= stepFactor;
	}
	else {
	    currentFactor += stepFactor;
	}
	return 0f;
    }

    public class DriftPosition {

	public Drift drift;

	public float vectorFactor;
    }
}
