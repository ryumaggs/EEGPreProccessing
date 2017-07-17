import java.io.*;
import java.net.URL;
import java.util.Arrays;

public class Profile {
	String name;
	double[] weights;
	double best_thresh;
	
	public Profile(String name){
		this.name = name;
		weights = new double[8];
		createProfile(name);
		best_thresh = 2.1;
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
	
	//header check for process input returns
	public int check_header(String in, String comp){
		for (int i =0; i < comp.length(); i++){
			if (in.charAt(i) != comp.charAt(i))
				return -1;
		}
		return 1;
	}
	
	//runs svm_train cross validation 10-fold for each channel separately
	//and then assigns their accuracies to the weights array	
	public void createProfile(String foldername){
		String path = "src/FilteredDataFolder/" + foldername;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		double[] bestAccuracy = new double[8];
		int accCount = -1;
		String prevChannel = "";
		String curChannel;
		File[] bestFiles = new File[8];
		
		String file_name;
		try{
			for(File file: listOfFiles){
				if(!file.isDirectory()){
					file_name = file.getName();
					curChannel = file_name.split("_")[0];
					System.out.println("looking at: " + file_name);
					ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "java -classpath libsvm.jar svm_train -v 10 " + path + "\\"+file_name+"\"");
					Process process = builder.start();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while (true){
						line = reader.readLine();
						if(line!=null && check_header(line,"Cross") == 1){
							double curAccuracy = Double.parseDouble(line.split(" ")[4].split("%")[0]);
							if(curChannel.equals(prevChannel)){
								if(curAccuracy > bestAccuracy[accCount]){
									bestAccuracy[accCount] = curAccuracy;
									bestFiles[accCount] = file;
								}
							}
							else{
								prevChannel = curChannel;
								accCount++;
								bestAccuracy[accCount] = curAccuracy;
								bestFiles[accCount] = file;
							}
							break;
						}	
					}
				}
			}
				System.arraycopy(bestAccuracy, 0, weights, 0, bestAccuracy.length);
				createBestFreqDir(path, bestFiles);
		}catch (IOException e){e.printStackTrace();}
	}
	
	private void createBestFreqDir(String des, File[] files){
		File bestFreqDir = new File(des + "/Best Frequency Set");
		bestFreqDir.mkdir();
		File cpyFile;
		InputStream source = null;
		OutputStream output = null;
		try{
			for(File file: files){
				cpyFile = new File(bestFreqDir + "/" + file.getName());
				source = new FileInputStream(file);
				output = new FileOutputStream(cpyFile);
				byte[] buffer = new byte[1024];
				int bytes;
				while((bytes = source.read(buffer))> 0){
					output.write(buffer,0,bytes);
				}
				source.close();
				output.close();
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	//saves the profile to a .profile file
	public static void save_profile(Profile toSave){
		File file = new File("src/Profiles/" + toSave.name + ".profile");
		try{
			PrintWriter writer = new PrintWriter(file,"UTF-8");
			writer.println(toSave.name);
			writer.println(doubleArray_to_string(toSave.weights));
			writer.println(toSave.best_thresh);
			writer.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	//converts a double array to a string to be saved
	public static String doubleArray_to_string(double[] darray){
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < darray.length; i++){
			str.append(darray[i]);
			if (i != darray.length -1)
				str.append(", ");
			else
				str.append("\n");
		}
		return str.toString();
	}
	
	public int makeDecision(int[] modelDecisions){
		double weightedAverage = 0;
		for(int idx=0; idx < modelDecisions.length; idx++){
			weightedAverage += modelDecisions[idx]*weights[idx];
		}
		if(weightedAverage >= best_thresh){
			return 1;
		}
		else{
			return 0;
		}
	}
	
	public static void main(String args[]){
		Profile bob = new Profile("Bob");
		//System.out.println(Arrays.toString(bob.weights));
		save_profile(bob);
		System.out.println("Successful Execution");
	}
}
