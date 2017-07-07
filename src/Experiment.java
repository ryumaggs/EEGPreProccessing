import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;


public class Experiment implements ActionListener,Runnable{
	/*
	 * Field definitions:
	 * trialImages - ImageIcon array which contains the images you want to scroll through
	 * buildStage - Experimental frame for images (panel, label)
	 * begin - button to begin data recording and image scrolling
	 * netStream - Object used for data streaming
	 * lock - concurrency object that will prevent data to be written between image change and file mark
	 */
	private ImageIcon[] trialImages;
	private JFrame buildStage;
	private JPanel panel;
	private JLabel label;
	private Button begin;
	private DataStreaming netStream;
	private OutputStream output;
	private static Lock lock;
	
	private TextField dirtxt;
	private TextField destxt;
	private TextField porttxt;
	
	//blank constructor to instantiate the menu
	public Experiment(){

	}
	
	/*
	 * Actual constructor that will load images, set up the JFrame, and start the data stream
	 * Parameters: photos - Directory file path to the images you want to cycle through
	 * 
	 */
	public Experiment(String photoDir, String outputfile, String port){
		lock = new Lock();
		
		//set up datastreaming object
		netStream = new DataStreaming();
		netStream.setupconnection(outputfile, port);
		output = netStream.getOutputWriter();
		System.out.println("Datastream Initializing...");
		
		//reset and start the serial port reading
		try{
			output.write('v');
			Thread.sleep(2000);
			output.write('b');
			System.out.println("Datastream started");
			loadImages(photoDir);
		}catch(Exception e){e.printStackTrace();}
		
		//set up the frame for image slideshow
		buildStage = new JFrame("Experiment");
		buildStage.setSize(1000, 1000);
		buildStage.setExtendedState(JFrame.MAXIMIZED_BOTH);
		buildStage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		label = new JLabel();
		panel.add(label,BorderLayout.CENTER);
		buildStage.add(panel,BorderLayout.CENTER);
		buildStage.setVisible(true);
	}
	
	//Returns the lock initialized in the Experiment object
	//Is called in DataStreaming.java
	public static Lock get_lock(){
		return lock;
	}
	
	/*Loop function whos orders:
	 * Blank (2 seconds)
	 * Image(2 seconds) - prompt
	 * Blank (3 seconds)
	 * Image(3 seconds) - experimental
	 * blank(2 seconds)
	 */
	public void scroll_images(){
		int current = 0;
		int stopped = 0;
		label.setIcon(null);
		try{
		Thread.sleep(2000);
		}catch(InterruptedException e){e.printStackTrace();}
		while(true){
			try{
				label.setIcon(trialImages[current]);
				Thread.sleep(2000);
				label.setIcon(null);
				Thread.sleep(3000);
				lock.lock();
				label.setIcon(trialImages[current]);
				netStream.write2file("MARKER", true);
				lock.unlock();
				Thread.sleep(3000);
			}catch(Exception e){e.printStackTrace();}
			current++;
			if(current >= trialImages.length)
				current = 0;
			stopped++;
			label.setIcon(null);
			if (stopped == 8){
				break;
			}
			try{
				Thread.sleep(2000);
			}catch(InterruptedException ep){System.out.println("erorro");}
		}
		netStream.endstream();
	}
	
	/*Function to load images into an ImageIcon array
	 * Parameter: photo - Directory file path for the photos
	 * Output: initializes each element in the ImageIcon array, trialImages, with new ImageIcon
	 */
	public void loadImages(String photoDir)throws IOException{
		File dir = new File(photoDir);
		File[] directoryListing = dir.listFiles();
		trialImages = new ImageIcon[directoryListing.length];
		for (int i = 0; i < directoryListing.length; i++){
			trialImages[i] = new ImageIcon(directoryListing[i].getPath());
		}
	}
	
	/*
	 * Function that thread calls to scroll the images
	 */
	public void run(){
		scroll_images();
	}
	
	//Allows the 'begin' button to work
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();
		if (holder == begin){
			String dir = dirtxt.getText();
			String outputfile = destxt.getText();
			String port = porttxt.getText();
			if(dir.equals("") || outputfile.equals("") || port.equals("")){
				if(dirtxt.getText().equals("")){
					System.out.println("Missing path to image directory");
				}
				else if (outputfile.equals("")){
					System.out.println("Missing filename for output data");
				}
				else{
					System.out.println("Missing portname for OPENBCI");
				}
			}
			else{
				Experiment images = new Experiment(dir, outputfile, port);
				Thread t = new Thread(images);
				t.start();
				(SwingUtilities.windowForComponent(begin)).dispose();
			}
		}
	}
	
	//function to load an intermediate menu that will contain 'begin' button
	public void load_menu(){
		buildStage = new JFrame("Experiment");
		buildStage.setSize(250,250);
		buildStage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		begin = new Button("Begin");
		begin.addActionListener(this);
		panel = new JPanel();
		
		JLabel dirlab = new JLabel("Path to Image Directory: ");
		JLabel deslab = new JLabel("Filename for Output Data: ");
		JLabel portlab = new JLabel("Portname for OPENBCI: ");
		dirtxt = new TextField("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\src\\ExperimentPhotos\\HC",30);
		destxt = new TextField("CLOSED.txt",30);
		porttxt = new TextField("COM4", 5);
		
		panel.add(dirlab);
		panel.add(dirtxt);
		panel.add(deslab);
		panel.add(destxt);
		panel.add(portlab);
		panel.add(porttxt);
		panel.add(begin);
		
		buildStage.add(panel,BorderLayout.CENTER);
		buildStage.setVisible(true);
	}
	
	public static void main(String args[]){
		Experiment menu = new Experiment();
		menu.load_menu();
	}
	
}