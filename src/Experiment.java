import java.awt.BorderLayout;
import java.io.*;
//import javax.comm.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
//import java.util.concurrent.TimeUnit;


public class Experiment{
	private static ImageIcon[] frames;
	private static JFrame exp2;
	private static JPanel[] imageDir;
	
	public static void beginExp(String photos){
		try{
		loadImages(photos);
		}catch(IOException e){e.printStackTrace();}
		
		exp2 = new JFrame("asijaisj");
		exp2.setSize(1000, 1000);
		exp2.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//exp2.setUndecorated(true);
		exp2.setVisible(true);
		int current = 0;
		int stopped = 0;
		while(true){
			current = nextImage(current);
			exp2.repaint();
			stopped++;
			if (stopped == 10)
				break;
			try{
				Thread.sleep(1000);
			}catch(InterruptedException ep){System.out.println("erorro");}
		}
	}
	
	public static void loadImages(String photo)throws IOException{
		File dir = new File(photo);
		File[] directoryListing = dir.listFiles();
		frames = new ImageIcon[directoryListing.length];
		System.out.println(frames.length + " is the length of frames");;
		imageDir = new JPanel[frames.length];
		System.out.println(imageDir.length + " is the length of imageDir");
		
		JLabel temp;
		for(int i = 0; i < directoryListing.length; i++){
			System.out.println("Currently looking at: " + directoryListing[i].getAbsolutePath());
			frames[i] = new ImageIcon(directoryListing[i].getAbsolutePath());
			temp = new JLabel("",frames[i],JLabel.CENTER);
			imageDir[i] = new JPanel();
			imageDir[i].add(temp);
			exp2.add(imageDir[i]);
		}
	}
	
	public static int nextImage(int cur){
		//exp2.removeAll();
		//exp2.revalidate();
		System.out.println("looking at image nmber: " + cur);
		if (cur == frames.length-1)
			cur = 0;
		else 
			cur +=1;
		
		imageDir[cur].setVisible(true);
		
		return cur;
		
	}
	
}



