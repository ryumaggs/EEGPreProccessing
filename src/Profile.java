import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
import java.util.Arrays;

public class Profile {
	String name;
	double[] weights;
	double best_thresh;
	
	public Profile(String name){
		this.name = name;
		//setWeights();
	}
	
	public Profile(){
		name = "";
		weights = new double[8];
		best_thresh = 0.0;
	}
	
	public static void load_profile(String filepath, Profile profile){
		File file = new File(filepath);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			profile.name = br.readLine();
			String[] s = br.readLine().split(", ");
			for(int i = 0; i < s.length; i++){
				profile.weights[i] = Double.parseDouble(s[i]);
			}
			br.readLine();
			profile.best_thresh = Double.parseDouble(br.readLine());
			br.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	public int check_header(String in, String comp){
		for (int i =0; i < comp.length(); i++){
			if (in.charAt(i) != comp.charAt(i))
				return -1;
		}
		return 1;
	}
	
	public void best_freq_range(){
		File folder = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder");
		File[] listOfFiles = folder.listFiles();
		int counter = 0;
		String[] rangeAccuracy = new String[listOfFiles.length];
		String file_name;
		try{
			for(File file: listOfFiles){
				file_name = file.getName();
				System.out.println("looking at: " + file_name);
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_train -v 3 \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\"");
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				rangeAccuracy[counter] = file_name;
				String input;
				while (true){
					input = r.readLine();
					//System.out.println(input);
					if(input!=null && check_header(input,"Cross") == 1){
						rangeAccuracy[counter] = rangeAccuracy[counter] + " " + input;
						System.out.println(rangeAccuracy[counter]);
						counter++;
						break;
					}
				}
			}
		}catch (IOException e){e.printStackTrace();}
//		for(int i = 0; i<rangeAccuracy.length; i++){
//			System.out.println(rangeAccuracy[i]);
//		}
	}
	
	public int[][] load_data(){
		int[][] dataHold = new int[104][8];
		try{
			File folder = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder");
			File[] listOfFiles = folder.listFiles();
			int counter = 0;
			String file_name;
			
			for(File file : listOfFiles){
				int indexCount = 0;
				file_name = file.getName();
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_predict \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\" \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\EyesAlpha\\"+file_name+".model\"");
				//builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String input;
				
				while(true){
					input = r.readLine();
					if(input!=null)
						//System.out.println(input);
					if(input.equals("V from the prediction is: -1.0")){
						//System.out.println("hit -1");
						dataHold[indexCount][counter] = 0;
						indexCount+=1;
					}
					else if(input.equals("V from the prediction is: 1.0")){
						//System.out.println("hit 1");
						dataHold[indexCount][counter] = 1;
						indexCount+=1;
					}
					else if(check_header(input,"aaa") == 1){
						break;
					}
				}
				counter++;
			}
		}catch(IOException e){e.printStackTrace();}
		return dataHold;
	}
	
	public void setWeights(){
		try{
			File folder = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder");
			File[] listOfFiles = folder.listFiles();
			weights = new double[8];
			int counter = 0;
			double correct = 0;
			double total = 0;
			String file_name;
			
			for(File file : listOfFiles){
				file_name = file.getName();
				System.out.println("looking at file: " + file_name);
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_train -v 3 \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\"");
				builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String input;
				while(true){
					input = r.readLine();
					if(input!=null){
						System.out.println(input);
						if(check_header(input,"aaa")==1){
							correct = Double.parseDouble(r.readLine());
							total = Double.parseDouble(r.readLine());
							weights[counter] = (correct/total);
							counter++;
							break;
						}
					}
				}
			}
		}catch(IOException e){e.printStackTrace();}
	}
	
	public double[] test_weights(int[][] data, double[]weights){
		double[] trial_sum = new double[104];
		for (int i = 0; i < data.length; i++){
			double combinedW = 0;
			for(int j = 0; j < data[i].length; j++){
				combinedW += (double)data[i][j] * weights[j];
			}
			trial_sum[i] = combinedW;
		}
		return trial_sum;
	}
	
	public double find_threshhold(double[] sums){
		double currentBest = 1.0;
		double thresh = 1.0;
		double bestPercent = .5;
		double total = 104.0;
		while(true){
			double correct = 0;
			for(int i = 0; i < sums.length; i++){
				if(i < 56 && sums[i] > thresh){
					correct+=1.0;
				}
				if(i > 56 && sums[i] < thresh){
					correct+=1.0;
				}
			}
			if(correct/total > bestPercent){
				bestPercent = correct/total;
				currentBest = thresh;
			}
			thresh = thresh + .01;
			if(thresh > 4)
				break;
		}
		System.out.println("best threshhold is: " + currentBest + " with accuracy: " + bestPercent);
		return currentBest;
	}
	
	public static void save_profile(Profile toSave){
		File file = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\"+toSave.name+".profile");
		try{
			PrintWriter writer = new PrintWriter(file,"UTF-8");
			writer.println(toSave.name);
			writer.println(doubleArray_to_string(toSave.weights));
			writer.println(toSave.best_thresh);
			writer.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	public static String doubleArray_to_string(double[] dub){
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < dub.length; i++){
			ret.append(dub[i]);
			if (i != dub.length -1)
				ret.append(", ");
			else
				ret.append("\n");
		}
		return ret.toString();
	}
	public static void main(String args[]){
		Profile bob = new Profile("bob");
//		System.out.println(Arrays.toString(bob.weights));
//		int[][] dat = bob.load_data();
//		double[] sum = bob.test_weights(dat, bob.weights);
//		double b_thresh = bob.find_threshhold(sum);
// 		bob.best_thresh = b_thresh;
		bob.best_freq_range();
	}
}
