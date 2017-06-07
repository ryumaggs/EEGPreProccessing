import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class filterGUI extends Frame implements ActionListener{

	private Button remTrials;
	private Button go;
	private Button destination;
	private Button expOne;
	private Button expTwo;
	//swtich these to experiment.java
	//---------------------------
	private Button desti;
	private Button pictures;
	private Button gogo;
	private static String expPicPath;
	private static String savePath;
	//--------------------------
	
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
		frame.setSize(400,200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		FlowLayout GUILayout = new FlowLayout();
		panel.setLayout(GUILayout);
		GUILayout.setAlignment(FlowLayout.TRAILING);
		go = new Button("Run Mu Filter");
		remTrials = new Button("Remaining Trials");
		destination = new Button("Destination Folder");
		expOne = new Button("Run Experiment: Open Hand");
		expTwo = new Button("Run Experiment: Two");

		rpath = new TextField("Path for directory containing all trials");
		dpath = new TextField("Path for destination directory");
		
		go.addActionListener(this);
		remTrials.addActionListener(this);
		destination.addActionListener(this);
		expOne.addActionListener(this);
		expTwo.addActionListener(this);

		//Add everything to the JFrame
		panel.add(rpath);
		panel.add(dpath);
		panel.add(go);
		panel.add(destination);
		panel.add(remTrials);
		panel.add(expOne);
		panel.add(expTwo);
		panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		frame.add(panel);
		
		//Show Frame
		//frame.pack();
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
		else if (holder == expOne || holder == expTwo)
			runExperiment();
		
		//Move this to experiment.java
		else if (holder == pictures || holder == desti){
			JFileChooser chose2 = new JFileChooser(new File(System.getProperty("user.home") + System.getProperty("file.seperator")+"Desktop"));
			chose2.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int result = chose2.showSaveDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION){
				if (holder == pictures){
					expPicPath = chose2.getSelectedFile().getAbsolutePath();
				}
				else if (holder == desti){
					savePath = chose2.getSelectedFile().getAbsolutePath();
				}
			}
			else if (result == JFileChooser.CANCEL_OPTION){
				System.out.println("Cancel was selected");
			}
		}
		else if (holder == gogo){
			System.out.println("begining experiment");
			beginExp();
			}
		//frame.add(panel);
		//frame.pack();
		frame.setVisible(true);
		
	}
	public void runExperiment(){
		frame.setState(Frame.ICONIFIED);
		JFrame exp = new JFrame("herro");
		exp.setLocationRelativeTo(null);
		exp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel eegExperiment = new JPanel();
		FlowLayout experimentLayout = new FlowLayout();
		
		eegExperiment.setLayout(experimentLayout);
		eegExperiment.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		//move buttons to parameter of experiment
		desti = new Button("Destination Foler");
		pictures = new Button("Picture Folder");
		gogo = new Button("begin");
		
		desti.addActionListener(this);
		pictures.addActionListener(this);
		gogo.addActionListener(this);
		
		eegExperiment.add(desti);
		eegExperiment.add(pictures);
		eegExperiment.add(gogo);
		
		exp.add(eegExperiment);
		exp.pack();
		exp.setVisible(true);
	}
	
	public void beginExp(){
		JFrame exp2 = new JFrame("asijaisj");
		exp2.setSize(1000, 1000);
		JPanel tester = new JPanel(new BorderLayout());
		
		System.out.println("expPicPath");
		ImageIcon image = new ImageIcon(expPicPath);
		JLabel label = new JLabel("", image, JLabel.CENTER);
		tester.add(label, BorderLayout.CENTER );
		
		exp2.setExtendedState(JFrame.MAXIMIZED_BOTH);
		exp2.add(tester);
		exp2.setUndecorated(true);
		exp2.setVisible(true);
		
		scrollImages();
	}
	
	public void scrollImages(){
		//nothing
		
	}
	
	
	public static void main(String args[]){
		new filterGUI();

	}
}
