package ui;
import geoJSON.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.*;

public class DialogShowProperties extends JDialog {
	private JTable table;
	
	FeatureCollection fc;
	
	public void setTableSource(FeatureCollection fc) {
		this.fc = fc;
		String[] headers = fc.getHeaders();
		String[][] data = fc.getData(headers);
		Vector<Feature> vf = fc.features;
		System.out.println("found "+headers.length+" headers and "+vf.size()+" rows");
		

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new DefaultTableModel(data,headers));
		table.invalidate();
		table.repaint();
		
	}
	public DialogShowProperties() {
		setTitle("Properties table");
		//getContentPane().setLayout(null);
		this.setSize(new Dimension(400,400));
		getContentPane().setPreferredSize(new Dimension(400,400));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(254, 136, 61, 64);
		getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);

	}
}