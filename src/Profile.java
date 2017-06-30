import java.io.*;
import java.util.Arrays;

public class Profile {
	String name;
	double[] false_positives;
	
	public Profile(String name){
		this.name = name;
		try{
			File folder = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder");
			File[] listOfFiles = folder.listFiles();
			false_positives = new double[8];
			int counter = 0;
			double correct = 0;
			double total = 0;
			String file_name;
			
			for(File file : listOfFiles){
				file_name = file.getName();
				System.out.println(file_name);
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_predict \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\" \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\EyesAlpha\\"+file_name+".model\"");
				builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String input;
				while(true){
					input = r.readLine();
					if(input!=null){
						//System.out.println(input);
						if(check_header(input)==1){
							//System.out.println("hit the header");
							correct = Double.parseDouble(r.readLine());
							total = Double.parseDouble(r.readLine());
							System.out.println("correct: " + correct + " and total: " + total);
							false_positives[counter] = 1-(correct/total);
							counter++;
							break;
						}
					}
				}
			}
		}catch(IOException e){e.printStackTrace();}
	}
	
	public int check_header(String in){
		for (int i =0; i < 3; i++){
			if (in.charAt(i) != 'a')
				return -1;
		}
		return 1;
	}
	
	public int decide(int[] predictions){
		double alpha = 1.0;
		for (int i = 0; i < predictions.length;i++){
			if(predictions[i] == 1)
				alpha = alpha * false_positives[i];
		}
		if (alpha < .05)
			return 1;
		return 0;
	}
	
	public static void main(String args[]){
		Profile bob = new Profile("bob");
		System.out.println(Arrays.toString(bob.false_positives));
	}
}
