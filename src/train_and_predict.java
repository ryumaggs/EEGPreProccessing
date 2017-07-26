import java.util.*;
import java.lang.Runtime;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class train_and_predict extends Frame implements ActionListener{
	//Containers for buttons
	JFrame frame;
	JPanel panel;
	
	/*
	 * Buttons for GUI:
	 * 1. set_path: 	Button that will allow user to select directory containing training data
	 * 2. svm_path: 	Button that allows user to set directory path for the SVM
	 * 3. train: 		once 1 and 2 have been set, will create a separate process to run the svm training
	 * 4. closeHand: 	pre-made button that will cause hand to close via svm_predict
	 * 5. openHand:		pre-made button that will cause hand to open via svm_predict
	 */
	JButton set_data;
	JButton svm_path;
	JButton set_model;
	JButton set_sample;
	JButton set_profile;
	JButton train;
	JButton load_arm;
	JButton closeHand;
	JButton openHand;
	JButton runHand;
	JTextField dataPath;
	JTextField modelPath;
	JTextField samplePath;
	JTextField profilePath;
	
	boolean dataIsSet;
	boolean modelIsSet;
	boolean sampleIsSet;
	boolean profileIsSet;
	
	//Object through which commands can be sent to Arduino
	ArduinoArm hand;
	
	Profile profile;
	
	public train_and_predict(){
		dataIsSet = false;
		modelIsSet = false;
		sampleIsSet = false;
		profileIsSet = false;
		profile = new Profile();
		//hand = new ArduinoArm();
		//hand.initialize();
		createGui();
	}
	
	private void createGui(){
		frame = new JFrame("train and predict");
		frame.setSize(440,235);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.fill = GridBagConstraints.HORIZONTAL;
		
		set_data = new JButton("Browse Data");
		dataPath = new JTextField("Training Data Directory Path",20);
		train = new JButton("Train");
		load_arm = new JButton("connect arm");
		set_model = new JButton("Browse Model");
		modelPath = new JTextField("Model Directory Path",20);
		set_sample = new JButton("Browse Sample");
		samplePath = new JTextField("Testing Sample File Path", 20);
		set_profile = new JButton("Browse Profile");
		profilePath = new JTextField("Profile File Path", 20);
		runHand = new JButton("Move Hand");
//		closeHand = new JButton("close hand");
//		openHand = new JButton("open hand");
		set_data.addActionListener(this);
		train.addActionListener(this);
		load_arm.addActionListener(this);
		set_model.addActionListener(this);
		set_sample.addActionListener(this);
		set_profile.addActionListener(this);
		runHand.addActionListener(this);
//		closeHand.addActionListener(this);
//		openHand.addActionListener(this);
		
		JLabel padding = new JLabel();
		padding.setPreferredSize(new Dimension (110,20));
		padding.setMaximumSize(new Dimension(110,20));
		padding.setMinimumSize(new Dimension(110,20));
		
		constraint.gridwidth = 2;
		constraint.gridx = 0;
		constraint.gridy = 0;
		panel.add(dataPath, constraint);
		constraint.gridwidth = 1;
		constraint.gridx = 2;
		constraint.gridy = 0;
		panel.add(set_data, constraint);
		constraint.gridx = 0;
		constraint.gridy = 1;
		panel.add(padding, constraint);
		constraint.gridx = 1;
		constraint.gridy = 1;
		panel.add(train, constraint);
		constraint.gridx = 1;
		constraint.gridy = 2;
		panel.add(load_arm, constraint);
		constraint.gridwidth = 2;
		constraint.gridx = 0;
		constraint.gridy = 3;
		panel.add(modelPath, constraint);
		constraint.gridwidth = 1;
		constraint.gridx = 2;
		constraint.gridy = 3;
		panel.add(set_model, constraint);
		constraint.gridwidth = 2;
		constraint.gridx = 0;
		constraint.gridy = 4;
		panel.add(profilePath, constraint);
		constraint.gridwidth = 1;
		constraint.gridx = 2;
		constraint.gridy = 4;
		panel.add(set_profile, constraint);
		constraint.gridwidth = 2;
		constraint.gridx = 0;
		constraint.gridy = 5;
		panel.add(samplePath, constraint);
		constraint.gridwidth = 1;
		constraint.gridx = 2;
		constraint.gridy = 5;
		panel.add(set_sample, constraint);
		constraint.gridx = 1;
		constraint.gridy = 6;
		panel.add(runHand, constraint);
		
		frame.add(panel);
		
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();
		
		//opens file choosers to save directory paths
		if (holder == set_data || holder == set_model || holder == set_sample || holder == set_profile){
			JFileChooser chose = new JFileChooser(new File(System.getProperty("user.home") + System.getProperty("file.seperator")+"Desktop"));
			chose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int result = chose.showSaveDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION){
				if(holder == set_data){
					dataPath.setText(chose.getSelectedFile().getAbsolutePath());
					dataIsSet = true;
				}
				else if(holder == set_model){
					modelPath.setText(chose.getSelectedFile().getAbsolutePath());
					modelIsSet = true;
				}
				else if(holder == set_profile){
					profilePath.setText(chose.getSelectedFile().getAbsolutePath());
					Profile.load_profile(profilePath.getText(), profile);
					profileIsSet = true;
				}
				else{
					samplePath.setText(chose.getSelectedFile().getAbsolutePath());
					sampleIsSet = true;
				}
			}
		}
		
		//calls new process to run svm_train
		if (holder == train){
			if (!dataIsSet)
				System.out.println("error: Training Data Directory Path is not set");
			else{
				System.out.println("started training proccess...");
				svm_train.run_Directory(dataPath.getText(), null);
				System.out.println("\ntraining proccess complete");
			}
		}
		
		if(holder == load_arm){
			hand = new ArduinoArm();
			hand.initialize();
		}
		
		if(holder == runHand){
			if(!modelIsSet){
				System.err.println("error: Model Directory Path is not set");
			}
			else if(!sampleIsSet){
				System.err.println("error: Sample File Path is not set");
			}
			else if(!profileIsSet){
				System.err.println("error: Profile File Path is not set");
			}
			else if(hand == null){
				System.err.print("hand is not connected");
			}
			else{
				try{
					int counter = 0;
					File sample_dir = new File(samplePath.getText());
					File[] tests = sample_dir.listFiles();
					double[] modelDecisions = new double[tests.length];
					for(File test_channel: tests){
						String[] argv = new String[2];
						argv[0] = test_channel.getAbsolutePath();
						argv[1] = modelPath.getText()+"\\"+test_channel.getName()+".model";
						modelDecisions[counter] = svm_predict.get_Predictions(argv)[0];
						counter++;
					}
					System.out.println(Arrays.toString(modelDecisions));
					hand.writeMessage(profile.makeDecision(modelDecisions));
					try{
						Thread.sleep(2000);
					}catch(Exception q){q.printStackTrace();}
					hand.writeMessage(0);
				}catch(IOException p){p.printStackTrace();}
			}
		}
	}
	
	//main function for testing purposes
	public static void main(String[] args){
		URL path = train_and_predict.class.getResource("BCItester.txt");
		System.out.println(path.getFile());
		new train_and_predict();
	}
}
