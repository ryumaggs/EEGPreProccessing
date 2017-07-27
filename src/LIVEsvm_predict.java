import libsvm.*;
import java.io.*;
import java.util.*;

public class LIVEsvm_predict {
	public static svm_model[] loadModels(String model_dir_path){
		File dir = new File(model_dir_path);
		File[] listOfFiles = dir.listFiles();
		svm_model[] ret = new svm_model[8];
		int channel_counter = 0;
		for(File file:listOfFiles){
			try{
				svm_model model = svm.svm_load_model(file.getAbsolutePath());
				if (model == null)
				{
					System.err.print("can't open model file "+file.getAbsolutePath()+"\n");
					System.exit(1);
				}
				System.out.println(file.getName() + " is at index: " + channel_counter);
				ret[channel_counter] = model;
				channel_counter++;
			}catch(IOException e){e.printStackTrace();}
		}
		return ret;
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	private static double[] predict(String[] curData, svm_model[] model, int predict_probability) throws IOException
	{
		double[] predictionz = new double[8];
		int svm_type = 0;
		int nr_class = 0;
		int[] labels;
		
		for(int channel = 0; channel < model.length; channel++){
			svm_type=svm.svm_get_svm_type(model[channel]);
			nr_class=svm.svm_get_nr_class(model[channel]);
			labels=new int[nr_class];
			svm.svm_get_labels(model[channel],labels);
		
			String line = curData[channel];
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			int m = st.countTokens()/2;
			
			st.nextToken();
			svm_node[] x = new svm_node[m];
			
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v;
			v = svm.svm_predict(model[channel],x);
			predictionz[channel] = v;
		}
		
		return predictionz;
	}

	//for testing purposes only
	public static void main(String args[]){
		/*String[] test_arg = new String[2];
		svm_model[] test_models = loadModels("C:/Users/Ryan Yu/workspace/ImportantFreq/src/ModelFolder");
		test_arg[0] = "C:/Users/Ryan Yu/workspace/ImportantFreq/src/FilteredDataFolder/Bob/SingleExecutionTest/";
		test_arg[1] = "C:/Users/Ryan Yu/workspace/ImportantFreq/src/ModelFolder/";
		try{
			File data_dir_path = new File(test_arg[0]);
			File[] list_of_files = data_dir_path.listFiles();
			String[] test_arr_list = new String[8];
			String s = "";
			int arr_index_count = 0;
			for(File file:list_of_files){
				BufferedReader br = new BufferedReader(new FileReader(file));
				s = br.readLine();
				test_arr_list[arr_index_count] = s;
				System.out.println(test_arr_list[arr_index_count]);
				arr_index_count++;
				br.close();
			}
			
			double[] prediction_test = predict(test_arr_list, test_models, 0);
			System.out.println(Arrays.toString(prediction_test));
		}catch(IOException e){e.printStackTrace();}*/
	}
}
