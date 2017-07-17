import java.util.*;
import java.io.*;

public class RawFilter {
	private static int num_trials;
	private static final int NUM_TIMESTAMP = 128;
	private Complex[][] parsedunfilteredData;
	private Complex[][] filteredData;
	private Complex[][] transposed;
	private Complex[][] inversed;
	private Complex[][] compressed;
	private int[][] freq_array;
	final String CLOSED = "CLOSED";
	final String OPEN = "OPEN";
	final String MARKER = "MARKER";
	
	/*
	 * for each file in the directory:
	 * Read file
	 * transpose it so that it is an array that is channel x data
	 * zero-ground, and reference-ground the data
	 * FFT and filter
	 * inverse FFT
	 * write to a new data file
	 * 
	 * Citation: the Complex class and objects used throughout this code was taken from Algorithms Fourth Edition 
	 * by Robert Sedgewick and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: algs4.cs.princeton.edu/99scientific/Complex.java.html
	 */
	public RawFilter(String dirPath, String destination, int chan){
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		freq_array = new int[2][4];
		freq_array[0][0] = 0;
		freq_array[1][0] = 40;
		freq_array[0][1] = 0;
		freq_array[1][1] = 14;
		freq_array[0][2] = 8;
		freq_array[1][2] = 12;
		freq_array[0][3] = 10;
		freq_array[1][3] = 14;
		
		for(File file : listOfFiles){
			if (file.isFile()){
				num_trials = 0;
				
				//checks the type of file (+1 or -1) so that it can write that to the file
				System.out.println("-----------------------");
				int tp = check_type(file);
				
				//puts non-transposed, non-filtered data into parsedunfilteredData
				parsedunfilteredData = parseData(file, chan);
				System.out.println("finished parsing data");
				
				//transposes the data into transposed
				transposed = transpose(parsedunfilteredData);
				System.out.println("successfully transposed");
				
				//zeros each trial with data preceding the 2nd image change
				zero_data(transposed);
				System.out.println("zeroed out the data");
				
				//compresses the data to keep only what we want
				compressed = compress_array(8,num_trials/8);
				System.out.println("compressed data");
				System.out.println("compressed length: " + compressed[0].length);
				
				int low_range = 0;
				int high_range = 0;
				for(int i = 0; i < freq_array[0].length; i++){
					low_range = freq_array[0][i];
					high_range = freq_array[1][i];
					System.out.println("range: " + low_range + " -> " + high_range);
					//Filters data via FFT into filteredData
					filteredData = bandpassfilter(compressed, 256, low_range, high_range);
					System.out.println("bandpassfiltered the data for range: " + low_range +"," + high_range);
				
					//Inverses back from FFT
					inversed = inverseFFT2x2(filteredData);
					System.out.println("inverse FFT'd the data");
				
					//write the data to file
					writeToFile(destination,tp,Integer.toString(low_range)+Integer.toString(high_range));
					System.out.println("wrote to file");
				
				}
			}
		}
		System.out.println("Done.");
	}
	
	/*
	 * Checks the type of trial ran (open or close hand)
	 * Parameter: file - the file being processed
	 * Output: either int -1 or 1 depending on the file
	 */
	public int check_type(File file){
		String file_name = file.getName();
		System.out.println(file_name);
		for (int i = 0; i < CLOSED.length(); i++){
			if (!(file_name.substring(i,i+1).equals(CLOSED.substring(i, i+1)))){
				return -1;
			}
		}
		return 1;
	}
	
	/*
	 * Given a OpenBCI file, parse it into a 2D array of timesample by channels
	 * Parameters: dataFile - the file being processed, 
	 * 			   chan - the number of channels in the recording
	 * Output:     Complex[total_number_of_samples][number_of_channels]
	 */
	private Complex[][] parseData(File dataFile, int chan){
//		int first_time_flag = 0;
//		double old_line_counter = 0;
//		double line_counter = 0;
//		int second_counter = 0;
//		int num_trials = 0;
		Scanner scan;
		String s="";
		ArrayList<Complex[]> nonTransposed = new ArrayList<Complex[]>();
		Complex[][] array_nonTransposed;
		try {
			scan = new Scanner(dataFile);
			//ignore the first 5 lines of data, it will be the OpenBCI headers
			for(int i=0; i <=10; i++){
				scan.nextLine();
			}
			
			//read the rest of file adding either data or a distinct pattern for marker data.
			while(scan.hasNextLine()){
				s = scan.nextLine();
				if (s.equals(MARKER))
					nonTransposed.add(trialParserMark(chan));
				else
					nonTransposed.add(trialParser(s,chan));
//				old_line_counter = line_counter;
//				line_counter = returnIndex(s);
//				nonTransposed.add(trialParser(s, chan));
//				
//				if(old_line_counter > line_counter){
//					//System.out.println("old line counter: " + old_line_counter);
//					//System.out.println("line_counter: " + line_counter);
//					second_counter+=1;
//					//System.out.println("second_counter: " + second_counter);
//				}
//				if(first_time_flag == 0 && second_counter == 3){
//					//System.out.println("trial num: " + num_trials);
//					first_time_flag = 1;
//					num_trials++;
//					second_counter = 0;
//					nonTransposed.add(trialParserMark(chan));
//				}
//				if (first_time_flag == 1 && second_counter == 5){
//					//System.out.println("trial num: " + num_trials);
//					nonTransposed.add(trialParserMark(chan));
//					num_trials++;
//					second_counter = 0;
//				}
//				if(num_trials >= 64){
//					break;
//				}
			}
			//System.out.println("num trials: " + num_trials);
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int num_row = nonTransposed.size();
		array_nonTransposed = new Complex[num_row][nonTransposed.get(0).length];
		for(int row = 0; row < num_row; row++){
			array_nonTransposed[row] = nonTransposed.get(row);
		}
		return array_nonTransposed;
	}
	
	/*
	 * Parse a single line of the file and return a Complex[] that has split the value based on spaces
	 * 
	 * Parameters: timesample - the String formatted data to be parsed,
	 * 			   chan - the number of channels in the recording
	 * Output:     Complex[] that contains the values of the String split into seperate elements
	 */
	private Complex[] trialParser(String dataLine, int chan){
		String[] curData = dataLine.split(" ");
		Complex[] curChan = new Complex[chan];
		double curVal;
		for(int c=0; c<chan; c++){
			curVal = Double.parseDouble(curData[c+1]);
			curChan[c] = new Complex(curVal, 0);
		}
		return curChan;
	}
	
	private double returnIndex(String dataLine){
		String[] curData = dataLine.split(" ");
		return Double.parseDouble(curData[0]);
	}
	/*
	 * Called when the reading the file hits a marker signifying the actual experimental data
	 * 
	 * Parameters:  chan - the number of channels in the recording
	 * Output:      Complex[] that contains only 123+456i (should not be able to find this pattern in the data)
	 */
	private Complex[] trialParserMark(int chan){
		Complex[] curChan = new Complex[chan];
		for (int i = 0; i < chan; i++){
			curChan[i] = new Complex(123,456);
		}
		return curChan;
	}
	/*
	 * This function takes an array that is sample x channel, and
	 * turns it into an array that is channel x sample
	 * 
	 * Parameter: inputarray - the array you want to rotate
	 * Output:    tArray - the inputarray that has been rotated
	 */
	private Complex[][] transpose(Complex[][] inputarray){
		int num_channels = inputarray[0].length;
		int num_input = inputarray.length;
		Complex[][] tArray = new Complex[num_channels][num_input];
		for(int coll=0; coll<num_channels; coll++){ 
			for(int row=0; row<num_input; row++){ 
				tArray[coll][row] = inputarray[row][coll]; 
			}
			//System.out.println(Arrays.toString(tArray[coll]));
		}
		return tArray;
	}
	
	/*
	 * Zero's data in correspondence with common EEG practice: take a bit of the data before the
	 * data you want to use (experimental data), average it, and subtract it from your experimental data.
	 * 
	 * Parameter: N/A
	 * Output:    does the subtraction of the transposed data in place such that 256 samples after the
	 * 			  marker have the pre-experiment data average subtracted from them
	 */
	public void zero_data(Complex[][] transposed_data){
		double avg = 0.0;
		int marker_flag = 0;
		int data_counter = 0;
		for (int i = 0; i < transposed.length; i++){
			for (int j = 0; j < transposed_data[i].length; j++){
				if (marker_flag == 1){
					transposed_data[i][j] = transposed_data[i][j].minus(new Complex(avg, 0));
					data_counter++;
				}
				if (transposed_data[i][j].equals(new Complex(123,456))){
					//this may break if there are not 50 data points before the image change, but there should be
					avg = average(i,j-50,j-1);
					marker_flag = 1;
					num_trials+=1;
				}
				if(data_counter == NUM_TIMESTAMP){
					marker_flag = 0;
					data_counter = 0;
					avg = 0;
				}
			}
		}
	}
	
	/*
	 * helper function for zero_data() that computes the average from the transposed array of a set
	 * index range
	 * 
	 * Parameter: row - the row (channel) you are currently working with
	 * 	          beg - the beginning index to start the averaging
	 * 	          end - the ending index to end the values to be averaged
	 * Output:    Returns the average of the values of the Transposed array on the given indexes for a channel
	 */
	public double average(int row, int beg, int end){
		double total = 0.0;
		for (int i = beg; i < end; i++){
			total += transposed[row][i].re();
		}
		return total/(end-beg);
	}
	
	/*
	 * Data is constantly being streamed, but not all the data is required. Only need to train SVM
	 * on the experimental data. This function strips the array of all information except experimental data, 
	 * which is defined as 256 samples after the second time the image flashes
	 * 
	 * Parameter: chan - number of channels in the array
	 * 			  num_trials - the number of trials in the data (each time a marker appears/8)
	 * Output:    A 2d Complex array that only contains experimental data
	 */
	private Complex[][] compress_array(int chan, int num_trials){
		int flag = 0;
		int compressed_col_count = 0;
		int total_added_count = 0;
		int trial_count = 0;
		Complex marker = new Complex(123,456);
		System.out.println("transposed length: " + transposed[0].length);
		Complex[][] compressed = new Complex[chan][num_trials*NUM_TIMESTAMP];
		System.out.println("compressed length: " + compressed[0].length);
		for (int i =0; i < chan; i++){
			trial_count = 0;
			for (int j = 0; j < transposed[i].length; j++){
				if(total_added_count == compressed[i].length){
					compressed_col_count = 0;
					total_added_count = 0;
					break;
				}
				//if you have already hit a marker and are within 0 < x < 128, add it to the compressed
				if (flag == 1){
					compressed[i][compressed_col_count] = transposed[i][j];
					//System.out.println("set " + i + "," + compressed_col_count + " to: " + transposed[i][j]);
					compressed_col_count +=1;
					total_added_count +=1;
				}
				//if you hit x == 128, which reset and unset the flag
				if (compressed_col_count == NUM_TIMESTAMP){
					flag = 0;
				}
				//if you hit a flag, set flag == 1
				if (flag == 0 && transposed[i][j].equals(marker)){
					flag = 1;
					trial_count++;
				}
			}
		}
		
		return compressed;
	}
	
	/*
	 * Filters the EEG data to within any specified frequency
	 * 
	 * Parameters: data - the compressed experimental data
	 * 			   samprate - the sample rate for the recording
	 * 	 		   lpfreq - lowest frequency bound
	 * 			   hpfreq - highest frequency bound
	 * Output  	   Returns a frequency array that has any frequency outside the specified range
	 * 			   scaled to 0, and any frequency within the range scaled by 2
	 * 
	 * Citation: the fft function was taken from Algorithms Fourth Edition by Robert Sedgewick
	 * and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: algs4.cs.princeton.edu/99scientific/FFT.java.html
	 */
	public static Complex[][] bandpassfilter(Complex[][] data, int samprate, int lpfreq, int hpfreq){
		int ccount = data.length;
		int tcount = data[0].length;
		int poscount = 0;
		Complex[] sub_filtereddata = new Complex[NUM_TIMESTAMP];
		Complex[] fft_sub_filtereddata = new Complex[NUM_TIMESTAMP];
		Complex[][] filtereddata = new Complex[ccount][tcount];
		for(int channel = 0; channel < ccount; channel++){
			for(int trial = 0; trial < num_trials/8; trial++){
				for (int index = 0; index < NUM_TIMESTAMP; index++){
					sub_filtereddata[index] = data[channel][NUM_TIMESTAMP*trial + index];
				}
				fft_sub_filtereddata = Channel.fft(sub_filtereddata);
				for(int freq=0; freq<NUM_TIMESTAMP; freq++){
					double curFreq = (double)freq * samprate / NUM_TIMESTAMP;
					if(curFreq >= lpfreq && curFreq <= hpfreq){
						fft_sub_filtereddata[freq] = fft_sub_filtereddata[freq].scale(2);
					}
					else{
						fft_sub_filtereddata[freq] = fft_sub_filtereddata[freq].scale(0);
					}
				}
				//System.out.println(Arrays.toString(fft_sub_filtereddata));
				for(int index1 = 0; index1 < NUM_TIMESTAMP; index1++){
					filtereddata[channel][poscount] = fft_sub_filtereddata[index1];
					poscount++;
				}
			}
			poscount = 0;
		}
		return filtereddata;
	}
	
	/*
	 * Computes the inverse FFT (from frequency array -> time array)
	 * 
	 * Parameters: filterData - the filtered experimental data
	 * Output: 	   array converted back into time-based domain
	 * 
	 * Citation: the ifft function was taken from Algorithms Fourth Edition by Robert Sedgewick
	 * and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: algs4.cs.princeton.edu/99scientific/FFT.java.html
	 */
	public static Complex[][] inverseFFT2x2(Complex[][] filterData){
		Complex[][] ret = new Complex[filterData.length][(filterData[0].length)];
		Complex[] sub_ret = new Complex[NUM_TIMESTAMP];
		Complex[] ifft_sub_ret = new Complex[NUM_TIMESTAMP];
		int poscounter = 0;
		for (int channel1 = 0; channel1 < filterData.length; channel1++){
			for(int trialnum = 0; trialnum< num_trials/8; trialnum++){
				for(int index = 0; index < NUM_TIMESTAMP; index++){
					sub_ret[index] = filterData[channel1][trialnum*NUM_TIMESTAMP+index];
				}
				//System.out.println(Arrays.toString(sub_ret));
				ifft_sub_ret = Channel.ifft(sub_ret);
				for(int index1 = 0; index1 < NUM_TIMESTAMP; index1++){
					ret[channel1][poscounter] = ifft_sub_ret[index1];
					poscounter++;
				}
			}
			poscounter = 0;
		}
		return ret;
	}
	
	/*
	 * Writes the experimental data to a text file in the LIBSVM format. Splits the data into seperate files
	 * for each channel (a model will be trained on each channel).
	 * 
	 * Parameter: destination - the file path for where the files are to be written
	 * 	          trial_type - the type of file (open or close hand)
	 * 	          filteredData - the actual experimental data (preprocessing finished)
	 */
	public void writeToFile(String destination, int trial_type, String freq_range){
		int temp_count = 0;
		try{
			int sample_count = 0;
			//go through each channel (row), and add each channel data to its respective file
			for (int i = 0; i < inversed.length; i++){
				File file = new File(destination + "\\Channel"+i+"_"+freq_range+".txt");
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				sample_count = 0;
				for (int j = 0; j < inversed[i].length; j++){
					if (sample_count >= NUM_TIMESTAMP){
						sample_count = 0;
						out.print("\n");
					}
					if(sample_count==0){
						out.print(trial_type + " ");
						temp_count++;
					}
					if (inversed[i][j].re() == 0 && inversed[i][j].im()== 0){
						//System.out.println("hit a blank at: " + sample_count);
						sample_count++;
						continue;
					}
					out.print(sample_count+":"+inversed[i][j].abs()+" ");
					sample_count++;
				}
				out.print("\n");
				out.close();
			}
		} catch (IOException e){}
		
		//System.out.println("shoudl have written: " + temp_count/8 + " number of trials");
	}
	
	//main function is for testing purposes
	public static void main(String[] args){
		RawFilter test = new RawFilter("src/RawDataFolder","src/FilteredDataFolder", 8);
	}
}
