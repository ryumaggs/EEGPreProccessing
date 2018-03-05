
public class slidingDFT {
	private static final int NUM_DATA_POINTS = 128;
	private static final int NUM_CHANNELS = 8;
	private static final double SAMPRATE = 128.0;
	private static double[] freqs_wanted;
	Complex[] window;
	Complex[] Wk;
	double kth;
	int num_sample = 128;
	
	public slidingDFT(String name){
		freqs_wanted = list_of_freq();
		window = new Complex[num_sample];
		Wk = new Complex[num_sample];
		for(int freq = 0; freq < num_sample; freq++){
			kth = -2 * freq * Math.PI / num_sample;
			Wk[freq] = new Complex(Math.cos(kth), Math.sin(kth));
		}
	}
	
	public Complex[] initialFFT(Complex[] x){
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
        Complex[] q = initialFFT(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = initialFFT(odd);

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
	
	public static double[] list_of_freq(){
		double[] index = new double[18];
		int val = 0;
		for (int freq = 6; freq <= 40; freq = freq + 2){
			index[val] = (freq * NUM_DATA_POINTS) / SAMPRATE;
			val += 1;
			System.out.println(val+" and "+freq);
		}
		return index;
	}
	
	public void slideOne(Complex[] orig_fft, Complex new_val){
		//for (int i = )
	}
	public static void main(String args[]){
		double[] test = list_of_freq();
		for(int i = 0; i < test.length; i++){
			System.out.println(test[i]);
		}
	}
}
