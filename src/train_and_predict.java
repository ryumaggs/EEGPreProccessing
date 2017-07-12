import java.util.*;
import java.lang.Runtime;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class train_and_predict extends Frame implements ActionListener{
	//Containers for buttons
	JFrame frame;
	JPanel panel;
	
	/*
	 * String files that will contain file paths:
	 * 1. training_file_path:DIRECTORY path for all of the training data
	 * 2. svm_dir_path:      DIRECTORY path for the support vector machine (libsvm)
	 */
	String training_file_path;
	String svm_dir_path;
	
	/*
	 * Buttons for GUI:
	 * 1. set_path: 	Button that will allow user to select directory containing training data
	 * 2. svm_path: 	Button that allows user to set directory path for the SVM
	 * 3. train: 		once 1 and 2 have been set, will create a seperate proccess to run the svm training
	 * 4. closeHand: 	pre-made button that will cause hand to close via svm_predict
	 * 5. openHand:		pre-made button that will cause hand to open via svm_predict
	 */
	JButton set_path;
	JButton svm_path;
	JButton train;
	JButton closeHand;
	JButton openHand;
	
	//Object through which commands can be sent to Arduino
	ArduinoArm hand;
	
	public train_and_predict(){
		training_file_path = "";
		//hand = new ArduinoArm();
		//hand.initialize();
		
		frame = new JFrame("train and predict");
		frame.setSize(400,200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		FlowLayout GUILayout = new FlowLayout();
		panel.setLayout(GUILayout);
		GUILayout.setAlignment(FlowLayout.TRAILING);
		
		set_path = new JButton("set directory path for training data");
		svm_path = new JButton("set directory path for SVM");
		train = new JButton("train");
		closeHand = new JButton("close hand");
		openHand = new JButton("open hand");
		set_path.addActionListener(this);
		svm_path.addActionListener(this);
		train.addActionListener(this);
		closeHand.addActionListener(this);
		openHand.addActionListener(this);
		
		panel.add(set_path);
		panel.add(svm_path);
		panel.add(train);
		panel.add(closeHand);
		panel.add(openHand);
		
		frame.add(panel);
		
		frame.setVisible(true);
		
	}
	
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();
		
		//opens file choosers to save directory paths
		if (holder == set_path || holder == svm_path){
			JFileChooser chose = new JFileChooser(new File(System.getProperty("user.home") + System.getProperty("file.seperator")+"Desktop"));
			chose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int result = chose.showSaveDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION){
				if (holder == set_path){
					training_file_path = chose.getSelectedFile().getAbsolutePath();
				}
				else if (holder == svm_path)
					svm_dir_path = add_quotes(chose.getSelectedFile().getAbsolutePath());
			}
		}
		
		//calls new process to run svm_train
		if (holder == train){
			if (svm_path.equals("") || set_path.equals(""))
				System.out.print("error: paths are not both set");
			else{
				System.out.println("started training proccess...\n");
				File dir = new File(training_file_path);
				File[] channels = dir.listFiles();
				try{
					for(File file : channels){
						ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd "+svm_dir_path+" && java -classpath libsvm.jar svm_train -t 0 " + add_quotes(file.getAbsolutePath()));
						builder.redirectErrorStream(true);
						Process p = builder.start();
						System.out.println("finished training for: " + file.getName());
		        }
				System.out.println("training proccess complete");
			}catch(IOException p){p.printStackTrace();}
			}
		}
		
		//need to adjust this so that it predicts on two different known files
		if (holder == closeHand || holder == openHand){
			try{
				int writeMes = 0;
				if (holder == closeHand)
					writeMes = 1;
				//consider rearranging the next few lines to lower number of opening and closing of objects
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "java -classpath libsvm.jar svm_predict \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\singleExec.txt\" \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\svm_testData.txt.model\"");
				builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String line;
				while(true){
					line = r.readLine();
					if (line == null) {break;}
					if (line.equals("V from the prediction is: 1.0") || line.equals("V from the prediction is: -1.0")){
						hand.writeMessage(writeMes);
						break;
					}
				}
				r.close();
			} catch(IOException ep){ep.printStackTrace();}
		}
	}
	
	/*
	 * Windows CMD requires quotes around file paths so this function just adds quotes around a String
	 */
	public String add_quotes(String p){
		String w = "\""+p+"\"";
		return w;
	}
	//main function for testing purposes
	public static void main(String[] args){
		URL path = train_and_predict.class.getResource("BCItester.txt");
		System.out.println(path.getFile());
		new train_and_predict();
	}
}
