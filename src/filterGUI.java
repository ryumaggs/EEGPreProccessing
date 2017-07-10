import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class filterGUI extends Frame implements ActionListener{

	private JButton dataBrowser;
	private JButton runFilter;
	private JButton outputBrowser;
	private JButton runOpenEXP;
	private JButton runClosedEXP;
	private JButton runTrainingGUI;
	
	private static String datalocString; // save a directory
	private static String outputlocString;
	
	private JTextField datalocation;
	private JTextField outputlocation;

	
	JPanel panel;
	JFrame frame;
	
	/*
	 * Set up the JFrame which should contain:
	 * button for the trial data selection
	 * button for destination selection
	 */
	public filterGUI(){
		//Set up the JFrame
		frame = new JFrame("EEG Filter");
		frame.setSize(480,225);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		JPanel innerpanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.fill = GridBagConstraints.HORIZONTAL;
		
		runFilter = new JButton("Run Mu Filter");
		dataBrowser = new JButton("Browse Data");
		outputBrowser = new JButton("Browse Output");
		runTrainingGUI = new JButton("Run Training and Prediction");
		runOpenEXP = new JButton("Run Experiment: Open Hand");
		runClosedEXP = new JButton("Run Experiment: Close Hand");

		datalocation = new JTextField("Path for directory containing all trials", 20);
		outputlocation = new JTextField("Path for destination directory", 20);
		
		runFilter.addActionListener(this);
		dataBrowser.addActionListener(this);
		outputBrowser.addActionListener(this);
		runOpenEXP.addActionListener(this);
		runClosedEXP.addActionListener(this);
		runTrainingGUI.addActionListener(this);

		//Add everything to the JFrame
		JLabel padding = new JLabel();
		padding.setPreferredSize(new Dimension (110,20));
		padding.setMaximumSize(new Dimension(110,20));
		padding.setMinimumSize(new Dimension(110,20));

		constraint.gridwidth = 2;
		constraint.gridx = 0;
		constraint.gridy = 1;
		innerpanel.add(datalocation, constraint);
		constraint.gridwidth = 1;
		constraint.gridx = 2;
		constraint.gridy = 1;
		innerpanel.add(dataBrowser, constraint);
		constraint.gridwidth = 2;
		constraint.gridx = 0;
		constraint.gridy = 2;
		innerpanel.add(outputlocation, constraint);
		constraint.gridwidth = 1;
		constraint.gridx = 2;
		constraint.gridy = 2;
		innerpanel.add(outputBrowser, constraint);
		constraint.gridx = 0;
		constraint.gridy = 3;
		innerpanel.add(padding, constraint);
		constraint.gridx = 1;
		constraint.gridy = 3;
		innerpanel.add(runFilter, constraint);
		constraint.gridx = 1;
		constraint.gridy = 4;
		innerpanel.add(runTrainingGUI, constraint);
		constraint.gridx = 1;
		constraint.gridy = 5;
		innerpanel.add(runOpenEXP, constraint);
		constraint.gridx = 1;
		constraint.gridy = 6;
		innerpanel.add(runClosedEXP, constraint);
		constraint.gridx = 1;
		constraint.gridy = 7;
		panel.add(innerpanel,BorderLayout.CENTER);
		
		frame.add(panel);

		//Show Frame
		frame.setVisible(true);
	}
	
	//returns the destination path
	public static String getDestPath(){
			return outputlocString;
	}
	
	/*
	 * Open up directory selector by pressing outputBrowser or Destination
	 * Navigate to the file or directory
	 * Press Save
	 * And it saves the path into a String
	 */
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();

		if (holder == dataBrowser || holder == outputBrowser){
			JFileChooser chose = new JFileChooser(new File(System.getProperty("user.home") + System.getProperty("file.seperator")+"Desktop"));
			chose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int result = chose.showSaveDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION){
				if (holder == dataBrowser){
					datalocation.setText(chose.getSelectedFile().getAbsolutePath());
					datalocString = datalocation.getText();
					//System.out.println(restTrialsDidatalocation);
				}
				else if (holder == outputBrowser){
					outputlocation.setText(chose.getSelectedFile().getAbsolutePath());
					outputlocString = outputlocation.getText();
				}
			}
			else if (result == JFileChooser.CANCEL_OPTION){
				//System.out.println("Cancel was selected");
			}
		}
		else if (holder == runFilter){
			if (datalocString != null)
				new RawFilter(datalocString,outputlocString, 8);
			else
				System.out.println("filepath was not entered");
		}
		
		//frame.setVisible(true);
		
		if(holder == runTrainingGUI){
			new train_and_predict();
			frame.dispose();
		}
		
		if (holder == runOpenEXP){
			String[] s = {"nothing in particular"};
			Experiment.main(s);
			frame.dispose();
			}
	}
	
	
	public static void main(String args[]){
		new filterGUI();

	}
}
