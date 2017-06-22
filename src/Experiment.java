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
	
	public Experiment(){
		//blank creator for menu
	}
	public Experiment(String photos){
		try{
		loadImages(photos);
		}catch(IOException e){e.printStackTrace();}
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
	
	public void scroll_images(){
		//DataStreaming teststream = new DataStreaming();
		//teststream.setupconnection("Newtester.txt");
		int current = 0;
		int stopped = 0;
		label.setIcon(null);
		try{
		Thread.sleep(2000);
		}catch(InterruptedException ep){ep.printStackTrace();}
		while(true){
			//System.out.println("entered the experiment loop");
			current = nextImage(current);
			stopped++;
			if (stopped == 10)
				break;
			try{
				Thread.sleep(4000);
			}catch(InterruptedException ep){System.out.println("erorro");}
			label.setIcon(null);
			try{
			Thread.sleep(2000);
			}catch(InterruptedException ep){ep.printStackTrace();}
		}
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
	
	public int nextImage(int cur){
		//System.out.println("looking at image nmber: " + cur);
		if (cur == frames.length-1)
			cur = 0;
		else 
			cur +=1;
	
		label.setIcon(frames[cur]);
		return cur;
		
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