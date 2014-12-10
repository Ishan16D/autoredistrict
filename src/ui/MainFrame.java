package ui;

import geoJSON.Feature;
import geoJSON.FeatureCollection;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import mapCandidates.*;

import javax.swing.event.*;
import javax.swing.border.*;


public class MainFrame extends JFrame {
	boolean suppress_duplicates = false;
	
	JTextField textField_1 = new JTextField();
	JTextField textField = new JTextField();
	JSlider slider = new JSlider();
	JSlider slider_1 = new JSlider();
	JSlider slider_2 = new JSlider();
	JSlider slider_3 = new JSlider();
	JSlider slider_4 = new JSlider();
	JSlider slider_5 = new JSlider();
	JSlider slider_6 = new JSlider();
	JSlider slider_7 = new JSlider();
	JSlider slider_8 = new JSlider();
	
	double minx,maxx,miny,maxy;
	
	MapPanel mapPanel = new MapPanel(); 


	public Ecology ecology = new Ecology();
	public FeatureCollection featureCollection = new FeatureCollection();
	
	public MainFrame() {
		Dimension d = new Dimension(800,1024);
		//this.setPreferredSize(d);
		this.setSize(d);
		//this.getContentPane().setPreferredSize(d);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();
				StringBuffer sb = new StringBuffer();
				try {
					FileInputStream fis = new FileInputStream(f);
					while( fis.available() > 0) {
						byte[] bb = new byte[fis.available()];
						fis.read(bb);
						sb.append(new String(bb));
						Thread.sleep(10);
					}
					
					fis.close();
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					return;
				} 
				
				ecology = new Ecology();
				try {
					ecology.fromJSON(sb.toString());
				} catch (Exception ex) {
					System.out.println("ex "+ex);
					ex.printStackTrace();
				}

			}
		});
		
		JMenuItem mntmClear = new JMenuItem("Clear");
		mnFile.add(mntmClear);
		mnFile.add(mntmOpen);
		
		JMenuItem mntmOpenGeojson = new JMenuItem("Open GeoJSON file");
		mntmOpenGeojson.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				//jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//jfc.sh
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				/*if( !fd.isDirectory()) {
					return;
				}*/
				File[] ff = new File[]{fd};//fd.listFiles();
				
				featureCollection = new FeatureCollection(); 
				featureCollection.features = new Vector<Feature>();
				HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();
				
				for( int i = 0; i < ff.length; i++) {
					String s = ff[i].getName().toLowerCase();
					if(s.indexOf(".json") < 0) {
						continue;
					}
					System.out.println("Processing "+s+"...");
					File f = ff[i];
					StringBuffer sb = new StringBuffer();
					try {
						FileInputStream fis = new FileInputStream(f);
						while( fis.available() > 0) {
							byte[] bb = new byte[fis.available()];
							fis.read(bb);
							sb.append(new String(bb));
							Thread.sleep(10);
						}
						
						fis.close();
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						return;
					} 
					
					FeatureCollection fc = new FeatureCollection();
					try {
						fc.fromJSON(sb.toString());
					} catch (Exception ex) {
						System.out.println("ex "+ex);
						ex.printStackTrace();
					}
					for( Feature fe : fc.features) {
						//if( fe.properties.DISTRICT != null && !fe.properties.DISTRICT.toLowerCase().equals("null") ) {
						if( suppress_duplicates) {
							hmFeatures.put(fe.properties.DISTRICT, fe);
						} else {
							featureCollection.features.add(fe);
						}
						//}
					}
					
				}
				for( Feature fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				Vector<Feature> features = featureCollection.features;
				System.out.println(features.size()+" precincts loaded.");
				System.out.println("Initializing blocks...");
				featureCollection.initBlocks();
				minx = features.get(0).geometry.coordinates[0][0][0];
				maxx = features.get(0).geometry.coordinates[0][0][0];
				miny = features.get(0).geometry.coordinates[0][0][1];
				maxy = features.get(0).geometry.coordinates[0][0][1];
				HashSet<String> types = new HashSet<String>();
				for( Feature f : features) {
					double[][][] coordinates2 = f.geometry.coordinates;
					for( int j = 0; j < coordinates2.length; j++) {
						double[][] coordinates = coordinates2[j];
						for( int i = 0; i < coordinates.length; i++) {
							if( coordinates[i][0] < minx) {
								minx = coordinates[i][0];
							}
							if( coordinates[i][0] > maxx) {
								maxx = coordinates[i][0];
							}
							if( coordinates[i][1] < miny) {
								miny = coordinates[i][1];
							}
							if( coordinates[i][1] > maxy) {
								maxy = coordinates[i][1];
							}
						}
					}					
				}
				System.out.println(""+minx+","+miny);
				System.out.println(""+maxx+","+maxy);
				
				mapPanel.minx = minx;
				mapPanel.miny = miny;
				mapPanel.maxx = maxx;
				mapPanel.maxy = maxy;
				mapPanel.features = features;
				mapPanel.invalidate();
				mapPanel.repaint();
				System.out.println("Ready.");
			}
		});
		mnFile.add(mntmOpenGeojson);
		
		JMenuItem mntmOpenGeojsonFolder = new JMenuItem("Open GeoJSON folder");
		mntmOpenGeojsonFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//jfc.sh
				jfc.showOpenDialog(null);
				File fd = jfc.getSelectedFile();
				if( !fd.isDirectory()) {
					return;
				}
				File[] ff = fd.listFiles();
				
				featureCollection = new FeatureCollection(); 
				featureCollection.features = new Vector<Feature>();
				HashMap<String,Feature> hmFeatures = new HashMap<String,Feature>();
				
				for( int i = 0; i < ff.length; i++) {
					String s = ff[i].getName().toLowerCase();
					if(s.indexOf(".json") < 0) {
						continue;
					}
					System.out.println("Processing "+s+"...");
					File f = ff[i];
					StringBuffer sb = new StringBuffer();
					try {
						FileInputStream fis = new FileInputStream(f);
						while( fis.available() > 0) {
							byte[] bb = new byte[fis.available()];
							fis.read(bb);
							sb.append(new String(bb));
							Thread.sleep(10);
						}
						
						fis.close();
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						return;
					} 
					
					FeatureCollection fc = new FeatureCollection();
					try {
						fc.fromJSON(sb.toString());
					} catch (Exception ex) {
						System.out.println("ex "+ex);
						ex.printStackTrace();
					}
					for( Feature fe : fc.features) {
						//if( fe.properties.DISTRICT != null && !fe.properties.DISTRICT.toLowerCase().equals("null") ) {
						if( suppress_duplicates) {
							hmFeatures.put(fe.properties.DISTRICT, fe);
						} else {
							featureCollection.features.add(fe);
						}
						//}
					}
					
				}
				for( Feature fe : hmFeatures.values()) {
					featureCollection.features.add(fe);
				}
				Vector<Feature> features = featureCollection.features;
				System.out.println(features.size()+" precincts loaded.");
				System.out.println("Initializing blocks...");
				featureCollection.initBlocks();

				minx = features.get(0).geometry.coordinates[0][0][0];
				maxx = features.get(0).geometry.coordinates[0][0][0];
				miny = features.get(0).geometry.coordinates[0][0][1];
				maxy = features.get(0).geometry.coordinates[0][0][1];
				HashSet<String> types = new HashSet<String>();
				for( Feature f : features) {
					double[][][] coordinates2 = f.geometry.coordinates;
					for( int j = 0; j < coordinates2.length; j++) {
						double[][] coordinates = coordinates2[j];
						for( int i = 0; i < coordinates.length; i++) {
							if( coordinates[i][0] < minx) {
								minx = coordinates[i][0];
							}
							if( coordinates[i][0] > maxx) {
								maxx = coordinates[i][0];
							}
							if( coordinates[i][1] < miny) {
								miny = coordinates[i][1];
							}
							if( coordinates[i][1] > maxy) {
								maxy = coordinates[i][1];
							}
						}
					}					
				}
				System.out.println(""+minx+","+miny);
				System.out.println(""+maxx+","+maxy);
				
				mapPanel.minx = minx;
				mapPanel.miny = miny;
				mapPanel.maxx = maxx;
				mapPanel.maxy = maxy;
				mapPanel.features = features;
				mapPanel.invalidate();
				mapPanel.repaint();
				System.out.println("Ready.");

			}
		});
		mnFile.add(mntmOpenGeojsonFolder);
		
		JMenuItem mntmOpenElectionResults = new JMenuItem("Open Election results");
		mntmOpenElectionResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();
				StringBuffer sb = new StringBuffer();
				try {
					FileInputStream fis = new FileInputStream(f);
					while( fis.available() > 0) {
						byte[] bb = new byte[fis.available()];
						fis.read(bb);
						sb.append(new String(bb));
						Thread.sleep(10);
					}
					
					fis.close();
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					return;
				}
				String s = sb.toString();
				String[] lines = s.split("\n");
				int num_candidates = lines[0].split("\t").length - 1;
				HashMap<String,double[]> votes = new HashMap<String,double[]>();
				for( int i = 0; i < lines.length; i++) {
					String[] ss = lines[i].split("\t");
					String district = ss[0].trim();
					double[] dd = votes.get(district);
					if( dd == null) {
						dd = new double[num_candidates];
						for( int j = 0; j < num_candidates; j++) {
							dd[j] = 0;
						}
						votes.put(district, dd);
					}
					for( int j = 0; j < num_candidates && j < ss.length-1; j++) {
						dd[j] += Double.parseDouble(ss[j+1]);
					}
				}
				
				for( Entry<String, double[]> es : votes.entrySet()) {
					Block b = featureCollection.precinctHash.get(es.getKey());
					double[] dd = es.getValue();
					for( int j = 0; j < num_candidates; j++) {
						Demographic d = new Demographic();
						d.block_id = b.id;
						d.turnout_probability = 1;
						d.population = (int) dd[j];
						d.vote_prob = new double[num_candidates];
						for( int i = 0; i < d.vote_prob.length; i++) {
							d.vote_prob[i] = 0;
						}
						d.vote_prob[j] = 1;
						b.demographics.add(d);
					}
				}
				
			}
		});
		mnFile.add(mntmOpenElectionResults);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		mnFile.add(new JSeparator());
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		JPanel panel_1 = new JPanel();
		panel.setPreferredSize(new Dimension(200,100));
		panel_1.setPreferredSize(new Dimension(200,100));
		panel.setLayout(null);
		panel_1.setLayout(null);
		splitPane.setLeftComponent(panel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(0, 424, 200, 343);
		panel.add(panel_2);
		panel_2.setLayout(null);
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JLabel lblCompactness = new JLabel("compactness");
		lblCompactness.setBounds(6, 36, 172, 16);
		panel_2.add(lblCompactness);
		slider_3.setBounds(6, 57, 190, 29);
		panel_2.add(slider_3);
		
		JLabel lblContiguency = new JLabel("connectedness");
		lblContiguency.setBounds(6, 280, 172, 16);
		panel_2.add(lblContiguency);
		slider_7.setBounds(6, 301, 190, 29);
		panel_2.add(slider_7);
		
		JLabel lblEvolutionaryPressure = new JLabel("Evolutionary pressure");
		lblEvolutionaryPressure.setBounds(6, 8, 179, 16);
		panel_2.add(lblEvolutionaryPressure);
		
		JLabel lblPopulationBalance = new JLabel("population balance");
		lblPopulationBalance.setBounds(6, 98, 172, 16);
		panel_2.add(lblPopulationBalance);
		slider_4.setBounds(6, 119, 190, 29);
		panel_2.add(slider_4);
		
		JLabel lblProportionalRepresentation = new JLabel("proportional representation\n");
		lblProportionalRepresentation.setBounds(6, 161, 172, 16);
		panel_2.add(lblProportionalRepresentation);
		slider_5.setBounds(6, 182, 190, 29);
		panel_2.add(slider_5);
		
		JLabel lblVotingPowerBalance = new JLabel("voting power balance");
		lblVotingPowerBalance.setBounds(6, 218, 172, 16);
		panel_2.add(lblVotingPowerBalance);
		slider_6.setBounds(6, 239, 190, 29);
		panel_2.add(slider_6);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setBounds(0, 0, 200, 358);
		panel.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblPopulation = new JLabel("Population");
		lblPopulation.setBounds(6, 40, 104, 16);
		panel_3.add(lblPopulation);
		textField.setBounds(105, 34, 91, 28);
		panel_3.add(textField);
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textField.postActionEvent();
			}
		});
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 Settings.population = new Integer(textField.getText());
					 ecology.resize_population();
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		textField.setText("512");
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Population dynamics");
		lblNewLabel.setBounds(6, 6, 159, 16);
		panel_3.add(lblNewLabel);
		textField_1.setBounds(105, 68, 91, 28);
		panel_3.add(textField_1);
		textField_1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textField_1.postActionEvent();
			}
		});
		textField_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
					 Settings.trials = new Integer(textField.getText());
				 } catch (Exception ex) {
					 
				 }
			}
		});
		
		textField_1.setText("128");
		textField_1.setColumns(10);
		
		JLabel lblTrials = new JLabel("Trials");
		lblTrials.setBounds(6, 74, 104, 16);
		panel_3.add(lblTrials);
		
		JLabel lblPopulationReplaced = new JLabel("% population replaced");
		lblPopulationReplaced.setBounds(6, 108, 172, 16);
		panel_3.add(lblPopulationReplaced);
		slider.setBounds(6, 129, 190, 29);
		panel_3.add(slider);
		JLabel lblBorderMutation = new JLabel("% border mutation");
		lblBorderMutation.setBounds(6, 170, 172, 16);
		panel_3.add(lblBorderMutation);
		slider_1.setBounds(6, 191, 190, 29);
		panel_3.add(slider_1);
		
		JLabel lblScatterMutation = new JLabel("% scatter mutation");
		lblScatterMutation.setBounds(6, 232, 172, 16);
		panel_3.add(lblScatterMutation);
		slider_2.setBounds(6, 253, 190, 29);
		panel_3.add(slider_2);
		
		JLabel lblSpeciation = new JLabel("% speciation");
		lblSpeciation.setBounds(6, 294, 172, 16);
		panel_3.add(lblSpeciation);
		
		slider_8.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.species_fraction = 1.0 - (slider_8.getValue()/100.0);
			}
		});
		slider_8.setBounds(6, 315, 190, 29);
		panel_3.add(slider_8);
		slider_2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_rate = slider.getValue()/100.0;
			}
		});
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.mutation_boundary_rate = slider.getValue()/100.0;
			}
		});
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.replace_fraction = slider.getValue()/100.0;
			}
		});
		slider_6.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.voting_power_balance_weight = slider_6.getValue()/100.0;
			}
		});
		slider_5.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.disenfranchise_weight = slider_5.getValue()/100.0;

			}
		});
		slider_4.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.population_balance_weight = slider_3.getValue()/100.0;
			}
		});
		slider_7.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.disconnected_population_weight = slider_7.getValue()/100.0;
			}
		});
		slider_3.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Settings.geometry_weight = slider_3.getValue()/100.0;
			}
		});
		
		splitPane.setRightComponent(mapPanel);
	}
}