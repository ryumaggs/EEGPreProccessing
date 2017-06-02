import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class filterGUI extends Frame implements ActionListener{
	private Button base1;
	private Button base2;
	private Button remTrials;
	private Button go;

	private String base1path;
	private String base2path;
	private String restTrialsDirPath; // save a directory
	
	private TextField b1path = new TextField(10);
	private TextField b2path = new TextField(10);
	private TextField rpath = new TextField(10);

	
	JPanel panel;
	JFrame frame;
	public filterGUI(){
		/*
		 * Set up the JFrame which should contain:
		 * Base Trial 1 Button
		 * Base Trial 2 Button
		 * Shift select button for the rest of the trial data
		 */

		
		//Set up the JFrame
		frame = new JFrame("EEG Filter");
		frame.setSize(300,300);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		go = new Button("Run Mu Filter");
		base1 = new Button("Base1");
		base2 = new Button("Base2");
		remTrials = new Button("Remaining Trials");
		
		b1path = new TextField("Path for base 1");
		b2path = new TextField("Path for base 2");
		rpath = new TextField("Path for directory containing all trials");
		
		go.addActionListener(this);
		base1.addActionListener(this);
		base2.addActionListener(this);
		remTrials.addActionListener(this);

		//Add everything to the JFrame
		panel.add(b1path);
		panel.add(b2path);
		panel.add(rpath);
		panel.add(go);
		panel.add(base1);
		panel.add(base2);
		panel.add(remTrials);
		frame.add(panel);
		
		//Show Frame
		frame.pack();
		frame.setVisible(true);
	}
	

	public void actionPerformed(ActionEvent e){
		/*
		 * Open up directory selector by pressing any of the 3 buttons.
		 * Navigate to the file or directory
		 * Press Save
		 * And it saves the path into a String
		 */
		Object holder = e.getSource();

		if (holder == base1 | holder == base2 | holder == remTrials){
			JFileChooser chose = new JFileChooser();
			chose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
			int result = chose.showSaveDialog(this);
			if (result == JFileChooser.APPROVE_OPTION){
				if (e.getSource() == base1){
					b1path.setText(chose.getSelectedFile().getAbsolutePath());
					base1path = b1path.getText();
				}
				else if (e.getSource() == base2){
					b2path.setText(chose.getSelectedFile().getAbsolutePath());
					base2path = b2path.getText();
				}
				else if (e.getSource() == remTrials){
					rpath.setText(chose.getSelectedFile().getAbsolutePath());
					restTrialsDirPath = rpath.getText();
				}
			}
			else if (result == JFileChooser.CANCEL_OPTION){
				System.out.println("Cancel was selected");
			}
		}
		else if (holder == go){
			if (base1path != null)
				new RawFilter(base1path,8,16);
			else
				System.out.println("no filepath");
		}
		//frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
	}
	public static void main(String args[]){
		new filterGUI();

	}
}
