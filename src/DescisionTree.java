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
	private int trial_type;
	private Channel[] channels;
	
	//Used to condense trial data into an int (for trial type) and channel data
	//CHANGE THE PARAMETER, figure out what type of data it should take
	public DescisionTree(int frequ, Complex[][] eeg_data){
		trial_type = frequ;
		for (int i = 0; i < channels.length; i++){
			Channel temp = new Channel(eeg_data);
			channels[i] = temp;
		}
	}
	
	public int num_wlengths(Complex[][] channel){
		return channel.length;
	}
	
	public double compareWaveLength(Complex[][] base_fft, Complex[][] trial_fft){
		/*
		 Input: the wavelength you are looking at, the base DT, and the trial DT
		 Output: returns the overall distance from the origin of the difference between the base and trial
		 
		 goes through the wavelengths of ONE channel in ONE trial and returns their difference distance in a
		 double array
		 */
		int num_freq = base_fft.length;
		double[] diff_dist = new double[num_freq];
		//probably have to change this upper bound
		for (int i = 0; i < num_freq; i++){
			double re_diff_sum = 0.0;
			double im_diff_sum = 0.0;
			for (int j = 0; j < base_fft[0].length; j++){
				re_diff_sum += Math.abs(trial_fft[i][j].re() - (base_fft[i][j]).re());
				im_diff_sum += Math.abs(trial_fft[i][j].im() - (base_fft[i][j]).im());
			
			}
		}
		
		double complexMod = Math.sqrt(Math.pow(re_diff_sum, 2.0) + Math.pow(im_diff_sum, 2.0));
		
		return complexMod;
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
	
	public int[] bestWaves(DescisionTree base, DescisionTree[] trials){
		/*
		 * Goes through each channel in a trial and computes the best wavelength for each channel
		 */
		int[] best_waves = new int[trials.length];
		for (int i = 0; i < trials.length; i++){
			double[] wLengths = new double[trials.length];
			for (int j = 0; j < (trials[i]).length; j++){
				wLengths[j] = compareWaveLength(j,base,trials[i]);
			}
			
		}
	}

	public static void main(String args[]){
		int num_channels = 0;
		
		//nothing
	}
}
