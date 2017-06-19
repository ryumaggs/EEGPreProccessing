import java.util.*;
import java.lang.Runtime;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class train_and_predict extends Frame implements ActionListener{
	JFrame frame;
	JPanel panel;
	String training_file_path;
	String svm_dir_path;
	String model_path;
	Button set_path;
	Button svm_path;
	Button train;
	Button predict;
	
	public train_and_predict(){
		training_file_path = "";
		frame = new JFrame("train and predict");
		frame.setSize(400,200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		FlowLayout GUILayout = new FlowLayout();
		panel.setLayout(GUILayout);
		GUILayout.setAlignment(FlowLayout.TRAILING);
		
		set_path = new Button("set directory path for training data");
		svm_path = new Button("set directory path for SVM");
		train = new Button("train");
		predict = new Button("predict(premade)");
		set_path.addActionListener(this);
		svm_path.addActionListener(this);
		train.addActionListener(this);
		predict.addActionListener(this);
		
		panel.add(set_path);
		panel.add(svm_path);
		panel.add(train);
		panel.add(predict);
		
		frame.add(panel);
		
		frame.setVisible(true);
		
	}
	
	public void actionPerformed(ActionEvent e){
		Object holder = e.getSource();
		if (holder == set_path || holder == svm_path){
			JFileChooser chose = new JFileChooser(new File(System.getProperty("user.home") + System.getProperty("file.seperator")+"Desktop"));
			chose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int result = chose.showSaveDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION){
				if (holder == set_path){
					model_path = chose.getSelectedFile().getAbsolutePath();
					training_file_path = add_quotes(chose.getSelectedFile().getAbsolutePath());
				}
				else if (holder == svm_path)
					svm_dir_path = add_quotes(chose.getSelectedFile().getAbsolutePath());
			}
		}
		System.out.println(training_file_path + " : " + svm_dir_path);
		if (holder == train){
			if (svm_path.equals("") || set_path.equals(""))
				System.out.print("error: paths are not both set");
			else{
				try{
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd "+svm_dir_path+" && java -classpath libsvm.jar svm_train " +training_file_path);
		        builder.redirectErrorStream(true);
		        Process p = builder.start();
		        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        String line;
		        while (true) {
		            line = r.readLine();
		            if (line == null) { break; }
		            System.out.println(line);
		        }
		        model_path = add_quotes(model_path + ".model");
		        System.out.println("model path should be: " + model_path);
			}catch(IOException p){System.out.print("erororororor");}
			}
		}
		
		if (holder == predict){
			try{
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_predict \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\singleExec.txt\" \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\svm_testData.txt.model\" \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\Output.txt\"");
				builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String line;
				while(true){
					line = r.readLine();
					System.out.println(line);
					if (line == null) {break;}
					if (line.equals("V from the prediction is: 1.0"))
						System.out.println("got it");
				}
			} catch(IOException ep){System.out.println("heorororro");}
		}
	}
	public String add_quotes(String p){
		String w = "\""+p+"\"";
		return w;
	}
	
	public static void main(String[] args){
		new train_and_predict();
	}
}
