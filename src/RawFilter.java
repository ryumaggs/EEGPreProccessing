import java.util.*;
import java.io.*;

public class RawFilter {
	private static int num_trials_global = 40;
	private static final int NUM_DATA_POINTS = 1000;
	private static final Complex NUMBER_MARKER = new Complex(123,456);
	private static final int NUM_CHANNELS = 32;
	
	private Complex[][] parsedunfilteredData;
	private Complex[][] filteredData;
	private Complex[][] compressedData;
	
	final String CLOSED = "CLOSED";
	final String OPEN = "OPEN";
	final String MARKER = "MARKER";
	
	/*
	 * Citation: the Complex class and objects used throughout this code was taken from Algorithms Fourth Edition 
	 * by Robert Sedgewick and Kevin Wayne, and found on the princeton.edu CS department website
	 * URL: algs4.cs.princeton.edu/99scientific/Complex.java.html
	 */
	public RawFilter(String dirPath, String destination, int chan, int inverse){
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		long time_begin = 0;
		long time_end = 0;
		long total_time = 0;
		for(File file : listOfFiles){
			System.out.println(file.getName());
			if (file.isDirectory()){
				File secFolder = file;
				File[] listOfFiles2 = secFolder.listFiles();
				for(File file2 : listOfFiles2){
					System.out.println(file2.getName());
					time_begin = System.currentTimeMillis();
					num_trials_global = 0;
				
					System.out.println("\n-----------------------");
					int trial_type = check_file_type(file2);
					if(!(file2.getName().substring(0,1).equals("b")))
						continue;
					parsedunfilteredData = parseRecordingData(file2);
					System.out.println("finished parsing data");
				
					//compressedData= baselineCorrect_and_Compress_data(parsedunfilteredData);
					//System.out.println("compressed and baseline corrected the data");
					compressedData = parsedunfilteredData;
					int low_frequency_bound = 0;
					int high_frequency_bound = 40;
					
					//System.out.println(compressedData[0].length);
//					for(int i = 0; i < 1; i++){
//						System.out.println("");
//						for(int j = 0; j < compressedData[i].length; j++){
//							System.out.print(compressedData[i][j]+" ");
//						}
//					}
					//System.exit(1);
					filteredData = bandpassfilter(compressedData, 1000, low_frequency_bound, high_frequency_bound);
					
					writeToFile(file.getAbsolutePath(),trial_type,Integer.toString(low_frequency_bound)+Integer.toString(high_frequency_bound));
				
					System.out.println("bandpassfiltered and saved the data for freq range: " + low_frequency_bound +"," + high_frequency_bound);
				}
				time_end = System.currentTimeMillis();
				total_time += (time_end - time_begin);
			}
			System.out.println("-----------------------");
			System.out.println("\ntotal time taken: " + total_time + " milliseconds");
			System.out.println("\nDone.");
		}
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
		Scanner scan;
		String s="";
		ArrayList<Complex[]> nonTransposedData = new ArrayList<Complex[]>();
		Complex[][] array_nonTransposedData;
		try {
			scan = new Scanner(dataFile);
			for(int trial = 0; trial < 40; trial++){
				//skip first 100 lines of each trial
				for(int i=0; i <100; i++){
					scan.nextLine();
				}
				//record the next 1000
				for(int i = 0; i < 1000; i++){
					s = scan.nextLine();

					nonTransposedData.add(String_to_Complex_Arr(s));

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
			curVal = Double.parseDouble(curData[c]);
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
		Complex[][] zerod_compressed_data = new Complex[40*NUM_DATA_POINTS][NUM_CHANNELS];
		for (int channel = 0; channel < NUM_CHANNELS; channel++){
			int cur_samp = 0;
			int compressed_index = 0;
			for(int trial = 0; trial < 40; trial++){
				double aveg = average(channel, cur_samp, cur_samp+50, parsed_data);
				for (int samp = (cur_samp + 50); samp < (cur_samp+50) + NUM_DATA_POINTS; samp++){
					zerod_compressed_data[compressed_index][channel] = parsed_data[samp][channel].minus(new Complex(aveg, 0));
					compressed_index++;
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
		int num_data = data.length+(24*40);
		int array_index = 0;
		Complex[] single_trial_filtereddata = new Complex[1024];
		Complex[] fft_single_trial_filtereddata = new Complex[1024];
		Complex[][] filtereddata = new Complex[num_data][NUM_CHANNELS];
		
		for(int channel = 0; channel < NUM_CHANNELS; channel++){
			array_index = 0;
			for(int trial_count = 0; trial_count<40;trial_count++){
				//System.out.println(trial_count);
				single_trial_filtereddata = resetToZero(single_trial_filtereddata);
				
				for(int sampleIndex = (trial_count * 1000); sampleIndex< (trial_count * 1000) + 1000; sampleIndex++){
					//System.out.println(sampleIndex);
					single_trial_filtereddata[sampleIndex%1000] = data[sampleIndex][channel];
				}
//				for(int i = 0; i < single_trial_filtereddata.length; i++){
//					System.out.print(single_trial_filtereddata[i]+" ");
//				}
//				System.exit(1);
				fft_single_trial_filtereddata = Complex.fft(single_trial_filtereddata);
//				for(int i = 0; i < fft_single_trial_filtereddata.length; i++){
//					System.out.print(fft_single_trial_filtereddata[i].toString()+", ");
//				}
//				System.exit(1);
				//reset the DC
				fft_single_trial_filtereddata[0] = new Complex(0,0);
				for(int freq = 0; freq < 1024; freq++){
					double curFreq = (double)freq * samprate / 1024;
					if(curFreq >= lpfreq && curFreq <= hpfreq){
						fft_single_trial_filtereddata[freq] = fft_single_trial_filtereddata[freq].scale(2);
					}
					else{
						fft_single_trial_filtereddata[freq] = fft_single_trial_filtereddata[freq].scale(0);
					}
				}
				
				for(int copy_counter = 0; copy_counter < 1024; copy_counter++){
					filtereddata[array_index][channel] = fft_single_trial_filtereddata[copy_counter];
					array_index+=1;
				}
			}
			array_index = 0;
		}
		return filtereddata;
	}
	
	public static Complex[] resetToZero(Complex[] arr){
		Complex[] ret = new Complex[arr.length];
		for(int i = 0; i < ret.length; i++){
			ret[i] = new Complex(0,0);
		}
		return ret;
	}
	public static void addZeros(Complex[] arr){
		for(int i = 1000; i < 1024; i++){
			arr[i] = new Complex(0,0);
		}
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
					if (sample_index >= NUM_DATA_POINTS+24){
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
	
	public void plot_to_file (String destination, int trial_type, String freq_range){
		try{
			System.out.println("plotting to file");
			File file = new File(destination + "\\plot_"+freq_range+".txt");
			FileWriter fw = new FileWriter(file,true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			
			for(int sampIndex = 0; sampIndex < filteredData.length; sampIndex++){
				for(int channel = 0; channel < NUM_CHANNELS; channel++){
					out.print(filteredData[sampIndex][channel].abs());
					out.print(" ");
				}
				out.print("\n");
			}
			out.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	//main function is for testing purposes
	public static void main(String[] args){
		RawFilter test = new RawFilter("C:/Users/Ryan Yu/Desktop/SeniorThesis Data/4/b2","C:/Users/Ryan Yu/Desktop/SeniorThesis Data/4/b2", 33,0);
	}
}
