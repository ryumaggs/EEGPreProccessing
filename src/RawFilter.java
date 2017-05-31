import java.util.*;
import java.io.*;

public class RawFilter {
	public ArrayList<double[]> filteredData;
	private ArrayList<double[]> parsedunfilteredData;
	
	public RawFilter(String rawData, int chan){
		parsedunfilteredData = new ArrayList<double[]>();
		parseData(rawData, chan);
	}
	
	//
	private void parseData(String rawData, int chan){
		File file = new File(rawData);
		Scanner scan;
		try {
			scan = new Scanner(file);
			for(int i=0; i <=5; i++){
				System.out.println(scan.nextLine());
			}
			while(scan.hasNext()){
				trialParser(scan.nextLine(), chan);
			}
			System.out.println(Arrays.toString(parsedunfilteredData.get(0)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//For each line of data, parse it into 
	private void trialParser(String timesample, int chan){
		String[] curData = timesample.split(", ");
		double[] curChan = new double[chan];
		for(int c=0; c<chan; c++){
			curChan[c] = Double.parseDouble(curData[c+1]);
		}
		parsedunfilteredData.add(curChan);
	}
	
	public static void main(String[] args){
		RawFilter test = new RawFilter("BCItester.txt", 4);
	}
}
