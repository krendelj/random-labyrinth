package org.spindle.labyrinth;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DriftTestSamplesReader {

    public Drift drift1;

    public Drift drift2;

    public void readSampleFile() {
	try {
	    Path file = Paths.get("testSamples.txt");
	    BufferedReader reader = Files.newBufferedReader
		(file, StandardCharsets.UTF_8);
	    try {
		drift1 = readDrift(reader);
		drift2 = readDrift(reader);
	    }
	    catch (LabyrinthReadSampleFileException e) {
	    }
	    finally {
		reader.close();
	    }
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private Drift readDrift(BufferedReader reader) 
	throws IOException, LabyrinthReadSampleFileException {
	String line = reader.readLine();
	Drift d = new Drift();
	d.vector = new Vector();
	Pattern p = Pattern.compile("^\\s*(\\w+)\\s*=\\s*([\\d\\.-]+)\\s*$");
	while (line != null && !line.isEmpty()) {
	    Matcher m = p.matcher(line);
	    if (!m.find()) {
		System.out.println("line doesn't match:");
		System.out.println(line);
		throw new LabyrinthReadSampleFileException();
	    }
	    if (m.group(1).equals("startX")) {
		d.startX = Float.parseFloat(m.group(2));
	    }
	    else if (m.group(1).equals("startY")) {
		d.startY = Float.parseFloat(m.group(2));
	    }
	    else if (m.group(1).equals("vectorX")) {
		d.vector.x = Float.parseFloat(m.group(2));
	    }
	    else if (m.group(1).equals("vectorY")) {
		d.vector.y = Float.parseFloat(m.group(2));
	    }
	    else if (m.group(1).equals("vectorFactor")) {
		d.vectorFactor = Float.parseFloat(m.group(2));
	    }
	    line = reader.readLine();
	}
	return d;
    }
}
