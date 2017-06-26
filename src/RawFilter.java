import java.util.*;
import java.io.*;

public class RawFilter {
	private Complex[][] parsedunfilteredData;
	private Complex[][] filteredData;
	final String CLOSED = "CLOSED";
	final String OPEN = "OPEN";
	final String MARKER = "";
	
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
		//System.out.println(dirPath);
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		
		for(File file : listOfFiles){
			if (file.isFile()){
				int tp = check_type(file);
				parsedunfilteredData = new Complex[numsample][chan];
				filteredData = new Complex[chan][numsample];
				parseData(file, chan, numsample);
				Complex[][] transposed = new Complex[chan][numsample];
				transposed = transpose(parsedunfilteredData);
				filteredData = bandpassfilter(transposed, 250, 8, 12);
				Complex[][] inversed = new Complex[chan][numsample];
				inversed = inverseFFT2x2(filteredData);
				for (int i = 0; i < transposed.length; i++){
					System.out.println(Arrays.toString(inversed[i]));
				}
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
	private void parseData(File dataFile, int chan, int numsample){
		Scanner scan;
		String s;
		try {
			scan = new Scanner(dataFile);
			for(int i=0; i <=5; i++){
				scan.nextLine();
			}
			for(int idx=0; idx<numsample; idx++){
				s = scan.nextLine();
				//adjust the comparison when actual marker is decided
				if (!(s.equals(MARKER)))
					trialParser(s, chan, idx);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//Parse a line of data into an array channels for a given timesample
	//and append it to the next index in the parsedunfilteredData
	private void trialParser(String timesample, int chan, int sampleidx){
		String[] curData = timesample.split(", ");
		Complex[] curChan = new Complex[chan];
		double curVal;
		for(int c=0; c<chan; c++){
			curVal = Double.parseDouble(curData[c+1]);
			curChan[c] = new Complex(curVal, 0);
		}
		parsedunfilteredData[sampleidx] = curChan;
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
		
	}
	
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
				//System.out.println("looking at frequency of : " + curFreq);
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
	
	public static Complex[][] inverseFFT2x2(Complex[][] filterData){
		Complex[][] ret = new Complex[filterData.length][(filterData[0].length)];
		for (int channel1 = 0; channel1 < filterData.length; channel1++){
			ret[channel1] = Channel.ifft(filterData[channel1]);
		}
		return ret;
	}
	
	public static void writeToFile(String destination, int trial_type, Complex[][] filteredData){
		try{
			//go through each channel (row), and add each channel data to its respective file
			for (int i = 0; i < filteredData.length; i++){
				//consider adding in empty sections because that's still data.
				if (isEmptyArray(filteredData[i])== false){
					File file = new File(destination + "\\Channel"+i+".txt");
					FileWriter fw = new FileWriter(file,true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw);
					out.print(trial_type + " ");
					for (int j = 0; j < filteredData[i].length; j++){
						if (filteredData[i][j].re() != 0 && filteredData[i][j].im() != 0){
							out.print(j+":"+filteredData[i][j].abs()+" ");
						}
					}
					out.print("\n");
					out.close();
				}
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
		//RawFilter test = new RawFilter("BCItester.txt", 4, 256);
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
