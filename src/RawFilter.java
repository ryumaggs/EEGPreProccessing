import java.util.*;
import java.io.*;

public class RawFilter {
	int num_trials;
	private Complex[][] parsedunfilteredData;
	private Complex[][] filteredData;
	private Complex[][] transposed;
	private Complex[][] inversed;
	private Complex[][] compressed;
	final String CLOSED = "CLOSED";
	final String OPEN = "OPEN";
	final String MARKER = "MARKER";
	
	/*
	 * for each file in the directory:
	 * Read file
	 * transpose it so that it is an array that is channel x data
	 * zero-ground, and reference-ground the data
	 * FFT and filter (0-40hz)
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
				
				//Filters data via FFT into filteredData
				filteredData = bandpassfilter(compressed, 256, 0, 40);
				System.out.println("bandpassfiltered the data");
				
				//Inverses back from FFT
				inversed = inverseFFT2x2(filteredData);
				System.out.println("inverse FFT'd the data");
				
				//write the data to file
				writeToFile(destination,tp,inversed);
				System.out.println("wrote to file. Done");
			}
		}
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
		Scanner scan;
		String s="";
		ArrayList<Complex[]> nonTransposed = new ArrayList<Complex[]>();
		Complex[][] array_nonTransposed;
		try {
			scan = new Scanner(dataFile);
			//ignore the first 5 lines of data, it will be the OpenBCI headers
			for(int i=0; i <=5; i++){
				scan.nextLine();
			}
			
			//read the rest of file adding either data or a distinct pattern for marker data.
			while(scan.hasNextLine()){
				s = scan.nextLine();
				//Note:adjust the comparison when actual marker is decided
				if (!(s.equals(MARKER)))
					nonTransposed.add(trialParser(s, chan));
				else
					nonTransposed.add(trialParserMark(chan));
			}
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
				if(data_counter == 128){
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
		int compressed_col = 0;
		Complex[][] compressed = new Complex[chan][num_trials*128];
		for (int i =0; i < chan; i++){
			for (int j = 0; j < transposed[i].length; j++){
				if (flag == 1){
					compressed[i][compressed_col] = transposed[i][j];
					compressed_col +=1;
				}
				if (transposed[i][j].equals(new Complex(123,456))){
					flag = 1;
				}
				if (compressed_col == (num_trials*128)){
					compressed_col = 0;
					flag = 0;
					break;
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
		Complex[][] filtereddata = new Complex[ccount][tcount];
		for(int channel=0; channel<ccount; channel++){
			filtereddata[channel] = Channel.fft(data[channel]);
		}
		for(int channel=0; channel<ccount; channel++){
			for(int freq=0; freq<tcount; freq++){
				double curFreq = (double)freq * samprate / tcount;
				if(curFreq >= lpfreq && curFreq <= hpfreq){
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(2);
				}
				else{
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(0);
				}
			}
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
		for (int channel1 = 0; channel1 < filterData.length; channel1++){
			ret[channel1] = Channel.ifft(filterData[channel1]);
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
	public void writeToFile(String destination, int trial_type, Complex[][] filteredData){
		int temp_count = 0;
		try{
			int sample_count = 0;
			//go through each channel (row), and add each channel data to its respective file
			for (int i = 0; i < compressed.length; i++){
				File file = new File(destination + "\\Channel"+i+".txt");
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				//need to adjust the bounds possibly
				for (int j = 0; j < compressed[i].length; j++){
					if(sample_count==0){
						out.print(trial_type + " ");
						temp_count++;
					}
					if (compressed[i][j].re() == 0 && compressed[i][j].im()== 0){
						sample_count++;
						continue;
					}
					out.print(sample_count+":"+compressed[i][j].abs()+" ");
					sample_count++;
					if (sample_count >= 128){
						sample_count = 0;
						out.print("\n");
					}
				}
				out.close();
			}
		} catch (IOException e){}
		
		System.out.println("shoudl have written: " + temp_count + " number of trials");
	}
	
	//main function is for testing purposes
	public static void main(String[] args){
		RawFilter test = new RawFilter("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\tester","C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder", 8);
	}
}
