import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;

import java.awt.Color;
import java.io.*;
import java.util.Arrays;

public class VisualizeSignals extends ApplicationFrame{
	private XYPlot plot;
	private int datasetIndex = 0;

		public VisualizeSignals(String app_title, String chart_title){
			super(app_title);
			JFreeChart lineChart = ChartFactory.createLineChart(
					chart_title,
					"frequency", "volts",
					createDataset("src/FilteredDataFolder"),
					PlotOrientation.VERTICAL,
					true,true,false);
			
			this.plot = lineChart.getXYPlot();
			this.plot.setBackgroundPaint(Color.lightGray);
			this.plot.setDomainGridlinePaint(Color.white);
			this.plot.setRangeGridlinePaint(Color.white);
			lineChart.setBackgroundPaint(Color.white);
			ChartPanel chartPanel = new ChartPanel( lineChart);
			chartPanel.setPreferredSize( new java.awt.Dimension(560,367));
			setContentPane( chartPanel );
		}
		
		private XYSeries createDataset(String dirpath){
			File dir = new File(dirpath);
			File[] listofFiles = dir.listFiles();
			double[] dat = new double[18];
			XYSeries dataSet = new XYSeries("boop");
			for (File file: listofFiles){
				dat = loadValues(file);
				for(int index = 0; index < dat.length; index++){
					if(dat[index] != 0.0){
						dataSet.add((double)index, dat[index]);
					}
				}
			}
			return dataSet;
		}
		
		private double[] loadValues(File file){
			double[] data = new double[19];
			String input = "";
			try{
				BufferedReader br = new BufferedReader(new FileReader(file));
				input = br.readLine();
				String[] curData = input.split(" ");
				for(int index = 1; index < curData.length; index++){
					System.out.println(curData[index]);
					data[index] = Double.parseDouble(curData[index].substring(3, curData[index].length()));
				}
				br.close();
			}catch(IOException e){e.printStackTrace();}
			return data;
		}
		
		public static void main(String[] args){
			VisualizeSignals test = new VisualizeSignals(
					"temp title 1",
					"temp title 2");
			
			test.pack();
			RefineryUtilities.centerFrameOnScreen(test);;
			test.setVisible(true);
		}
}
