package org.spindle.labyrinth;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.io.Serializable;

public class Drift extends LineSegment 
    implements Serializable {

    public static final float halfWidth = 15f;

    public ArrayList<org.spindle.labyrinth.Cross> crosses;

    public LineSegment leftBorder;

    public LineSegment rightBorder;

    public void paint(Graphics2D g2) {
	Vector v2 = vector.rotateMinus90(),
	    v1 = v2.getMinusVector();
	float v1lr = halfWidth / v1.getLength();
	Point2D.Float p1 = new Point2D.Float(startX + v1.x * v1lr,
					     startY + v1.y * v1lr),
	    pEnd = new Point2D.Float(startX + vector.x * vectorFactor,
				     startY + vector.y * vectorFactor),
	    p2 = new Point2D.Float(pEnd.x + v1.x * v1lr,
				   pEnd.y + v1.y * v1lr),
	    p3 = new Point2D.Float(pEnd.x + v2.x * v1lr,
				   pEnd.y + v2.y * v1lr),
	    p4 = new Point2D.Float(startX + v2.x * v1lr,
				   startY + v2.y * v1lr);
	g2.draw(new Line2D.Float(p1, p2));
	g2.draw(new Line2D.Float(p2, p3));
	g2.draw(new Line2D.Float(p3, p4));
	g2.draw(new Ellipse2D.Float(pEnd.x - halfWidth,
				    pEnd.y - halfWidth,
				    2f * halfWidth,
				    2f * halfWidth));
    }

    public void paintFilled(Graphics2D g2) {
	Vector v2 = vector.rotateMinus90(),
	    v1 = v2.getMinusVector();
	float v1lr = halfWidth / v1.getLength();
	Point2D.Float p1 = new Point2D.Float(startX + v1.x * v1lr,
					     startY + v1.y * v1lr),
	    pEnd = new Point2D.Float(startX + vector.x * vectorFactor,
				     startY + vector.y * vectorFactor),
	    p2 = new Point2D.Float(pEnd.x + v1.x * v1lr,
				   pEnd.y + v1.y * v1lr),
	    p3 = new Point2D.Float(pEnd.x + v2.x * v1lr,
				   pEnd.y + v2.y * v1lr),
	    p4 = new Point2D.Float(startX + v2.x * v1lr,
				   startY + v2.y * v1lr);
	GeneralPath driftRect = new GeneralPath();
	driftRect.moveTo(p1.x, p1.y);
	driftRect.lineTo(p2.x, p2.y);
	driftRect.lineTo(p3.x, p3.y);
	driftRect.lineTo(p4.x, p4.y);
	driftRect.closePath();
	g2.fill(driftRect);
	g2.fill(new Ellipse2D.Float(pEnd.x - halfWidth,
				    pEnd.y - halfWidth,
				    2f * halfWidth,
				    2f * halfWidth));
	g2.fill(new Ellipse2D.Float(startX - halfWidth,
				    startY - halfWidth,
				    2f * halfWidth,
				    2f * halfWidth));
    }

    public boolean touchMiddle(float x, float y) {
	Drift perp = new Drift();
	perp.startX = x;
	perp.startY = y;
	perp.vector = vector.rotateMinus90();
	perp.vectorFactor = 1f;
	LineSegment.LinesCross thisc = crossLines(perp);
	LineSegment.LinesCross perpc = perp.crossLines(this);	
	if (!inSegment(thisc.vectorFactor, this.vectorFactor))
	    return false;
	Vector perpv = new Vector(perpc.vectorFactor * perp.vector.x,
				  perpc.vectorFactor * perp.vector.y);
	return perpv.getLength() <= halfWidth * 2
	    || Settings.floatsEqual(perpv.getLength(), halfWidth * 2);
    }

    public boolean touchEnd(float x1, float y1, float x2, float y2) {
	Vector v = new Vector(x2 - x1, y2 - y1);
	return v.getLength() <= halfWidth * 2
	    || Settings.floatsEqual(v.getLength(), halfWidth * 2);
    }

    public TouchMiddle checkTouchMiddle(Drift drift2) {
	TouchMiddle res = new TouchMiddle();
	res.s21 = touchMiddle(drift2.startX, drift2.startY);
	res.e21 = touchMiddle(drift2.getEndX(), drift2.getEndY());
	res.s12 = drift2.touchMiddle(startX, startY);
	res.e12 = drift2.touchMiddle(getEndX(), getEndY());
	return res;
    }

    public TouchEnd checkTouchEnd(Drift drift2) {
	TouchEnd res = new TouchEnd();
	res.s2s1 = touchEnd(startX, startY, drift2.startX, drift2.startY);
	res.e2s1 = touchEnd(startX, startY, drift2.getEndX(), drift2.getEndY());
	res.s2e1 = touchEnd(getEndX(), getEndY(), drift2.startX, drift2.startY);
	res.e2e1 = touchEnd(getEndX(), getEndY(), 
			    drift2.getEndX(), drift2.getEndY());
	return res;
    }

    public void calculateBorders() {
	Vector vr = vector.rotateMinus90(),
	    vl = vr.getMinusVector();
	float vlr = halfWidth / vr.getLength();
	leftBorder = new LineSegment();
	leftBorder.startX = startX + vl.x * vlr;
	leftBorder.startY = startY + vl.y * vlr;
	leftBorder.vector = new Vector(vector.x, vector.y);
	leftBorder.vectorFactor = vectorFactor;
	rightBorder = new LineSegment();
	rightBorder.startX = startX + vr.x * vlr;
	rightBorder.startY = startY + vr.y * vlr;
	rightBorder.vector = new Vector(vector.x, vector.y);
	rightBorder.vectorFactor = vectorFactor;
    }

    public BorderCross crossByBorders(LineSegment otherBorder) {
	LineSegment.LinesCross leftCross = leftBorder.crossLines(otherBorder);
	LineSegment.LinesCross rightCross = rightBorder.crossLines(otherBorder);
	if (!LineSegment.inSegment(leftCross.vectorFactor, 
				   leftBorder.vectorFactor)
	    && !LineSegment.inSegment(rightCross.vectorFactor,
				      rightBorder.vectorFactor)) {
	    return null;
	}
	leftCross = otherBorder.crossLines(leftBorder);
	rightCross = otherBorder.crossLines(rightBorder);
	BorderCross res = new BorderCross();
	res.higher = Math.max(leftCross.vectorFactor,
			      rightCross.vectorFactor);
	res.lower = Math.min(leftCross.vectorFactor,
			     rightCross.vectorFactor);
	if (res.lower > otherBorder.vectorFactor
	    || Settings.floatsEqual(res.lower, otherBorder.vectorFactor)
	    || res.higher < 0
	    || Settings.floatsEqual(res.higher, 0)) {
	    return null;
	}
	if (res.lower < 0) {
	    res.lower = 0;
	}
	if (res.higher > otherBorder.vectorFactor) {
	    res.higher = otherBorder.vectorFactor;
	}
	return res;
    }

    public class TouchMiddle {
	
	public boolean s21;
	
	public boolean e21;

	public boolean s12;

	public boolean e12;

	public boolean touch() {
	    return s21 || e21 || s12 || e12;
	}
    }

    public class TouchEnd {

	public boolean s2s1;

	public boolean e2s1;

	public boolean s2e1;

	public boolean e2e1;

	public boolean touch() {
	    return s2s1 || e2s1 || s2e1 || e2e1;
	}
    }

    public class BorderCross 
	implements Serializable {
	
	public float lower;

	public float higher;
    }
}
