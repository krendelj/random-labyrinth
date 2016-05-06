package org.spindle.labyrinth;

import java.io.Serializable;

public class Vector implements Serializable {
    
    public float x;

    public float y;

    public Vector() { }

    public Vector(float x, float y) {
	this.x = x;
	this.y = y;
    }

    public Vector rotateMinus90() {
	return new Vector(-y, x);
    }

    public Vector getMinusVector() {
	return new Vector(-x, -y);
    }

    public float getLength() {
	return (float)Math.sqrt(x * x + y * y);
    }

    public float getCosine(Vector vector2) {
	return (x * vector2.x + y * vector2.y) 
	    / (getLength() * vector2.getLength());
    }
}
