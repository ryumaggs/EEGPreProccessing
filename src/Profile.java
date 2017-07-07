import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
import java.util.Arrays;

public class Profile {
	String name;
	//LogRegression logistic;
	double[] weights;
	double best_thresh;
	
	public Profile(String name){
		this.name = name;
		//logistic = new LogRegression(8);
		setWeights();
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
//	public static double sigmoid(double z){
//		return 1.0/(1.0 + Math.exp(-z));
//	}
//	
//	public class LogRegression{
//		/*
//		 * A simple logistic regression algorithm based off of tpeng and Mattheiu Labas
//		 */
//		private double rate;
//		private double[] weightz;
//		private int ITERATIONS = 3000;
//		
//		public LogRegression(int n){
//			this.rate = 0.0001;
//			weightz = new double[n];
//		}
//		
//		public void train(List<Instance> instances){
//			for(int n = 0; n < ITERATIONS; n++){
//				for(int i = 0; i < instances.size(); i++){
//					int[] x =  instances.get(i).x;
//					double predicted = classify(x);
//					int label = instances.get(i).label;
//					for(int j = 0; j < weightz.length; j++){
//						weightz[j] = weightz[j] + (rate * (label - predicted) * x[j]);
//					}
//				}
//			}
//		}
//		
//		private double classify(int[] x){
//			double logit = .0;
//			for(int i = 0; i < weightz.length; i++){
//				logit += weightz[i] * x[i];
//			}
//			
//			return sigmoid(logit);
//		}
//	}
//	
//	public static class Instance{
//		public int label;
//		public int[] x;
//		
//		public Instance(int label, int[] x){
//			this.label = label;
//			this.x = x;
//		}
//	}
	
	public int check_header(String in){
		for (int i =0; i < 3; i++){
			if (in.charAt(i) != 'a')
				return -1;
		}
		return 1;
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
					else if(check_header(input) == 1){
						break;
					}
				}
				counter++;
			}
		}catch(IOException e){e.printStackTrace();}
		return dataHold;
	}
	
//	public static List<Instance> convert_to_instance(int[][] data){
//		List<Instance> dataset = new ArrayList<Instance>();
//		int label = 0;
//		for(int i =0; i < data.length; i++){
//			if (i < 56)
//				label = 1;
//			else
//				label = 0;
//			Instance instance = new Instance(label,data[i]);
//			dataset.add(instance);
//		}
//		return dataset;
//	}
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
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_predict \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\" \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\EyesAlpha\\"+file_name+".model\"");
				builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String input;
				while(true){
					input = r.readLine();
					if(input!=null){
						if(check_header(input)==1){
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
		//double correct = 0.0;
		Profile bob = new Profile("bob");
		System.out.println(Arrays.toString(bob.weights));
		int[][] dat = bob.load_data();
		double[] sum = bob.test_weights(dat, bob.weights);
		double b_thresh = bob.find_threshhold(sum);
 		bob.best_thresh = b_thresh;
		//save_profile(bob);
		//System.out.println("saved profile");
		
		/*Profile bobclone = new Profile();
		load_profile("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\bob.profile",bobclone);
		System.out.println("name of clone: " + bobclone.name);
		System.out.println("weights are: ");
		for (int i = 0; i < bobclone.weights.length; i++)
			System.out.print(bobclone.weights[i] + ", ");
		System.out.println("");
		System.out.println("best thresh is: " + bobclone.best_thresh);*/
		/*List<Instance> dset = convert_to_instance(dat);
		bob.logistic.train(dset);
		for(int i = 0; i < 104; i++){
			double prediction = bob.logistic.classify(dat[i]);
			if(i < 56 && prediction>=.5){
				correct+=1.0;
			}
			else if ( i > 56 && prediction < .5){
				correct += 1.0;
			}
		}
		System.out.println("logistic prediction rate: " + correct/104.0);*/
	}
}
