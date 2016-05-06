package org.spindle.labyrinth;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.ArrayList;
import java.io.IOException;
import java.io.Serializable;

public class SamplesCrossTest {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
		createAndShowGUI();
	    }
        });
    }
    
    private static void createAndShowGUI() {
        System.out.println("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());
	System.out.println("Working directory: " + 
			   System.getProperty("user.dir"));
        JFrame f = new JFrame("Lines Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	MyPanel myPanel = new MyPanel();
        f.add(myPanel, BorderLayout.CENTER);
	JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	f.add(p, BorderLayout.LINE_END);
	JButton button = new JButton("Reread");
	button.addActionListener(myPanel);
	p.add(button);
        f.pack();
        f.setVisible(true);
    }

}

class MyPanel extends JPanel
    implements ActionListener {

    public DriftTestSamplesReader driftTestSamplesReader;

    public MyPanel() {
	driftTestSamplesReader = new DriftTestSamplesReader();
	readSample();
    }

    public Dimension getPreferredSize() {
        return new Dimension(Settings.PANEL_WIDTH, Settings.PANEL_HEIGHT);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

	Graphics2D g2 = (Graphics2D)g;

	g2.setPaint(Color.yellow);
	g2.fill(new Rectangle(0, 0, Settings.PANEL_WIDTH, 
			      Settings.PANEL_HEIGHT));
	
	g2.setColor(Color.black);
	driftTestSamplesReader.drift1.paint(g2);
	driftTestSamplesReader.drift2.paint(g2);
    }

    public void actionPerformed(ActionEvent e) {
	readSample();
	repaint();
    }

    private void readSample() {
	DriftTestSamplesReader tsr = driftTestSamplesReader;
	System.out.println("Reading sample");
	tsr.readSampleFile();
       	LineSegment.SegmentsCross tc = tsr.drift1.crossSegments(tsr.drift2);
	System.out.println("crossSegments: " + tc.cross);
	Drift.TouchMiddle tm = tsr.drift1.checkTouchMiddle(tsr.drift2);
	System.out.println("touchMiddle: 2.start->1: "
			   + tm.s21
			   + " 2.end->1: "
			   + tm.e21
			   + " 1.start->2: "
			   + tm.s12
			   + " 1.end->2: "
			   + tm.e12);
	Drift.TouchEnd te = tsr.drift1.checkTouchEnd(tsr.drift2);
	System.out.println("touchEnds: 2.start->1.start: "
			   + te.s2s1
			   + " 2.end->1.start: "
			   + te.e2s1
			   + " 2.start->1.end: "
			   + te.s2e1
			   + " 2.end->1.end: "
			   + te.e2e1);
	System.out.println("cosine: "
			   + tsr.drift1.vector.getCosine(tsr.drift2.vector));
    }
}
