import java.util.*;
import java.io.*;

public class RawFilter {
	private Complex[][] parsedunfilteredData;
	
	public RawFilter(String rawData, int chan, int numsample){
		parsedunfilteredData = new Complex[numsample][chan];
		parseData(rawData, chan, numsample);
		Complex[][] transposed = transpose(parsedunfilteredData);
		parsedunfilteredData = transposed;
	}
	
	//Given a OpenBCI file, parse it into a 2D array of timesample by channels
	private void parseData(String rawData, int chan, int numsample){
		File file = new File(rawData);
		Scanner scan;
		try {
			scan = new Scanner(file);
			for(int i=0; i <=5; i++){
				System.out.println(scan.nextLine());
			}
			for(int idx=0; idx<numsample; idx++){
				trialParser(scan.nextLine(), chan, idx);
			}
//			for(int i=0; i<256; i++){
//				System.out.println(parsedunfilteredData[i][3].re());
//			}
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
					filtereddata[channel][freq] = filtereddata[channel][freq].scale(0);
				}
			}
		}
		return filtereddata;
	}
	
	public static void main(String[] args){
		RawFilter test = new RawFilter("BCItester.txt", 4, 256);
		Complex[][] data = test.getParsedData();
		Complex[][] filtereddata = bandpassfilter(data, 250, 8, 12);
	}
}
