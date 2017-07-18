import java.util.*;
import java.io.*;

public class RawFilter {
	private static int num_trials;
	private static final int NUM_DATA_POINTS = 128;
	
	private Complex[][] parsedunfilteredData;
	private Complex[][] filteredData;
	private Complex[][] transposedData;
	private Complex[][] compressedData;
	private int[][] frequency_ranges;
	
	final String CLOSED = "CLOSED";
	final String OPEN = "OPEN";
	final String MARKER = "MARKER";
	
	/*
	 * Citation: the Complex class and objects used throughout this code was taken from Algorithms Fourth Edition 
	 * by Robert Sedgewick and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: algs4.cs.princeton.edu/99scientific/Complex.java.html
	 */
	public RawFilter(String dirPath, String destination, int chan){
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		
		frequency_ranges = new int[2][4];
		frequency_ranges[0][0] = 0;
		frequency_ranges[1][0] = 40;
		frequency_ranges[0][1] = 0;
		frequency_ranges[1][1] = 14;
		frequency_ranges[0][2] = 8;
		frequency_ranges[1][2] = 12;
		frequency_ranges[0][3] = 10;
		frequency_ranges[1][3] = 14;
		
		for(File file : listOfFiles){
			if (file.isFile()){
				num_trials = 0;
				
				System.out.println("-----------------------");
				int trial_type = check_trial_type(file);
				
				parsedunfilteredData = parseRecordingData(file, chan);
				System.out.println("finished parsing data");
				
				transposedData = transpose(parsedunfilteredData);
				System.out.println("successfully transposed");
				
				zero_data(transposedData);
				System.out.println("zeroed out the data");
				
				compressedData = compress_array(8,num_trials/8);
				System.out.println("compressed data");
				System.out.println("compressed length: " + compressedData[0].length);
				
				int low_frequency_bound = 0;
				int high_frequency_bound = 0;
				
				for(int i = 0; i < frequency_ranges[0].length; i++){
					low_frequency_bound = frequency_ranges[0][i];
					high_frequency_bound = frequency_ranges[1][i];
					
					System.out.println("range: " + low_frequency_bound + " -> " + high_frequency_bound);
					
					filteredData = bandpassfilter(compressedData, 256, low_frequency_bound, high_frequency_bound);
					
					System.out.println("bandpassfiltered the data for range: " + low_frequency_bound +"," + high_frequency_bound);
					
					writeToFile(destination,trial_type,Integer.toString(low_frequency_bound)+Integer.toString(high_frequency_bound));
					
					System.out.println("wrote to file");
				
				}
			}
		}
		System.out.println("\nDone.");
	}
	
	public int check_trial_type(File file){
		String file_name = file.getName();
		System.out.println(file_name);
		for (int i = 0; i < CLOSED.length(); i++){
			if (!(file_name.charAt(i) == CLOSED.charAt(i))){
				return -1;
			}
		}
		return 1;
	}
	
	private Complex[][] parseRecordingData(File dataFile, int chan){
		int stage_tracker = 0;
		double old_line_counter = 0;
		double line_counter = 0;
		int last_sample_flag = 0;
		int second_counter = 0;
		int num_trials = 0;
		
		Scanner scan;
		String s="";
		ArrayList<Complex[]> nonTransposedData = new ArrayList<Complex[]>();
		Complex[][] array_nonTransposedData;
		try {
			scan = new Scanner(dataFile);
			//first five lines are just headers; skip.
			for(int i=0; i <=5; i++){
				scan.nextLine();
			}
			
			/*
			 * Goal: add a mark 3 seconds into the recording, and then every 5 seconds. This marker should
			 * match when the GO prompt plays so the data we want can be easily found later
			 * 
			 * stage_tracker == 0 || waiting until 3 seconds in to add first marker
			 * stage_tracker == 1 || now adding a marker every 5 seconds
			 */
			while(scan.hasNextLine()){
				s = scan.nextLine();
				
				if(s.equals(MARKER) || s.equals("CHANGED MY IMAGE HERE BOYS"))
					continue;
				
				old_line_counter = line_counter;
				line_counter = getIndex(s);
				nonTransposedData.add(convertDataLine(s, chan));
				
				if(old_line_counter > line_counter){
					second_counter+=1;
				}
				if(stage_tracker == 0 && second_counter == 3){
					stage_tracker = 1;
					num_trials++;
					second_counter = 0;
					nonTransposedData.add(addMarker(chan));
				}
				if (stage_tracker == 1 && second_counter == 5 && last_sample_flag == 0){
					nonTransposedData.add(addMarker(chan));
					num_trials++;
					second_counter = 0;
				}
				if(num_trials == 63){
					last_sample_flag = 1;
				}
				if(num_trials >= 64){
					System.out.println("exiting with num trials as: " + num_trials);
					break;
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int num_row = nonTransposedData.size();
		array_nonTransposedData = new Complex[num_row][nonTransposedData.get(0).length];
		for(int row = 0; row < num_row; row++){
			array_nonTransposedData[row] = nonTransposedData.get(row);
		}
		
		return array_nonTransposedData;
	}
	
	private Complex[] convertDataLine(String dataLine, int chan){
		String[] curData = dataLine.split(" ");
		Complex[] curChan = new Complex[chan];
		double curVal;
		for(int c=0; c<chan; c++){
			curVal = Double.parseDouble(curData[c+1]);
			curChan[c] = new Complex(curVal, 0);
		}
		return curChan;
	}
	
	private double getIndex(String dataLine){
		String[] curData = dataLine.split(" ");
		return Double.parseDouble(curData[0]);
	}

	private Complex[] addMarker(int chan){
		Complex[] curChan = new Complex[chan];
		for (int i = 0; i < chan; i++){
			curChan[i] = new Complex(123,456);
		}
		return curChan;
	}

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
	 * A common technique in EEG data analysis; average signals before your desired data and
	 * subtract that average from your desired data
	 */
	public void zero_data(Complex[][] transposed_data){
		double avg = 0.0;
		int marker_flag = 0;
		int data_counter = 0;
		for (int i = 0; i < transposed_data.length; i++){
			for (int j = 0; j < transposed_data[i].length; j++){
				if (marker_flag == 1){
					transposed_data[i][j] = transposed_data[i][j].minus(new Complex(avg, 0));
					data_counter++;
				}
				if (transposed_data[i][j].equals(new Complex(123,456))){
					avg = average(i,j-50,j-1);
					marker_flag = 1;
					num_trials+=1;
				}
				if(data_counter == NUM_DATA_POINTS){
					marker_flag = 0;
					data_counter = 0;
					avg = 0;
				}
			}
		}
	}
	
	public double average(int row, int beg, int end){
		double total = 0.0;
		for (int i = beg; i < end; i++){
			total += transposedData[row][i].re();
		}
		return total/(end-beg);
	}

	private Complex[][] compress_array(int chan, int num_trials){
		int hit_marker = 0;
		int compressed_col_count = 0;
		int total_added_count = 0;
		Complex marker = new Complex(123,456);
		Complex[][] compressed = new Complex[chan][num_trials*NUM_DATA_POINTS];
		for (int i =0; i < chan; i++){
			compressed_col_count = 0;
			total_added_count = 0;
			for (int j = 0; j < transposedData[i].length; j++){
				if(total_added_count == compressed[i].length){
					break;
				}
				if (hit_marker == 1){
					compressed[i][compressed_col_count] = transposedData[i][j];
					compressed_col_count +=1;
					total_added_count +=1;
				}
				if (compressed_col_count == NUM_DATA_POINTS){
					hit_marker = 0;
				}
				if (hit_marker == 0 && transposedData[i][j].equals(marker)){
					hit_marker = 1;
				}
			}
		}
		return compressed;
	}
	
	/*
	 * Citation: the FFT function used in this code was taken from Algorithms Fourth Edition 
	 * by Robert Sedgewick and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: http://introcs.cs.princeton.edu/java/97data/FFT.java.html
	 */
	
	public static Complex[][] bandpassfilter(Complex[][] data, int samprate, int lpfreq, int hpfreq){
		int num_channel = data.length;
		int num_data = data[0].length;
		int array_index = 0;
		Complex[] sub_filtereddata = new Complex[NUM_DATA_POINTS];
		Complex[] fft_sub_filtereddata = new Complex[NUM_DATA_POINTS];
		Complex[][] filtereddata = new Complex[num_channel][num_data];
		for(int channel = 0; channel < num_channel; channel++){
			for(int trial = 0; trial < num_trials/8; trial++){
				for (int index = 0; index < NUM_DATA_POINTS; index++){
					sub_filtereddata[index] = data[channel][NUM_DATA_POINTS*trial + index];
				}
				
				fft_sub_filtereddata = Channel.fft(sub_filtereddata);
				
				for(int freq=0; freq<NUM_DATA_POINTS; freq++){
					double curFreq = (double)freq * samprate / NUM_DATA_POINTS;
					if(curFreq >= lpfreq && curFreq <= hpfreq){
						fft_sub_filtereddata[freq] = fft_sub_filtereddata[freq].scale(2);
					}
					else{
						fft_sub_filtereddata[freq] = fft_sub_filtereddata[freq].scale(0);
					}
				}

				for(int index1 = 0; index1 < NUM_DATA_POINTS; index1++){
					filtereddata[channel][array_index] = fft_sub_filtereddata[index1];
					array_index++;
				}
			}
			array_index = 0;
		}
		return filtereddata;
	}
	
	public void writeToFile(String destination, int trial_type, String freq_range){
		try{
			int sample_index = 0;
			
			for (int i = 0; i < filteredData.length; i++){
				File file = new File(destination + "\\Channel"+i+"_"+freq_range+".txt");
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				sample_index = 0;
				for (int j = 0; j < filteredData[i].length; j++){
					if (sample_index >= NUM_DATA_POINTS){
						sample_index = 0;
						out.print("\n");
					}
					
					if(sample_index==0){
						out.print(trial_type + " ");
					}
					
					if (filteredData[i][j].re() == 0 && filteredData[i][j].im()== 0){
						sample_index++;
						continue;
					}
					
					out.print(sample_index+":"+filteredData[i][j].abs());
					out.print(" ");
					
					sample_index++;
				}
				out.print("\n");
				out.close();
			}
		} catch (IOException e){}
	}
	
	//main function is for testing purposes
	public static void main(String[] args){
		//RawFilter test = new RawFilter("src/RawDataFolder","src/FilteredDataFolder", 8);
	}
}
