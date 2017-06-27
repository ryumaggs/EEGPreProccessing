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
	final String MARKER = "CHANGED MY IMAGE HERE BOYS";
	
	/*
	 * for each file in the directory:
	 * open it
	 * transpose it
	 * fft
	 * filter
	 * (convert back) yes
	 * write to a new data file, yes
	 */
	public RawFilter(String dirPath, String destination, int chan, int numsample){
		System.out.println(dirPath);
		File folder = new File(dirPath);
		System.out.println(folder.isDirectory());
		File[] listOfFiles = folder.listFiles();
		System.out.println(listOfFiles.length);
		//parsedunfilteredData = new Complex[numsample][chan];
		
		for(File file : listOfFiles){
			if (file.isFile()){
				num_trials = 0;
				int tp = check_type(file);
				//puts non-transposed, non-filtered data into parsedunfilteredData
				parsedunfilteredData = parseData(file, chan, numsample);
				//filteredData = new Complex[parsedunfilteredData[0].length][parsedunfilteredData.length];
				transposed = new Complex[parsedunfilteredData[0].length][parsedunfilteredData.length];
				//inversed = new Complex[parsedunfilteredData[0].length][parsedunfilteredData.length];
				System.out.println("finished parsing data");
				//transposes the data into transposed
				transposed = transpose(parsedunfilteredData);
				System.out.println("successfully transposed");
				//zeros each trial with data preceeding the 2nd image change
				zero_data();
				System.out.println("zerod out eh data");
				//compresses the data to keep only what we want
				compressed = compress_array(8);
				System.out.println("compressed has: " + compressed.length + " channels");
				System.out.println("and is comprised of: " + compressed[0].length + " samples");
				System.out.println("compressed data: ");
				//for (Complex[] row:compressed){
				//	System.out.println(Arrays.toString(row));
				//}
				//Filters data via FFT into filteredData
				filteredData = bandpassfilter(compressed, 256, 0, 40);
				//Inverses back from FFT
				inversed = inverseFFT2x2(filteredData);
				/*for (int i = 0; i < transposed.length; i++){
					System.out.println(Arrays.toString(inversed[i]));
				}*/
				writeToFile(destination,tp,inversed);
			}
		}
	}
	
	public int check_type(File file){
		//System.out.print(file.getName());
		String name = file.getName();
		for (int i = 0; i < CLOSED.length(); i++){
			if (!(name.substring(i,i+1).equals(CLOSED.substring(i, i+1)))){
				return -1;
			}
		}
	
		return 1;
	}
	
	//Given a OpenBCI file, parse it into a 2D array of timesample by channels
	private Complex[][] parseData(File dataFile, int chan, int numsample){
		Scanner scan;
		String s="";
		int counter = 0;
		ArrayList<Complex[]> nonTransposed = new ArrayList<Complex[]>();
		Complex[][] array_nonTransposed;
		try {
			scan = new Scanner(dataFile);
			for(int i=0; i <=5; i++){
				//scan.nextLine();
				System.out.println(scan.nextLine());
			}
			while(scan.hasNextLine()){
				s = scan.nextLine();
				//System.out.println("s is: " + s);
				//System.out.println(s);
				//adjust the comparison when actual marker is decided
				if (!(s.equals(MARKER)))
					nonTransposed.add(trialParser(s, chan, counter));
				else
					nonTransposed.add(trialParserMark(chan,counter));
				counter++;
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
	
	//Parse a line of data into an array channels for a given timesample
	//and append it to the next index in the parsedunfilteredData
	private Complex[] trialParser(String timesample, int chan, int sampleidx){
		//System.out.println(timesample);
		String[] curData = timesample.split(" ");
		Complex[] curChan = new Complex[chan];
		//System.out.println("curChan length is: " + curData.length);
		double curVal;
		for(int c=0; c<chan; c++){
			curVal = Double.parseDouble(curData[c+1]);
			curChan[c] = new Complex(curVal, 0);
		}
		return curChan;
	}
	
	private Complex[] trialParserMark(int chan, int sampleidx){
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
	
	public Complex[][] getParsedData(){
		return parsedunfilteredData;
	}
	
	public void reference_sub(){
		
	}
	
	public void zero_data(){
		double avg = 0.0;
		int flag = 0;
		int counter = 0;
		for (int i = 0; i < transposed.length; i++){
			for (int j = 0; j < transposed[i].length; j++){
				if (flag == 1){
					//System.out.println("old value was: " + transposed[i][j]);
					transposed[i][j] = transposed[i][j].minus(new Complex(avg, 0));
					//System.out.println("new value is: " + transposed[i][j]);
					counter++;
				}
				if (transposed[i][j].equals(new Complex(123,456))){
					//this may break if there are not 50 data points before the image change, but there should be
					avg = average(i,j-50,j-1);
					flag = 1;
					num_trials+=1;
				}
				if(counter == 256){
					flag = 0;
					counter = 0;
					avg = 0;
				}
			}
		}
	}
	
	private Complex[][] compress_array(int chan){
		int flag = 0;
		int compressed_col = 0;
		num_trials = num_trials/8;
		System.out.println("num trials = : " + num_trials);
		Complex[][] compressed = new Complex[chan][num_trials*256];
		//System.out.println(transposed.length + " asjdaisjdiajdiajs ");
		for (int i =0; i < chan; i++){
			for (int j = 0; j < transposed[i].length; j++){
				if (flag == 1){
					compressed[i][compressed_col] = transposed[i][j];
					compressed_col +=1;
				}
				if (transposed[i][j].equals(new Complex(123,456))){
					//System.out.println("flag has been triggered");
					flag = 1;
				}
				if (compressed_col == (num_trials*256)){
					//System.out.println("flag has been detriggered");
					compressed_col = 0;
					flag = 0;
					break;
				}
			}
		}
		
		return compressed;
	}
	
	public double average(int row, int beg, int end){
		double total = 0.0;
		for (int i = beg; i < end; i++){
			total += transposed[row][i].re();
		}
		return total/(end-beg);
	}
	
	public static Complex[][] bandpassfilter(Complex[][] data, int samprate, int lpfreq, int hpfreq){
		int ccount = data.length;
		int tcount = data[0].length;
		//System.out.println("there are: " + ccount + " channels");
		//System.out.println("and: " + tcount + " samples");
		Complex[][] filtereddata = new Complex[ccount][tcount];
		for(int channel=0; channel<ccount; channel++){
			//System.out.println("looking at channel: " + channel);
			filtereddata[channel] = Channel.fft(data[channel]);
			//System.out.println(Arrays.toString(filtereddata[channel]));
		}
		for(int channel=0; channel<ccount; channel++){
			for(int freq=0; freq<tcount; freq++){
				double curFreq = (double)freq * samprate / tcount;
				//System.out.println("looking at frequency of : " + curFreq);
				if(curFreq >= lpfreq && curFreq <= hpfreq){
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(2);
				}
				else{
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(0);
				}
			}
			//System.out.println(Arrays.toString(filtereddata[channel]));
		}
		return filtereddata;
	}
	
	public static Complex[][] inverseFFT2x2(Complex[][] filterData){
		Complex[][] ret = new Complex[filterData.length][(filterData[0].length)];
		for (int channel1 = 0; channel1 < filterData.length; channel1++){
			ret[channel1] = Channel.ifft(filterData[channel1]);
			//System.out.println(Arrays.toString(ret[channel1]));
		}
		return ret;
	}
	
	public void writeToFile(String destination, int trial_type, Complex[][] filteredData){
		try{
			int sample_count = 0;
			//go through each channel (row), and add each channel data to its respective file
			for (int i = 0; i < compressed.length; i++){
				//System.out.println("in here asidjaidja");
				//consider adding in empty sections because that's still data.
				//if (isEmptyArray(filteredData[i])== false){
				File file = new File(destination + "\\Channel"+i+".txt");
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				//need to adjust the bounds possibly
				for (int j = 0; j < compressed[i].length; j++){
					//System.out.println("accessed second for loop");
					if(sample_count==0){
						out.print(trial_type + " ");
					}
					if (compressed[i][j].re() == 0 && compressed[i][j].im()== 0){
						sample_count++;
						continue;
					}
					//System.out.println("compressedij is: " + compressed[i][j]);
					out.print(sample_count+":"+compressed[i][j].abs()+" ");
					sample_count++;
					if (sample_count >= 256){
						sample_count = 0;
						out.print("\n");
					}
				}
				out.close();
			}
		} catch (IOException e){}
	}
	
	public static boolean isEmptyArray(Complex[] arr){
		for (int i = 0; i < arr.length; i++){
			if (arr[i].re() != 0.0 && arr[i].im() != 0.0)
				return false;
		}
		return true;
	}
	
	public static void main(String[] args){
		RawFilter test = new RawFilter("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\tester","C:\\Users\\Ryan Yu\\workspace\\ImportantFreq", 8, 1024);
		//Complex[][] data = test.getParsedData();
		//Complex[][] filtereddata = bandpassfilter(data, 250, 8, 12);
		//String dirp = "C:/Users/'Ryan Yu'/Desktop/application.windows64/SavedData";
		//File asdasd = new File(dirp);
		//if (asdasd.isDirectory()){
			//System.out.println("dirp is a directory");
		//}
		//File[] listOfFiles = asdasd.listFiles();
		//System.out.println(Arrays.toString(listOfFiles));
		//System.out.println("got here1111");
		//for (int i = 0; i < listOfFiles.length; i++){
			//System.out.println("got here");
			//System.out.println(listOfFiles[i].getName());
		//}
		//new RawFilter(dirp,8,16);
	}
}
