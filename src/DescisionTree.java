import java.util.*;
import java.io.*;

/*
	1.Takes a Complex[][] from a single channel where each wavelength is represented as a COLLUMN of this array of arrays
	2.Runs FFT on each wavelength, storing it in a new Complex[][] 
	3.Picks two trials: (0,1)
	4.Loop through the wavelengths of the remaining trials comparing it to the picked trials
 */

public class DescisionTree {
	
	private int num_freq;
	private Complex[][] freq;
	
	//Used to condense trial data into an int (for trial type) and Complex[][]
	public DescisionTree(int frequ, Complex[][] eeg_values){
		num_freq = frequ;
		for (int i = 0; i < eeg_values.length; i++){
			Complex[] fftdata = fft(eeg_values[i]);
			freq[i] = fftdata;
		}
	}
	
	public double compareWaveLength(int wave_num, DescisionTree base, DescisionTree trial){
		double re_diff_sum = 0.0;
		double im_diff_sum = 0.0;
		
		//probably have to change this upper bound
		for(int i = 0; i < base.freq.length; i++){
			re_diff_sum += Math.abs((base.freq[i][wave_num]).re() - (trial.freq[i][wave_num]).re());
			im_diff_sum += Math.abs((base.freq[i][wave_num]).im() - (trial.freq[i][wave_num]).im());
		}
		
		double complexMod = Math.sqrt(Math.pow(re_diff_sum, 2.0) + Math.pow(im_diff_sum, 2.0));
		
		return complexMod;
	}
	// taken from princeton
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) { throw new RuntimeException("n is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[n/2];
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + n/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
	public static void main(String args[]){
		//nothing
	}
}
