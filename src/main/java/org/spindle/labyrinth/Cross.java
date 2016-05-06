package org.spindle.labyrinth;

import java.io.Serializable;

public class Cross implements Serializable {
    
    public Drift drift1;

    public Drift drift2;

    public float vectorFactor1;

    public float vectorFactor2;
    
    public Drift.BorderCross rightBorderCross1;

    public Drift.BorderCross leftBorderCross1;

    public Drift.BorderCross rightBorderCross2;

    public Drift.BorderCross leftBorderCross2;

    public Cross(Drift drift1, float vectorFactor1,
		 Drift drift2, float vectorFactor2) {
	this.drift1 = drift1;
	this.vectorFactor1 = vectorFactor1;
	this.drift2 = drift2;
	this.vectorFactor2 = vectorFactor2;
    }

    public float getVectorFactor(Drift drift) 
	throws InvalidOperationException {
	if (drift == drift1) {
	    return vectorFactor1;
	}
	else if (drift == drift2) {
	    return vectorFactor2;
	}
	else throw new InvalidOperationException();
    }

    public Drift getOtherDrift(Drift drift) 
	throws InvalidOperationException {
	if (drift == drift1) {
	    return drift2;
	}
	else if (drift == drift2) {
	    return drift1;
	}
	else throw new InvalidOperationException();
    }

    public Drift.BorderCross getRightBorderCross(Drift drift) 
	throws InvalidOperationException {
	if (drift == drift1) {
	    return rightBorderCross1;
	}
	else if (drift == drift2) {
	    return rightBorderCross2;
	}
	else throw new InvalidOperationException();
    }

    public Drift.BorderCross getLeftBorderCross(Drift drift)
	throws InvalidOperationException {
	if (drift == drift1) {
	    return leftBorderCross1;
	}
	else if (drift == drift2) {
	    return leftBorderCross2;
	}
	else throw new InvalidOperationException();
    }

    public void setRightBorderCross(Drift drift, Drift.BorderCross borderCross) 
	throws InvalidOperationException {
	if (drift == drift1) {
	    rightBorderCross1 = borderCross;
	}
	else if (drift == drift2) {
	    rightBorderCross2 = borderCross;
	}
	else throw new InvalidOperationException();
    }

    public void setLeftBorderCross(Drift drift, Drift.BorderCross borderCross) 
	throws InvalidOperationException {
	if (drift == drift1) {
	    leftBorderCross1 = borderCross;
	}
	else if (drift == drift2) {
	    leftBorderCross2 = borderCross;
	}
	else throw new InvalidOperationException();
    }
}
