import java.awt.BorderLayout;
import java.io.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;


public class Experiment{
	private static ImageIcon[] frames;
	private static JFrame exp2;
	private static JPanel panel;
	private static JLabel label;
	
	public Experiment(String photos){
		try{
		loadImages(photos);
		}catch(IOException e){e.printStackTrace();}
		
		exp2 = new JFrame("asijaisj");
		exp2.setSize(1000, 1000);
		exp2.setExtendedState(JFrame.MAXIMIZED_BOTH);
		exp2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		label = new JLabel();
		panel.add(label,BorderLayout.CENTER);
		exp2.add(panel,BorderLayout.CENTER);
		exp2.setVisible(true);
	}
	
	public static void loadImages(String photo)throws IOException{
		File dir = new File(photo);
		File[] directoryListing = dir.listFiles();
		frames = new ImageIcon[directoryListing.length];
		for (int i = 0; i < directoryListing.length; i++){
			frames[i] = new ImageIcon(directoryListing[i].getPath());
		}
	}
	
	public static int nextImage(int cur){
		System.out.println("looking at image nmber: " + cur);
		if (cur == frames.length-1)
			cur = 0;
		else 
			cur +=1;
	
		label.setIcon(frames[cur]);
		return cur;
		
	}
	
	public static void main(String args[]){
		new Experiment("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\src\\ExperimentPhotos\\HC");
		int current = 0;
		int stopped = 0;
		while(true){
			current = nextImage(current);
			stopped++;
			if (stopped == 10)
				break;
			try{
				Thread.sleep(4000);
			}catch(InterruptedException ep){System.out.println("erorro");}
		}
	}
	
}



