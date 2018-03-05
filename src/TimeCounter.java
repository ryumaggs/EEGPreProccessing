import java.io.*;
public class TimeCounter {
	public static boolean check_same(String a, String b){
		for(int i = 0; i < a.length(); i++){
			if(a.charAt(i) != b.charAt(i))
				return false;
		}
		return true;
	}
	public static void main(String args[]){
		try{
			double seconds = 0;
			File file = new File("C:\\Users\\Ryan Yu\\workspace\\ImportantFreq\\src\\RawDataFolder\\CLOSED.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = "";
			while((s = br.readLine()) != null){
				//System.out.println(s);
				if (check_same("255.0 6",s)){
					seconds++;
				}
			}
			br.close();
			System.out.println("number of seconds: " + seconds);
		}catch(Exception e){e.printStackTrace();}
	}
}
