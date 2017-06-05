import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class filterGUI extends Frame implements ActionListener{

	private Button remTrials;
	private Button go;
	private Button destination;

	private static String restTrialsDirPath; // save a directory
	private static String destinationPath;
	
	private TextField rpath;
	private TextField dpath;

	
	JPanel panel;
	JFrame frame;
	public filterGUI(){
		/*
		 * Set up the JFrame which should contain:
		 * button for the trial data selection
		 * button for destination selection
		 */

		
		//Set up the JFrame
		frame = new JFrame("EEG Filter");
		frame.setSize(300,300);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		go = new Button("Run Mu Filter");
		remTrials = new Button("Remaining Trials");
		destination = new Button("Destination Folder");

		rpath = new TextField("Path for directory containing all trials");
		dpath = new TextField("Path for destination directory");
		
		go.addActionListener(this);
		remTrials.addActionListener(this);
		destination.addActionListener(this);

		//Add everything to the JFrame
		panel.add(rpath);
		panel.add(dpath);
		panel.add(go);
		panel.add(destination);
		panel.add(remTrials);
		frame.add(panel);
		
		//Show Frame
		frame.pack();
		frame.setVisible(true);
	}
	
	//returns the destination path
	public static String getDestPath(){
			return destinationPath;
	}

	public void actionPerformed(ActionEvent e){
		/*
		 * Open up directory selector by pressing remTrials or Destination
		 * Navigate to the file or directory
		 * Press Save
		 * And it saves the path into a String
		 */
		Object holder = e.getSource();

		if (holder == remTrials || holder == destination){
			JFileChooser chose = new JFileChooser(new File(System.getProperty("user.home") + System.getProperty("file.seperator")+"Desktop"));
			chose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int result = chose.showSaveDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION){
				if (holder == remTrials){
					rpath.setText(chose.getSelectedFile().getAbsolutePath());
					restTrialsDirPath = rpath.getText();
					//System.out.println(restTrialsDirPath);
				}
				else if (holder == destination){
					dpath.setText(chose.getSelectedFile().getAbsolutePath());
					destinationPath = dpath.getText();
				}
			}
			else if (result == JFileChooser.CANCEL_OPTION){
				System.out.println("Cancel was selected");
			}
		}
		else if (holder == go){
			if (restTrialsDirPath != null)
				new RawFilter(restTrialsDirPath,destinationPath, 8,16);
			else
				System.out.println("no filepath");
		}
		//frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
	}
	public static void main(String args[]){
		new filterGUI();

	}
}
