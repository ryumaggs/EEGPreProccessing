
public class Trial {
	private int trial_type;
	private Channel[] channels1;
	
	public Trial(int type, Channel[] channelData){
		trial_type = type;
		channels1 = channelData;
	}
	
	public int trial_type(){
		return trial_type;
	}
	
	public static int num_freq(Channel channels2){
		return channels2.freq.length;
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
	
	public double[][] bestWaves(Trial base){
		/*
		 * Input: the base trial
		 * 
		 * Function: loop through the channels of the base and test trial and calculate the best wavelength for each trial
		 *
		 * Output: Returns a double array that is differences of channels x frequency for one trial
		 */
		double[][] bWave = new double[channels1.length][num_freq(this.channels1[0])];
		for (int channel = 0; channel < channels1.length; channel++){
			// bwave  = frequency distances of one channel
			bWave[channel] = Channel.compareWaveLengths(this.channels1[channel].freq, base.channels1[channel].freq);
		}
		return bWave;
	}
	
}
