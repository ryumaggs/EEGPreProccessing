import java.util.*;
import java.io.*;

public class RawFilter {
	private static int num_trials_global;
	private static final int NUM_DATA_POINTS = 128;
	private static final Complex NUMBER_MARKER = new Complex(123,456);
	private static final int NUM_CHANNELS = 8;
	
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
		frequency_ranges[0][0] = 5;
		frequency_ranges[1][0] = 40;
		frequency_ranges[0][1] = 5;
		frequency_ranges[1][1] = 14;
		frequency_ranges[0][2] = 8;
		frequency_ranges[1][2] = 12;
		frequency_ranges[0][3] = 10;
		frequency_ranges[1][3] = 14;
		
		for(File file : listOfFiles){
			if (file.isFile()){
				num_trials_global = 0;
				
				System.out.println("-----------------------");
				int trial_type = check_file_type(file);
				
				parsedunfilteredData = parseRecordingData(file);
				System.out.println("finished parsing data");
				
				compressedData= baselineCorrect_and_Compress_data(parsedunfilteredData);
				System.out.println("compressed and baseline corrected the data");
				System.out.println("compressed length: " + compressedData[0].length);
				
				int low_frequency_bound = 0;
				int high_frequency_bound = 0;
				
				for(int i = 0; i < frequency_ranges[0].length; i++){
					low_frequency_bound = frequency_ranges[0][i];
					high_frequency_bound = frequency_ranges[1][i];

					filteredData = bandpassfilter(compressedData, 256, low_frequency_bound, high_frequency_bound);
					
					System.out.println("bandpassfiltered the data for range: " + low_frequency_bound +"," + high_frequency_bound);
					
					writeToFile(destination,trial_type,Integer.toString(low_frequency_bound)+Integer.toString(high_frequency_bound));
					
					System.out.println("wrote to file");
				
				}
			}
		}
		System.out.println("\nDone.");
	}
	
	public int check_file_type(File file){
		String file_name = file.getName();
		System.out.println(file_name);
		for (int i = 0; i < CLOSED.length(); i++){
			if (!(file_name.charAt(i) == CLOSED.charAt(i))){
				return -1;
			}
		}
		return 1;
	}
	
	private Complex[][] parseRecordingData(File dataFile){
		int stage_tracker = 0;
		double old_sample_counter = 0;
		double sample_counter = 0;
		int last_sample_flag = 0;
		int second_counter = 0;
		int total_seconds = 0;
		
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
				
				old_sample_counter = sample_counter;
				sample_counter = getIndex(s);
				nonTransposedData.add(String_to_Complex_Arr(s));
				
				if(old_sample_counter > sample_counter){
					second_counter+=1;
					total_seconds+=1;
				}
				//need to delete this once data recording works properly. 
				if(total_seconds >= 75){
					break;
				}
				
				if(stage_tracker == 0 && second_counter == 3){
					stage_tracker = 1;
					second_counter = 0;
					nonTransposedData.add(addMarker());
					num_trials_global+=1;
				}
				if (stage_tracker == 1 && second_counter == 5 && last_sample_flag == 0){
					nonTransposedData.add(addMarker());
					num_trials_global+=1;
					second_counter = 0;
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int num_row = nonTransposedData.size();
		array_nonTransposedData = new Complex[num_row][NUM_CHANNELS];
		for(int row = 0; row < num_row; row++){
			array_nonTransposedData[row] = nonTransposedData.get(row);
		}
		
		return array_nonTransposedData;
	}
	
	private Complex[] String_to_Complex_Arr(String dataLine){
		String[] curData = dataLine.split(" ");
		Complex[] curChan = new Complex[NUM_CHANNELS];
		double curVal;
		for(int c=0; c < NUM_CHANNELS; c++){
			curVal = Double.parseDouble(curData[c+1]);
			curChan[c] = new Complex(curVal, 0);
		}
		return curChan;
	}
	
	private double getIndex(String dataLine){
		String[] curData = dataLine.split(" ");
		return Double.parseDouble(curData[0]);
	}

	private Complex[] addMarker(){
		Complex[] curChan = new Complex[NUM_CHANNELS];
		for (int i = 0; i < NUM_CHANNELS; i++){
			curChan[i] = NUMBER_MARKER;
		}
		return curChan;
	}
	
	/*
	 * A common technique in EEG data analysis; average signals before your desired data and
	 * subtract that average from your desired data
	 */
	public Complex[][] baselineCorrect_and_Compress_data(Complex[][] parsed_data){
		double avg = 0.0;
		int marker_flag = 0;
		int data_counter = 0;
		int compressed_index = 0;
		Complex[][] zerod_compressed_data = new Complex[num_trials_global*NUM_DATA_POINTS][NUM_CHANNELS];
		for (int channel = 0; channel < NUM_CHANNELS; channel++){
			compressed_index = 0;
			for (int sample = 0; sample < parsed_data.length; sample++){
				if (marker_flag == 1){
					zerod_compressed_data[compressed_index][channel] = parsed_data[sample][channel].minus(new Complex(avg, 0));
					data_counter++;
					compressed_index++;
				}
				if(compressed_index >= zerod_compressed_data.length){
					break;
				}
				if (parsed_data[sample][channel].equals(NUMBER_MARKER)){
					avg = average(channel,sample-50,sample-1,parsed_data);
					marker_flag = 1;
				}
				if(data_counter == NUM_DATA_POINTS){
					marker_flag = 0;
					data_counter = 0;
					avg = 0;
				}
			}
		}
		return zerod_compressed_data;
	}
	
	public double average(int channel, int beg, int end, Complex[][] arr){
		double total = 0.0;
		for (int sampleIndex = beg; sampleIndex < end; sampleIndex++){
			total += arr[sampleIndex][channel].re();
		}
		return total/(end-beg);
	}
	
	/*
	 * Citation: the FFT function used in this code was taken from Algorithms Fourth Edition 
	 * by Robert Sedgewick and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: http://introcs.cs.princeton.edu/java/97data/FFT.java.html
	 */
	
	public static Complex[][] bandpassfilter(Complex[][] data, int samprate, int lpfreq, int hpfreq){
		int num_data = data.length;
		int array_index = 0;
		Complex[] single_trial_filtereddata = new Complex[NUM_DATA_POINTS];
		Complex[] fft_single_trial_filtereddata = new Complex[NUM_DATA_POINTS];
		Complex[][] filtereddata = new Complex[num_data][NUM_CHANNELS];
		
		for(int channel = 0; channel < NUM_CHANNELS; channel++){
			for(int trial_count = 0; trial_count<num_trials_global;trial_count++){
				for(int sampleIndex = 0; sampleIndex< NUM_DATA_POINTS; sampleIndex++){
					single_trial_filtereddata[sampleIndex] = data[NUM_DATA_POINTS*trial_count + sampleIndex][channel];
				}
				
				fft_single_trial_filtereddata = Channel.fft(single_trial_filtereddata);
				
				for(int freq = 0; freq < NUM_DATA_POINTS; freq++){
					double curFreq = (double)freq * samprate / NUM_DATA_POINTS;
					if(curFreq >= lpfreq && curFreq <= hpfreq){
						fft_single_trial_filtereddata[freq] = fft_single_trial_filtereddata[freq].scale(2);
					}
					else{
						fft_single_trial_filtereddata[freq] = fft_single_trial_filtereddata[freq].scale(0);
					}
				}
				
				for(int copy_counter = 0; copy_counter < NUM_DATA_POINTS; copy_counter++){
					filtereddata[array_index][channel] = fft_single_trial_filtereddata[copy_counter];
					array_index+=1;
				}
			}
			array_index = 0;
		}
		return filtereddata;
	}
	
	public void writeToFile(String destination, int trial_type, String freq_range){
		try{
			int sample_index = 0;
			
			for (int channel = 0; channel < NUM_CHANNELS; channel++){
				File file = new File(destination + "\\Channel"+channel+"_"+freq_range+".txt");
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				sample_index = 0;
				for (int sampleIndex = 0; sampleIndex < filteredData.length; sampleIndex++){
					if (sample_index >= NUM_DATA_POINTS){
						sample_index = 0;
						out.print("\n");
					}
					
					if(sample_index==0){
						out.print(trial_type + " ");
					}
					
					if (filteredData[sampleIndex][channel].re() == 0 && filteredData[sampleIndex][channel].im()== 0){
						sample_index++;
						continue;
					}
					
					out.print(sample_index+":"+filteredData[sampleIndex][channel].abs());
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
		RawFilter test = new RawFilter("src/RawDataFolder","src/FilteredDataFolder", 8);
	}
}
