package com.brianmattllc.objectat.gui;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import com.brianmattllc.objectat.events.ObjectatEvent;

public class ObjectatEventViewer implements Runnable {
	public void run() {
		JFrame f = new JFrame("ObjectatEvent Viewer");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new FlowLayout());
		String columnNames[] = {
				"Key",
				"Category",
				"Classification",
				"Count",
				"Description",
				"From",
				"From Component",
				"From Component Identifier",
				"From Model",
				"From Vendor",
				"Group",
				"ID",
				"Last Change",
				"Severity",
				"Source",
				"Type",
				"First",
				"Last",
				"Owned",
				"Owned By"
		};
		
		String dataValues[][] = {
				
		};
		JTable eventTable = new JTable(dataValues, columnNames);
		JScrollPane eventScrollPane = new JScrollPane(eventTable);
		f.add(eventScrollPane);
		f.pack();
		f.setVisible(true);
	}

	public static void main(String[] args) {
		ObjectatEventViewer eventViewer = new ObjectatEventViewer();
		// Schedules the application to be run at the correct time in the event queue.
		SwingUtilities.invokeLater(eventViewer);
	}
}