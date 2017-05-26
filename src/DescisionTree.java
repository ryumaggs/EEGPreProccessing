import java.util.*;
import java.io.*;

/*
	1.Takes a Complex[][] from a single channel where each wavelength is represented as a row of this array of arrays
	2.Runs FFT on each wavelength, storing it in a new Complex[][] 
	3.Picks two trials: (0,1)
	4.Loop through the wavelengths of the remaining trials comparing it to the picked trials
 */

public class DescisionTree {
	
	//
	private Trial base1;
	private Trial base2;
	private Trial[] rest;
	
	//Used to condense trial data into an int (for trial type) and channel data
	//CHANGE THE PARAMETER, figure out what type of data it should take
	public DescisionTree(Trial[] all_data){
		int flag = 0;
		int count = 0;
		while (flag < 2){
			if (all_data[count].trial_type() == 0){
				base1 = all_data[count];
				flag++;
			}
			else if (all_data[count].trial_type() == 1){
				base2 = all_data[count];
				flag++;
			}
		}
		rest = all_data;
	}
	


	public static void main(String args[]){
		int num_channels = 0;
		
		//nothing
	}
}
