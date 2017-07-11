import java.io.*;
import java.util.Arrays;

public class Profile {
	String name;
	double[] weights;
	double best_thresh;
	
	public Profile(String name){
		this.name = name;
		setWeights();
	}
	
	public Profile(){
		name = "";
		weights = new double[8];
		best_thresh = 2.1;
	}
	
	//loads profile into an object
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
	
	//header check for proccess input returns
	public int check_header(String in, String comp){
		for (int i =0; i < comp.length(); i++){
			if (in.charAt(i) != comp.charAt(i))
				return -1;
		}
		return 1;
	}
	
	//this is not to be used in the combiner, it's a helper function for a different purpose
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
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_train -v 10 \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\"");
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
	}
	
	//runs svm_train cross validation 10-fold and then assigns that accuracy to the weights array
	public void setWeights(){
		try{
			File folder = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder");
			File[] listOfFiles = folder.listFiles();
			weights = new double[8];
			int counter = 0;
			String file_name;
			
			for(File file : listOfFiles){
				file_name = file.getName();
				System.out.println("looking at file: " + file_name);
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "set classpath = \"C:\\Users;C;\\Users\\SVMjava\\libsvm.jar\" && cd \"C:\\Users\\SVMjava\" && java -classpath libsvm.jar svm_train -v 10 \"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\TempHolder\\"+file_name+"\"");
				builder.redirectErrorStream(true);
				Process q = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(q.getInputStream()));
				String input;
				while(true){
					input = r.readLine();
					if(input!=null){
						if(check_header(input,"Cross")==1){
							String[] split = input.split("\\s");
							weights[counter] = Double.parseDouble(split[4].substring(0, 7));
							counter++;
							break;
						}
					}
				}
			}
		}catch(IOException e){e.printStackTrace();}
	}
	
	//saves the profile to a .profile file
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
	
	//converts a double array to a string to be saved
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
		System.out.println(Arrays.toString(bob.weights));
		bob.best_freq_range();
	}
}
