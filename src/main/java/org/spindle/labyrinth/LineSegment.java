package org.spindle.labyrinth;

import java.io.Serializable;

public class LineSegment
    implements Serializable {

    public float startX;

    public float startY;

    public Vector vector;

    public float vectorFactor;

    public float getEndX() {
	return startX + vector.x * vectorFactor;
    }

    public float getEndY() {
	return startY + vector.y * vectorFactor;
    }

    public LinesCross crossLines(LineSegment segment2) {
	LinesCross res = new LinesCross();
	float denominator = this.vector.x * segment2.vector.y
	    - this.vector.y * segment2.vector.x;
        if (Settings.floatsEqual(denominator, 0f)) {
	    res.parallel = true;
	    return res;
	}
	res.parallel = false;
	res.vectorFactor = 
	    (segment2.vector.y * (segment2.startX - this.startX)
	     + segment2.vector.x * (this.startY - segment2.startY))
	    / denominator;
	return res;
    }

    public static boolean inSegment(float factor, float vectorFactor) {
	if (Settings.floatsEqual(factor, 0f)
	    || Settings.floatsEqual(factor, vectorFactor))
	    return true;
	if (vectorFactor >= 0f) {
	    return factor <= vectorFactor
		&& factor >= 0f;
	}
	else {
	    return factor >= vectorFactor
		&& factor <= 0f;
	}
    }

    public SegmentsCross crossSegments(LineSegment segment2) {
	SegmentsCross res = new SegmentsCross();
	LinesCross c = this.crossLines(segment2);
	if (c.parallel) {
	    res.parallel = true;
	    return res;
	}
	res.parallel = false;
	res.vectorFactor1 = c.vectorFactor;
	c = segment2.crossLines(this);
	res.vectorFactor2 = c.vectorFactor;
	res.cross = inSegment(res.vectorFactor1, this.vectorFactor)
	    && inSegment(res.vectorFactor2, segment2.vectorFactor);
	return res;
    }

    public class LinesCross {
	
	public boolean parallel;

	public float vectorFactor;
    }

    public class SegmentsCross {

	public boolean parallel;

	public boolean cross;

	public float vectorFactor1;

	public float vectorFactor2;
    }

}
