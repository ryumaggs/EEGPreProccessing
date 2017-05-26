
public class Channel {
	public Complex[][] freq;
	
	public Channel(Complex[][] eeg_values){
		for (int i = 0; i < eeg_values.length; i++){
			Complex[] fftdata = fft(eeg_values[i]);
			freq[i] = fftdata;
		}
	}
	
	public static double[] compareWaveLengths(Complex[][] base_fft, Complex[][] trial_fft){
		/*
		 Input: the fft data of the randomly selected base-case, and the fft of one trial for ONE PARTICULAR CHANNEL.
		 
		 goes through the frequencies of ONE channel in ONE trial and returns their difference distance in a
		 double array
		 */
		int num_freq = base_fft.length;
		double[] diff_dist = new double[num_freq];
		// loop through each frequency
		for (int i = 0; i < num_freq; i++){
			double re_diff_sum = 0.0;
			double im_diff_sum = 0.0;
			//loop through each sub-wave of each frequency
			for (int j = 0; j < base_fft[0].length; j++){
				//subtract base from trial to get the difference and sum that difference
				re_diff_sum += Math.abs(trial_fft[i][j].re() - (base_fft[i][j]).re());
				im_diff_sum += Math.abs(trial_fft[i][j].im() - (base_fft[i][j]).im());
			}
			//compute total distance away from base of trial
			diff_dist[i] = Math.sqrt(Math.pow(re_diff_sum, 2.0) + Math.pow(im_diff_sum, 2.0));
		}
		return diff_dist;
	}
    // compute the FFT of x[], assuming its length is a power of 2
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
}
