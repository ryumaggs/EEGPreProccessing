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
	 * (convert back?) maybe
	 * write to a new data file? maybe
	 */
	public RawFilter(String dirPath, int chan, int numsample){
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		
		for(File file : listOfFiles){
			if (file.isFile()){
				parsedunfilteredData = new Complex[numsample][chan];
				filteredData = new Complex[numsample][chan];
				parseData(file, chan, numsample);
				Complex[][] transposed = transpose(parsedunfilteredData);
				parsedunfilteredData = transposed;
		
				filteredData = bandpassfilter(parsedunfilteredData, 250, 8, 12);
				for (int i = 0; i < filteredData.length; i++){
					System.out.println(Arrays.toString(parsedunfilteredData[i]));
				}
			}
		}
	}
	
	//Given a OpenBCI file, parse it into a 2D array of timesample by channels
	private void parseData(File dataFile, int chan, int numsample){
		Scanner scan;
		try {
			scan = new Scanner(dataFile);
			for(int i=0; i <=5; i++){
				System.out.println(scan.nextLine());
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
		int ccount = inputarray[0].length;
		int tcount = inputarray.length;
		Complex[][] tArray = new Complex[ccount][tcount];
		for(int channel=0; channel<ccount; channel++){
			for(int samp=0; samp<tcount; samp++){
				tArray[channel][samp] = inputarray[samp][channel];
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
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(.1);
				}
			}
		}
		return filtereddata;
	}
	
	public static void readFilesInDir(String fileName){
		//nothing
	}
	
	public static void writeToFile(String destination, String fileName, Complex[][] filteredData){
		try{
			File file = new File(destination + "\\" + fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			for (int i = 0; i < filteredData.length; i++){
				for (int j = 0; j < filteredData[i].length; j++){
					bw.write(filteredData[i][j].toString() + ((j == filteredData.length-1) ? "" : ","));
				}
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e){}
	}
	
	public static void main(String[] args){
		//RawFilter test = new RawFilter("BCItester.txt", 4, 256);
		//Complex[][] data = test.getParsedData();
		//Complex[][] filtereddata = bandpassfilter(data, 250, 8, 12);
		
	}
}
