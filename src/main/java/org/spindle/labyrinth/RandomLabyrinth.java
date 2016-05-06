package org.spindle.labyrinth;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;

public class RandomLabyrinth {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
		createAndShowGUI();
	    }
        });
    }

    private static void createAndShowGUI() {
	try {
	    System.out.println("Created GUI on EDT? "+
			       SwingUtilities.isEventDispatchThread());
	    JFrame f = new JFrame("Random Labyrinth");
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    final LabyrinthPanel myPanel = new LabyrinthPanel();
	    f.add(myPanel, BorderLayout.CENTER);
	    JPanel p = new JPanel();
	    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	    f.add(p, BorderLayout.LINE_END);
	    JButton button = new JButton("Generate");
	    button.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			myPanel.onGenerateClicked(e);
		    }
		});
	    p.add(button);
	    button = new JButton("Load");
	    button.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			myPanel.onLoadClicked(e);
		    }
		});
	    p.add(button);
	    button = new JButton("Save");
	    button.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			myPanel.onSaveClicked(e);
		    }
		});
	    p.add(button);
	    f.pack();
	    f.setVisible(true);
	}
	catch (InvalidOperationException ex) {
	    ex.printStackTrace();
	}
    }
}

class LabyrinthPanel extends JPanel 
    implements MouseListener {

    public Labyrinth labyrinth;

    public Walker walker;

    public Timer timer;

    public ShortestDistances shortestDistances;

    public DriftPoint target;

    public LabyrinthPanel() 
	throws InvalidOperationException {
	labyrinth = new Labyrinth();
	walker = new Walker();
	generateLabyrinth();
	addMouseListener(this);
	timer = new Timer
	    (1000 / Walker.MOVEMENT_STEP_SECOND_FRACTION,
	     new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
		     onTimer();
		 }
	     });
    }

    public Dimension getPreferredSize() {
        return new Dimension(Settings.PANEL_WIDTH, Settings.PANEL_HEIGHT);
    }

    public void onGenerateClicked(ActionEvent e) {
	try {
	    timer.stop();
	    generateLabyrinth();
	    repaint();
	}
	catch (InvalidOperationException ex) {
	    ex.printStackTrace();
	}
    }

    public void onSaveClicked(ActionEvent e) {
	ObjectOutputStream out = null;
	try {
	    JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
	    int returnVal = fc.showSaveDialog(this);
	    if (returnVal != JFileChooser.APPROVE_OPTION)
		return;
	    try {
		out = new ObjectOutputStream
		    (new BufferedOutputStream
		     (new FileOutputStream(fc.getSelectedFile())));
		out.writeObject(labyrinth.drifts.get(0));
	    } finally {
		out.close();
	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    public void onLoadClicked(ActionEvent e) {
	ObjectInputStream in = null;
	Drift drift0 = null;
	try {
	    JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
	    int returnVal = fc.showOpenDialog(this);
	    if (returnVal != JFileChooser.APPROVE_OPTION)
		return;
	    try {
		in = new ObjectInputStream
		    (new BufferedInputStream
		     (new FileInputStream(fc.getSelectedFile())));
		drift0 = (Drift)in.readObject();
	    } 
	    finally {
		in.close();
	    }
	    timer.stop();
	    labyrinth = new Labyrinth();
	    labyrinth.drifts = new ArrayList<Drift>();
	    labyrinth.drifts.add(drift0);
	    for (int i = 0; i < labyrinth.drifts.size(); i++) {
		Drift d = labyrinth.drifts.get(i);
		for (Cross c : d.crosses) {
		    if (labyrinth.drifts.indexOf(c.getOtherDrift(d)) == -1)
			labyrinth.drifts.add(c.getOtherDrift(d));
		}
	    }
	    walker.init(labyrinth);
	    shortestDistances = new ShortestDistances();
	    shortestDistances.calculate(labyrinth);
	    target = shortestDistances.getFarthestPoint(labyrinth);
	    repaint();
	}
	catch (ClassNotFoundException ex) {
	    ex.printStackTrace();
	}
	catch (IOException ex) {
	    ex.printStackTrace();
	}
	catch (InvalidOperationException ex) {
	    ex.printStackTrace();
	}
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);

	Graphics2D g2 = (Graphics2D)g;

	g2.setPaint(Color.YELLOW);
	g2.fill(new Rectangle(0, 0, Settings.PANEL_WIDTH, 
			      Settings.PANEL_HEIGHT));
	
	g2.setPaint(Color.BLACK);
	for (Drift d : labyrinth.drifts) {
	    if (walker.activeDrifts.contains(d)) {
		continue;
	    }
	    d.paintFilled(g2);
	}
	g2.setPaint(Color.CYAN);
	for (Drift d : walker.activeDrifts) {
	    d.paintFilled(g2);
	}
	
	g2.setPaint(Color.BLUE);
	walker.paintSelf(g2);
	
	g2.setColor(Color.RED);
	walker.paintTarget(g2, target);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
	try {
	    if (e.getClickCount() != 1) {
		return;
	    }
	    if (walker.processClick(labyrinth, e)) {
		timer.start();
	    }
	}
	catch (InvalidOperationException ex) {
	    ex.printStackTrace();
	}
    }

    public void onTimer() {
	try {
	    if (!walker.doMovement()) {
		timer.stop();
	    }
	    repaint();
	}
	catch (ShouldNotHappenException ex) {
	    ex.printStackTrace();
	}
	catch (InvalidOperationException ex) {
	    ex.printStackTrace();
	}
    }

    private void generateLabyrinth() 
	throws InvalidOperationException {
	labyrinth.generate();
	walker.init(labyrinth);
	shortestDistances = new ShortestDistances();
	shortestDistances.calculate(labyrinth);
	target = shortestDistances.getFarthestPoint(labyrinth);
    }
}
