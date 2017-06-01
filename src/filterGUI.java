import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class filterGUI extends Frame implements ActionListener{
	private Button base1;
	private Button base2;
	private Button remTrials;

	private String base1path;
	private String base2path;
	private String restTrialsDirPath; // save a directory
	
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
		frame = new JFrame();
		frame.setSize(300,300);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up the JPanel and buttons
		panel = new JPanel();
		base1 = new Button("Base1");
		base2 = new Button("Base2");
		remTrials = new Button("Remaining Trials");
		
		base1.addActionListener(this);
		base2.addActionListener(this);
		remTrials.addActionListener(this);
		

		//Add everything to the JFrame
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
				TextField path = new TextField();
				path.setText(chose.getSelectedFile().getAbsolutePath());
				if (e.getSource() == base1)
					base1path = path.getText();
				else if (e.getSource() == base2)
					base2path = path.getText();
				else if (e.getSource() == remTrials)
					restTrialsDirPath = path.getText();
				panel.add(path);
			}
			else if (result == JFileChooser.CANCEL_OPTION){
				System.out.println("Cancel was selected");
			}
		}
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
	}
	public static void main(String args[]){
		new filterGUI();

	}
}
