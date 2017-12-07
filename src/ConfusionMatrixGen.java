import java.util.*;
import java.io.*;
import java.nio.file.Files;

public class ConfusionMatrixGen {
	public static String participant_number = "3";
	public static String trial_type = "b2";
	public static String[] PARTICIPANT_NUM;
	public static String[] CONDITIONS;
	public static void main(String args[]){
		PARTICIPANT_NUM = new String[9];
		PARTICIPANT_NUM[0] = "1";
		PARTICIPANT_NUM[1] = "3";
		PARTICIPANT_NUM[2] = "4";
		PARTICIPANT_NUM[3] = "5";
		PARTICIPANT_NUM[4] = "6";
		PARTICIPANT_NUM[5] = "7";
		PARTICIPANT_NUM[6] = "9";
		PARTICIPANT_NUM[7] = "10";
		PARTICIPANT_NUM[8] = "11";
		System.out.println(PARTICIPANT_NUM[0]);
		CONDITIONS = new String[2];
		CONDITIONS[0] = "b1";
		CONDITIONS[1] = "b2";
		String curDir = System.getProperty("user.dir")+"\\src\\SeniorThesis Data";
		System.out.println(curDir);
		for(int part =0; part < PARTICIPANT_NUM.length; part++){
			for(int cond = 0; cond < CONDITIONS.length; cond++){
				//RawFilter test = new RawFilter(curDir+"\\"+PARTICIPANT_NUM[part]+"\\"+CONDITIONS[cond],curDir+"\\"+PARTICIPANT_NUM[part]+"\\"+CONDITIONS[cond], 33,0);
				//prep_files();
				//genResults();
				//clear_all();
			}
		}
	}
	
	public static void clear_all(){
		for(int part = 0; part < PARTICIPANT_NUM.length; part++){
			for(int cond = 0; cond < CONDITIONS.length; cond++){
				String curDir = System.getProperty("user.dir")+"\\src\\SeniorThesis Data";
				File folder = new File (curDir+"\\"+PARTICIPANT_NUM[part]+"\\"+CONDITIONS[cond]);
				File[] listOfFiles = folder.listFiles();
				for(int i = 0; i < listOfFiles.length; i++){
					System.out.println("main file is: " + listOfFiles[i].getName());
					File secFolder = listOfFiles[i];
					File[] s_list = secFolder.listFiles();
					for(int j = 0; j < s_list.length; j++){
						if (!(s_list[j].getName().substring(0,1).equals("b"))){
							s_list[j].delete();
						}
						else{
							System.out.println("        file spared: " + s_list[j].getName());
						}
					}
				}
			}
		}
	}
	public static String replaceSelected(String type, String filepath) {
		String inputStr = "";
		String line = "";
	    try {
	        // input the file content to the StringBuffer "input"
	        BufferedReader file = new BufferedReader(new FileReader(filepath));

	        while ((line = file.readLine()) != null) {
		        StringBuffer inputBuffer = new StringBuffer();
		        inputBuffer.append(line);
	            inputBuffer.append('\n');
	            //inputBuffer = inputBuffer.replace(0, 3, type+" ");
	            if (Integer.parseInt(type) == 0) {
	            	inputBuffer = inputBuffer.replace(0, 2, "-1");
	            }
	            else if (Integer.parseInt(type) == 1) {
	            	inputBuffer = inputBuffer.replace(0, 3, "1 ");
	            }

	            inputStr = inputStr + inputBuffer.toString();
	        }
	        file.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return inputStr;
	}
	
	public static void prep_files(){
		String curDir = System.getProperty("user.dir")+"\\src\\SeniorThesis Data";
		for(int part = 0; part < PARTICIPANT_NUM.length; part++){
			for(int cond = 0; cond < CONDITIONS.length; cond++){
				File folder = new File (curDir+"\\SeniorThesis Data\\"+PARTICIPANT_NUM[part]+"\\"+CONDITIONS[cond]);
				File[] listOfFiles = folder.listFiles();
				File[] secondlist = folder.listFiles();
				for(int i = 0; i < listOfFiles.length; i++){
					System.out.println("main file is: " + listOfFiles[i].getName());
					File secFolder = listOfFiles[i];
					File[] s_list = secFolder.listFiles();
					String[] positive_samp = new String[32];
					for(int q = 0; q < s_list.length; q++){
						if(s_list[q].getName().substring(0,1).equals("C")){
							int ind = parseChan(s_list[q].getName());
							positive_samp[ind] = replaceSelected("1",s_list[q].getAbsolutePath());
						}
					}
					for(int j = 0; j < secondlist.length; j++){
						int channel_track = 0;
						System.out.println("       comparing to: " + secondlist[j].getName());
						String[] neg_samp = new String[32];
						File f_Folder = listOfFiles[j];
						File[] f_list = f_Folder.listFiles();
						for(int r = 0; r < f_list.length; r++){
							if(f_list[r].getName().substring(0,1).equals("C")){
								int ind = parseChan(f_list[r].getName());
								neg_samp[ind] = replaceSelected("0",f_list[r].getAbsolutePath());
							}
						}
						try{
					        for(int help = 0; help < positive_samp.length; help++){
								File newFile = new File(listOfFiles[i].getAbsolutePath()+"/"+channel_track+"_"+listOfFiles[i].getName()+"vs"+secondlist[j].getName()+".txt");
						        if(!newFile.exists())
						        	newFile.createNewFile();
						        	FileOutputStream fileOut = new FileOutputStream(newFile);
					        		fileOut.write(positive_samp[help].getBytes());
					        		fileOut.write(neg_samp[help].getBytes());
					        		channel_track+=1;
					        		fileOut.close();
					        	}
					        }catch(IOException e){e.printStackTrace();}
						}
					}
			}
		}
	}
		
	
	public static void genResults(){
		String curDir = System.getProperty("user.dir")+"\\src\\SeniorThesis Data";
		for(int cond = 0; cond < CONDITIONS.length; cond++){
			for(int part =0; part < PARTICIPANT_NUM.length; part++){
				File folder = new File (curDir+"\\"+PARTICIPANT_NUM[part]+"\\"+CONDITIONS[cond]);
				File[] listOfFiles = folder.listFiles();
				for(int i = 0; i < listOfFiles.length; i++){
					System.out.println("file name: " + listOfFiles[i].getName());
					svm_train.run_Directory(listOfFiles[i].getAbsolutePath(), "-v 2 -t 0 -b 1", folder.getAbsolutePath());
				}
			}
		}
	}
	public static int parseChan(String fileName){
		String to_int = fileName.substring(7,9);
		if(to_int.charAt(1) == '_'){
			to_int = to_int.substring(0, 1);
		}
		return Integer.parseInt(to_int);
	}
	
	

}
