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
	 * frames - ImageIcon array which contains the images you want to scroll through
	 * exp2 - Experimental frame for images (panel, label)
	 * begin - button to begin data recording and image scrolling
	 * netStream - Object used for data streaming
	 * lock - concurrency object that will prevent data to be written betwen image change and file mark
	 */
	private ImageIcon[] frames;
	private JFrame exp2;
	private JPanel panel;
	private JLabel label;
	private Button begin;
	private DataStreaming netStream;
	private OutputStream output;
	private static Lock lock;
	
	//blank constructor to instantiate the menu
	public Experiment(){

	}
	
	/*
	 * Actual constructor that will load images, set up the JFrame, and start the data stream
	 * Parameters: photos - Directory file path to the images you want to cycle through
	 * 
	 */
	public Experiment(String photos){
		lock = new Lock();
		
		//set up datastreaming object
		netStream = new DataStreaming();
		netStream.setupconnection("CLOSED6.txt");
		output = netStream.getWriter();
		System.out.println("datastream initialized");
		
		//reset and start the serial port reading
		try{
			output.write('v');
			Thread.sleep(2000);
			output.write('b');
			System.out.println("Stream reset and started");
			loadImages(photos);
		}catch(Exception e){e.printStackTrace();}
		
		//set up the frame for image slideshow
		exp2 = new JFrame("Experiment");
		exp2.setSize(1000, 1000);
		exp2.setExtendedState(JFrame.MAXIMIZED_BOTH);
		exp2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		label = new JLabel();
		panel.add(label,BorderLayout.CENTER);
		exp2.add(panel,BorderLayout.CENTER);
		exp2.setVisible(true);
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
				label.setIcon(frames[current]);
				Thread.sleep(2000);
				label.setIcon(null);
				Thread.sleep(3000);
				lock.lock();
				label.setIcon(frames[current]);
				netStream.write2file("CHANGED MY IMAGE HERE BOYS", true);
				lock.unlock();
				Thread.sleep(3000);
			}catch(Exception e){e.printStackTrace();}
			current++;
			if(current >= frames.length)
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
	 * Output: initializes each element in the ImageIcon array, frames, with new ImageIcon
	 */
	public void loadImages(String photo)throws IOException{
		File dir = new File(photo);
		File[] directoryListing = dir.listFiles();
		frames = new ImageIcon[directoryListing.length];
		for (int i = 0; i < directoryListing.length; i++){
			frames[i] = new ImageIcon(directoryListing[i].getPath());
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
			Experiment images = new Experiment("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\src\\ExperimentPhotos\\HC");
			Thread t = new Thread(images);
			t.start();
			(SwingUtilities.windowForComponent(begin)).dispose();
		}
	}
	
	//function to load an intermediate menu that will contain 'begin' button
	public void load_menu(){
		exp2 = new JFrame("Experiment");
		exp2.setSize(1000, 1000);
		exp2.setExtendedState(JFrame.MAXIMIZED_BOTH);
		exp2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		begin = new Button("Begin");
		begin.addActionListener(this);
		panel = new JPanel();
		panel.add(begin);
		exp2.add(panel,BorderLayout.CENTER);
		exp2.setVisible(true);
	}
	
	public static void main(String args[]){
		Experiment menu = new Experiment();
		menu.load_menu();
	}
	
}