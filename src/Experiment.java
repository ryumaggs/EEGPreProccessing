import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;


public class Experiment implements ActionListener,Runnable{
	private ImageIcon[] frames;
	private JFrame exp2;
	private JPanel panel;
	private JLabel label;
	private Button begin;
	private DataStreaming netStream;
	private OutputStream output;
	
	private static Lock lock;
	
	public Experiment(){
		//blank creator for menu
	}
	public Experiment(String photos){
		lock = new Lock();
		netStream = new DataStreaming();
		System.out.println("Setting up datastream");
		netStream.setupconnection("Newtester.txt");
		output = netStream.getWriter();
		//dataHolder = netStream.getArrayList();
		System.out.println("datastream initialized");
		try{
		output.write('v');
		Thread.sleep(2000);
		output.write('b');
		System.out.println("Stream reset and started");
		loadImages(photos);
		}catch(Exception e){e.printStackTrace();}
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
	
	public static Lock get_lock(){
		return lock;
	}
	
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
			stopped++;
			if (stopped == 15)
				break;
			label.setIcon(null);
			try{
				Thread.sleep(2000);
			}catch(InterruptedException ep){System.out.println("erorro");}
		}
		netStream.endstream();
	}
	public void loadImages(String photo)throws IOException{
		File dir = new File(photo);
		File[] directoryListing = dir.listFiles();
		frames = new ImageIcon[directoryListing.length];
		for (int i = 0; i < directoryListing.length; i++){
			frames[i] = new ImageIcon(directoryListing[i].getPath());
		}
	}
	
	public void run(){
		scroll_images();
	}
	
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();
		if (holder == begin){
			Experiment images = new Experiment("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\src\\ExperimentPhotos\\HC");
			Thread t = new Thread(images);
			t.start();
			(SwingUtilities.windowForComponent(begin)).dispose();
		}
	}
	
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