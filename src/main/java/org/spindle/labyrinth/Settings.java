package org.spindle.labyrinth;

public class Settings {
    
    public static final int PANEL_WIDTH = 500;

    public static final int PANEL_HEIGHT = 400;

    public static boolean floatsEqual(float a, float b) {
	return Math.abs(a - b) < 0.0000001f;
    }

}
