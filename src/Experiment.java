import java.awt.*;
import java.awt.event.*;
import java.io.*;
import sun.audio.*;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;


public class Experiment implements ActionListener,Runnable{
	private ImageIcon[] Images;
	private Clip[] soundClips;
	private JFrame buildStage;
	private JPanel panel;
	private JLabel label;
	private JButton begin;
	private DataStreaming netStream;
	private OutputStream output;
	
	private JTextField image_folder_path;
	private JTextField output_folder_path;
	private JTextField com_port;
	
	public Experiment(){

	}
	
	public Experiment(String photoDir, String outputfile, String port){		
		//netStream = new DataStreaming();
		//netStream.setupconnection(outputfile, port);
		//output = netStream.getOutputWriter();
		//System.out.println("Datastream Initializing...");
		soundClips = new Clip[7];
		try{
//			output.write('v');
//			Thread.sleep(2000);
//			output.write('b');
//			System.out.println("Datastream started");
			loadImages(photoDir);
			loadAudio();
		}catch(Exception e){e.printStackTrace();}
//		
//		for(int i = 0; i < soundClips.length; i++){
//			playAudio(i);
//		}
		//System.exit(1);
		//set up the frame for image slide show
		buildStage = new JFrame("Experiment");
		buildStage.setSize(1000, 1000);
		buildStage.setExtendedState(JFrame.MAXIMIZED_BOTH);
		buildStage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		label = new JLabel();
		panel.add(label,BorderLayout.CENTER);
		buildStage.add(panel,BorderLayout.CENTER);
		buildStage.setVisible(true);
		
		//set up audio files
		
	}
	
	public void scroll_images(){
		int currentImageIndex = 0;
		int imagesPlayed_counter = 0;
		label.setIcon(null);
		try{
			Thread.sleep(1000);
			while(true){
				label.setIcon(Images[currentImageIndex]);
				playAudio(currentImageIndex);
				//Thread.sleep(1000);
				currentImageIndex++;
				
				if(currentImageIndex >= Images.length)
					currentImageIndex = 0;
				
				imagesPlayed_counter++;
				
				label.setIcon(null);
				Thread.sleep(2000);
				
				if (imagesPlayed_counter == 64){
					break;
				}
			}
		}catch(Exception ep){ep.printStackTrace();}
		netStream.endstream();
		System.out.print("experiment complete");
		System.exit(1);
	}
	
	public void loadImages(String photoDir)throws IOException{
		File dir = new File(photoDir);
		File[] directoryListing = dir.listFiles();
		Images = new ImageIcon[directoryListing.length];
		for (int i = 0; i < directoryListing.length; i++){
			Images[i] = new ImageIcon(directoryListing[i].getPath());
		}
	}
	
	public void loadAudio(){
		try {
			File directory = new File("C://Users//Ryan Yu//workspace//ImportantFreq//src//SoundFiles");
			File[] dir = directory.listFiles();
			int count = 0;
			Clip tempClip;
			for(File file:dir){
				tempClip= AudioSystem.getClip();
				tempClip.open(AudioSystem.getAudioInputStream(file));
				soundClips[count] = tempClip;
				count++;
			}
//			Clip clip = AudioSystem.getClip();
//			clip.open(AudioSystem.getAudioInputStream(file));
//			System.out.println("before play");
//			clip.start();
//			Thread.sleep(clip.getMicrosecondLength()/1000+500);
//			System.out.println("after play");
//			} catch (Exception murle) {
//			System.out.println(murle);
		}catch(Exception e){e.printStackTrace();}
	}
	public void playAudio(int file_num){ //need to make an array of these. what data type are they??
		soundClips[file_num].start();
		try{
			Thread.sleep(soundClips[file_num].getMicrosecondLength()/1000+100);
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void run(){
		scroll_images();
	}
	
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();
		
		if (holder == begin){
			String image_dir = image_folder_path.getText();
			String outputfile = output_folder_path.getText();
			String port = com_port.getText();
			if(image_dir.equals("") || outputfile.equals("") || port.equals("")){
				if(image_folder_path.getText().equals("")){
					System.out.println("Missing path to image directory");
				}
				else if (outputfile.equals("")){
					System.out.println("Missing filename for output data");
				}
				else{
					System.out.println("Missing portname for OPENBCI");
				}
			}
			//need to start the image rotation on separate thread or else image changes are not visible
			else{
				Experiment images = new Experiment(image_dir, outputfile, port);
				Thread t = new Thread(images);
				t.start();
				(SwingUtilities.windowForComponent(begin)).dispose();
			}
		}
	}
	
	//function to load an intermediate menu that will contain 'begin' button
	public void load_menu(){
		buildStage = new JFrame("Experiment");
		buildStage.setSize(290,220);
		buildStage.setLocationRelativeTo(null);
		buildStage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		begin = new JButton("Begin");
		begin.addActionListener(this);
		panel = new JPanel();
		
		JLabel dirlab = new JLabel("Path to Image Directory: ");
		JLabel deslab = new JLabel("Filename for Output Data: ");
		JLabel portlab = new JLabel("Portname for OPENBCI: ");
		image_folder_path = new JTextField("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\src\\ExperimentPhotos\\HC",20);
		output_folder_path = new JTextField("CLOSED.txt",20);
		com_port = new JTextField("COM4", 5);
		
		panel.add(dirlab);
		panel.add(image_folder_path);
		panel.add(deslab);
		panel.add(output_folder_path);
		panel.add(portlab);
		panel.add(com_port);
		panel.add(begin);
		
		buildStage.add(panel,BorderLayout.CENTER);
		buildStage.setVisible(true);
	}
	
	//used to load the menu
	public static void main(String args[]){
		Experiment menu = new Experiment();
		menu.load_menu();
	}
	
}