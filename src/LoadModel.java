import java.io.*;
import java.util.*;

public class LoadModel {
	private String[] memText;
	
	public LoadModel(String file_name){
		ArrayList<String> memFile = new ArrayList<String>();
		
		try{
			File s= new File(file_name);
			System.out.println("got here boy");
			BufferedReader input = new BufferedReader(new FileReader(file_name));
			while (true){
				String line = input.readLine();
				if(line == null) break;
				
				memFile.add(line);
			}
			input.close();
		} catch(IOException e){System.out.println("eorororor");}
		
		memText = new String[memFile.size()];
		memText = memFile.toArray(memText);
	}
	
	public String[] getText(){
		return memText;
	}
	
	public static void main(String[] args){
		String path = "C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\svm_testDataModel.txt";
		String path2 = "\""+path+"\"";
		System.out.println(path2);
		LoadModel bob = new LoadModel("\"C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\svm_testData.txt.model\"");
		for(String s : bob.getText())
		    System.out.println(s);
	}
}
