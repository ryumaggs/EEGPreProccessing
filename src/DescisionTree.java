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
	/*
	 * calculates the array when you add the elements of two double arrays
	 */
	public double[][] array_sum(double[][] a1, double[][] a2){
		double[][] ret = new double[a1.length][a1[0].length];
		for (int i = 0; i < a1.length; i++){
			for (int j = 0; j < a1[0].length; j++){
				ret[i][j] = a1[i][j] + a2[i][j];
			}
		}
		return ret;
	}
	
	public int minIndex(double[] distances){
		/*
		 * Returns the index of the lowest value in a double array: this is the best wavelength
		 * for a particular channel
		 */
		double min = distances[0];
		int min_index = 0;
		for (int i = 0; i < distances.length; i++){
			if (distances[i] < min)
				min_index = i;
		}
		
		return min_index;
	}
	
	public int[] total_diff(int base_num){
		/*
		 * Loops through all trials comparing to base a
		 */
		double[][] total_diff_0 = new double[base1.num_channels()][base1.channels1[0].num_freq()];
		double[][] total_diff_1 = new double[base1.num_channels()][base1.channels1[0].num_freq()];
		int[] ret = new int[base1.num_channels()];
		
		// FOR ALL TRIALS of type 0 and 1 respectively, generate a double[][] that marks channels x frequency and the values are the
		//differences in the FFT.
		for (int i = 0; i < rest.length; i++){
			if (rest[0].trial_type() == base_num){
				//gives you an array with total distances between all trials for all channels
				total_diff_0 = array_sum(total_diff_0, rest[i].bestWaves(base1));
			}
			else{
				total_diff_1 = array_sum(total_diff_1, rest[i].bestWaves(base2));
			}
		}
		
		if (base_num == 0){
			for (int i = 0; i < base1.num_channels(); i++)
				ret[i] = minIndex(total_diff_0[i]);
		}
		else{
			for (int i = 0; i < base1.num_channels(); i++)
				ret[i] = minIndex(total_diff_0[i]);
		}
		
		return ret;
		
	}


	public static void main(String args[]){
		
		//nothing
	}
}
