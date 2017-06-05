import java.util.*;
import java.io.*;

public class RawFilter {
	private Complex[][] parsedunfilteredData;
	private Complex[][] filteredData;
	
	/*
	 * for each file in the directory:
	 * open it
	 * transpose it
	 * fft
	 * filter
	 * (convert back) maybe
	 * write to a new data file? maybe
	 */
	public RawFilter(String dirPath, String destination, int chan, int numsample){
		//System.out.println(dirPath);
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		
		for(File file : listOfFiles){
			if (file.isFile()){
				parsedunfilteredData = new Complex[numsample][chan];
				filteredData = new Complex[chan][numsample];
				parseData(file, chan, numsample);
				Complex[][] transposed = new Complex[chan][numsample];
				transposed = transpose(parsedunfilteredData);
				filteredData = bandpassfilter(transposed, 250, 8, 12);
				for (int i = 0; i < filteredData.length; i++){
					System.out.println(Arrays.toString(filteredData[i]));
				}
				writeToFile(destination,1,filteredData);
			}
		}
	}
	
	//Given a OpenBCI file, parse it into a 2D array of timesample by channels
	private void parseData(File dataFile, int chan, int numsample){
		Scanner scan;
		try {
			scan = new Scanner(dataFile);
			for(int i=0; i <=5; i++){
				scan.nextLine();
			}
			for(int idx=0; idx<numsample; idx++){
				trialParser(scan.nextLine(), chan, idx);
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
		int num_channels = inputarray[0].length; //original collumns (8)
		int num_input = inputarray.length; // original rows (16)
		Complex[][] tArray = new Complex[num_channels][num_input];
		//System.out.println("tArray is a : " + num_channels + " by " + num_input + " array");
		for(int coll=0; coll<num_channels; coll++){ // loop through collumns of original
			for(int row=0; row<num_input; row++){ // loop through each row of each collumn
				tArray[coll][row] = inputarray[row][coll]; 
			}
		}
		return tArray;
	}
	
	public Complex[][] getParsedData(){
		return parsedunfilteredData;
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
				if(curFreq >= lpfreq && curFreq <= hpfreq){
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(2);
				}
				else{
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(0);
				}
			}
			//need to add reverse fft here
		}
		return filtereddata;
	}
	
	public static void writeToFile(String destination, int trial_type, Complex[][] filteredData){
		try{
			//go through each channel (row), and add each channel data to its respective file
			for (int i = 0; i < filteredData.length; i++){
				//need to adjust the filename here
				//if the channel file exists, just append as a new line to channel data
				File file = new File(destination + "\\Channel"+(i+1));
				if (file.exists()){
					FileWriter fw = new FileWriter(destination+"\\Channel"+i,true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw);
					
					out.println("file exists and currently writing data");
					for (int j = 0; j < filteredData[i].length; j++){
						out.print(filteredData[i][j].toString() + ((j == filteredData[j].length-1) ? ", 1\n" : ","));
					}
					out.close();
				}
				//if channel file doesn't exist, create new channel file and write to it
				else{
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					bw.write("data didn't exist halp me");
					for (int j = 0; j < filteredData[i].length; j++){
						bw.write(filteredData[i][j].toString() + ((j == filteredData[j].length-1) ? ", 1" : ","));
					}
					bw.newLine();
					bw.flush();
					bw.close();
				}
			}
		} catch (IOException e){}
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
