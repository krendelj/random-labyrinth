package org.spindle.labyrinth;

import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.Collections;

public class Labyrinth {
    
    public final int N_DRIFTS = 8;

    public final float MAX_COS = 0.946164f;

    public final int MAX_WASTED_CYCLES = 143;

    public ArrayList<Drift> drifts;

    public void generate() 
	throws InvalidOperationException {
	drifts = new ArrayList<Drift>();
	float totalLen = 0f;
	Random rnd = new Random();
	generateFirstDrift(rnd);
	totalLen += drifts.get(0).vector.getLength()
	    * Math.abs(drifts.get(0).vectorFactor);
	for (int nd = 1; nd < N_DRIFTS; nd++) {
	    Drift gend;
	    boolean fits;
	    RandomBase rb;
	    Cross basec;
	    int wastedCycles = 0;
	    do {
	        rb = getRandomBase(rnd, totalLen);
		gend = new Drift();
		gend.startX = rb.base.startX + rb.base.vector.x * rb.factor;
		gend.startY = rb.base.startY + rb.base.vector.y * rb.factor;
		gend.vector = new Vector();
		gend.crosses = new ArrayList<Cross>();
		basec = new Cross(rb.base, rb.factor, gend, 0f);
		gend.crosses.add(basec);
		chooseRandomDirection(rnd, gend);
		gend.calculateBorders();
		if (!angleIsGood(rb.base, gend)
		    || !checkBorderCrosses(rb.base, basec)) {
		    fits = false;
		    continue;
		}
		fits = fitIntoLabyrinth(gend, rb);
	    } while (!fits && wastedCycles++ < MAX_WASTED_CYCLES);
	    if (!fits) {
		break;
	    }
	    rb.base.crosses.add(basec);
	    drifts.add(gend);
	    totalLen += gend.vector.getLength() 
		* Math.abs(gend.vectorFactor);
	}
    }

    private void chooseRandomDirection(Random r, Drift drift) {
	do {
	    drift.vector.x = 
		(r.nextFloat() * 
		(Settings.PANEL_WIDTH - 2 * Drift.halfWidth)
		 + Drift.halfWidth) - drift.startX;
	    drift.vector.y = 
		(r.nextFloat() * 
		(Settings.PANEL_HEIGHT - 2 * Drift.halfWidth)
		 + Drift.halfWidth) - drift.startY;
	} while (drift.vector.getLength() <= Drift.halfWidth
		 || Settings.floatsEqual(drift.vector.getLength(),
					   Drift.halfWidth));
	drift.vectorFactor = 1f;
    }

    private boolean angleIsGood(Drift d1, Drift d2) {
	float cos = d1.vector.getCosine(d2.vector);
	if (cos >= 0) {
	    return cos < MAX_COS
		&& !Settings.floatsEqual(cos, MAX_COS);
	}
	else {
	    return cos > -MAX_COS
		&& !Settings.floatsEqual(cos, -MAX_COS);
	}
    }

    private void generateFirstDrift(Random rnd) {
	Drift drift0 = new Drift();
	drifts.add(drift0);
	drift0.startX = Drift.halfWidth;
	drift0.startY = Drift.halfWidth;
	drift0.vector = new Vector();
	drift0.crosses = new ArrayList<Cross>();
	chooseRandomDirection(rnd, drift0);
	drift0.calculateBorders();
    }

    private RandomBase getRandomBase(Random rnd, float totalLen) {
	RandomBase res = new RandomBase();
	float factor = rnd.nextFloat() * totalLen;
	Drift base = null;
	for (Drift d : drifts) {
	    float l = Math.abs(d.vectorFactor) * d.vector.getLength();
	    if (factor <= l
		|| Settings.floatsEqual(factor, l)) {
		base = d;
		break;
	    }
	    factor -= l;
	}
	factor = factor / (base.vector.getLength() 
			   * base.vectorFactor);
	res.base = base;
	res.factor = factor;
	return res;
    }

    private boolean fitIntoLabyrinth(Drift gend, RandomBase rb) 
	throws InvalidOperationException {
	ArrayList<Cross> crosses2 = new ArrayList<Cross>();
	for (Drift d : drifts) {
	    if (d == rb.base) continue;
	    LineSegment.SegmentsCross tc = d.crossSegments(gend);
	    if (tc.cross) {
		if (!angleIsGood(d, gend)) {
		    return false;
		}
		Cross c = new Cross(d, tc.vectorFactor1, 
				    gend, tc.vectorFactor2);
		if (!checkBorderCrosses(gend, c)) {
		    return false;
		}
		gend.crosses.add(c);
		if (!checkBorderCrosses(d, c)) {
		    return false;
		}
		crosses2.add(c);
	    }
	    else {
		if (d.checkTouchMiddle(gend).touch()
		    || d.checkTouchEnd(gend).touch()) {
		    return false;
		}
	    }
	}
	for (int i = 1; i < gend.crosses.size(); i++) {
	    gend.crosses.get(i).getOtherDrift(gend).crosses.add
		(crosses2.get(i - 1));
	}
	return true;
    }

    private boolean checkBorderCrosses(Drift d, Cross c)
	throws InvalidOperationException {
	c.setRightBorderCross(d, c.getOtherDrift(d).crossByBorders
			      (d.rightBorder));
	c.setLeftBorderCross(d, c.getOtherDrift(d).crossByBorders
			     (d.leftBorder));
	for (Cross dc : d.crosses) {
	    if (c.getRightBorderCross(d) != null
		&& dc.getRightBorderCross(d) != null
		&& borderCrossesOverlap
		(c.getRightBorderCross(d), dc.getRightBorderCross(d))) {
		return false;
	    }
	    if (c.getLeftBorderCross(d) != null
		&& dc.getLeftBorderCross(d) != null
		&& borderCrossesOverlap
		(c.getLeftBorderCross(d), dc.getLeftBorderCross(d))) {
		return false;
	    }
	}
	return true;
    }

    private boolean borderCrossesOverlap(Drift.BorderCross bc1, 
					 Drift.BorderCross bc2) {
	return (bc1.lower < bc2.higher
		|| Settings.floatsEqual(bc1.lower, bc2.higher))
	    && (bc1.higher > bc2.lower
		|| Settings.floatsEqual(bc1.higher, bc2.lower));
    }

    public class RandomBase {

	public float factor;

	public Drift base;
    }
}
