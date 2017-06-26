//import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;
//import java.util.Scanner;
//import java.util.ArrayList;
import java.text.DecimalFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class DataStreaming implements SerialPortEventListener{
	private BufferedInputStream input;
	private OutputStream output;
	SerialPort serialPort;
	String portName = "COM3";
	private String Filename;
	private BufferedWriter filewriter;
	private Lock lock;
	
	private static final int TIME_OUT = 5000;
	private static final int baudrate = 115200;
	private static final double eegscale = .02235; //4.5/24/(Math.pow(2,23)-1);
	private static final double accelscale = .002/16;
	private static DecimalFormat format2 = new DecimalFormat(".##");
	
	private int linecounter;
	private byte[] backbuffer;
	private boolean usebackbuffer;
	
	public void setupconnection(String filename){
		lock = Experiment.get_lock();
		linecounter = 0;
		
		Filename = filename;
		try{
			filewriter = new BufferedWriter(new FileWriter(Filename));
		}
		catch (IOException e){
			e.printStackTrace();
		}
		usebackbuffer = false;
		
		String portName = "COM4";
		
	    CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}
		
		
		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedInputStream(serialPort.getInputStream(),65536);
			//input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
			// add event listeners
			serialPort.addEventListener((SerialPortEventListener) this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	//Listens to serialPort for incoming signals and parse them into readable format
	public synchronized void serialEvent(SerialPortEvent oEvent) {
			try {
				while(input.available() >= 33){
				byte[] inputStrand = new byte[33];
				int c;
				if(usebackbuffer){
					c = backbuffer[0];
				}
				else{
					c = input.read();
				}
				//Read the first 4 lines of signal (OPENBCI header)
				if(isalpha(c) && linecounter <4){
					linecounter ++;
					String inputLine= ((char) c) + linereader();
					write2file(inputLine, false);
				}
				//Read the 5th line of signal (OPENBCI header "$$$")
				else if(c == '$' && linecounter == 4){
					linecounter ++;
					input.read();
					input.read();
				}
				else{
					if(c == (byte) 0xA0){
						inputStrand[0] = (byte) c;
						input.read(inputStrand, 1, 32);
						usebackbuffer = false;
						backbuffer = null;
					}
					else{
						if(usebackbuffer){
							int bblen = backbuffer.length;
							System.arraycopy(backbuffer, 0, inputStrand, 0, bblen);
							inputStrand[bblen] = (byte) c;
							if(bblen < 32){
								input.read(inputStrand, bblen+1, 32-bblen);
							}
						}
						else{
							inputStrand[0] = (byte) c;
							input.read(inputStrand, 1, 32);
						}
					}
					if(handleconsistency(inputStrand)){
						
						double[] data = sampleparser(inputStrand);
						String sdata = data2String(data) + '\n';
						lock.check_lock();
						write2file(sdata, false);
						}
					else{
						System.out.println("lost packet");
						write2file("##################LOST PACKET###################", true);
					}
					}
				}
				} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	
	/*
	 * Check to see that the packet of data (33 bytes) is of consistent format (a header of 0xA0 and a footer between 0xC0-0xC6)
	 * Returns: 0 if data is consistent; 1 if missing a header; 2 if missing a footer
	 */
	private boolean handleconsistency(byte[] data){
		int lastidx = data.length -1;
		int headerint = -1;
		//if the header and footer exist in the correct position, return true
		if(data[0] == (byte) 0xA0 && data[lastidx] >= (byte) 0xC0 && data[lastidx] <= (byte) 0xC6){
			usebackbuffer = false;
			backbuffer = null;
			return true;
		}
		//if the first element is not a header, then find the index of the next header and append the remainder of the array to the backbuffer
		else{
//			if(data[0] != (byte) 0xA0){
				for(int i= 1; i<data.length; i++){
					if(data[i] == (byte) 0xA0){
						headerint = i;
						//break;
					}
				}
//			}
//			else{
//				for(int i= 1; i<data.length; i++){
//					if(data[i] == (byte) 0xA0){
//						headerint = i;
//						break;
//					}
//					if(data[i] >= (byte) 0xC0 && data[i] <= (byte) 0xC6){
//						headerint = i+1;
//						break;
//					}
//				}
//			}
		}
		if(headerint != -1 && headerint < data.length){
			usebackbuffer = true;
			int buffsize = data.length - headerint;
			backbuffer = new byte[buffsize];
			System.arraycopy(data, headerint, backbuffer, 0, buffsize);
		}
		else{
			backbuffer = null;
			usebackbuffer = false;
		}
		return false;
	}
	
	
	private boolean isalpha(int c){
		if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
			return true;
		}
		return false;
	}
	
	//ReadLine function for BufferedOutputStream
	private String linereader(){
		String line = "";
		try{
			int curchar = input.read();
			line += (char) curchar;
			while(curchar != '\n'){
				curchar = input.read();
				line += (char) curchar;
			}
		}
		catch (Exception e){
			System.err.println(e.toString());
		}
		return line;
	}

	//Parse a line of data into the follow components:
	//samplenumber, eeg data, acceleration data
	private double[] sampleparser(byte[] sample){
		double[] data = new double[12];
		//data[0] = Character.getNumericValue(sample[1]);
		long l = sample[1] & 0xFFL;
		data[0] = (int) l;
		data[1] = interpret24bitAsInt32(bytesubarray(sample,2,4)) * eegscale;
		data[2] = interpret24bitAsInt32(bytesubarray(sample,5,7)) * eegscale;
		data[3] = interpret24bitAsInt32(bytesubarray(sample,8,10)) * eegscale;
		data[4] = interpret24bitAsInt32(bytesubarray(sample,11,13)) * eegscale;
		data[5] = interpret24bitAsInt32(bytesubarray(sample,14,16)) * eegscale;
		data[6] = interpret24bitAsInt32(bytesubarray(sample,17,19)) * eegscale;
		data[7] = interpret24bitAsInt32(bytesubarray(sample,20,22)) * eegscale;
		data[8] = interpret24bitAsInt32(bytesubarray(sample,23,25)) * eegscale;
//		for(int eeg=0; eeg < 8; eeg++){
//			int basecount = 2 + (3*eeg);
//			System.out.print(basecount);
//			data[eeg+1] = interpret24bitAsInt32(bytesubarray(sample,basecount, basecount+2)) * eegscale;
//		}
		for(int accel= 0; accel < 3; accel++){
			int basecount = 26 + (2*accel);
			byte[] unexpandedint = new byte[2];
			unexpandedint[0] = sample[basecount];
			unexpandedint[1] = sample[basecount +1];
			data[accel + 9] = interpret16bitAsInt32(unexpandedint) * accelscale;
		}
		return data;
	}
	
	//Retrieve a subarray of a byte array
	private byte[] bytesubarray(byte[] arr, int start, int end){
		byte[] subarr = new byte[end-start+1];
		for(int i=0; i<subarr.length; i++){
			subarr[i] = arr[i+start];
		}
		return subarr;
	}
	
	//Format a byte array to printable data for visualization
	private String data2String(double[] data){
		String dString = "";
		for (int i=0; i < data.length; i++){
			dString = dString + format2.format(data[i]) + " ";
		}
		return dString;
	}
	
	//'nline' refers to if you NEED a newline character (aka don't have one in the string)
	public void write2file(String data, Boolean nline){
		try{
			//System.out.println("uncloekd from write2file");
			filewriter.write(data);
			if(nline == true){
				filewriter.newLine();
			}
			filewriter.flush();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void endstream(){
		try{ 
			output.write('s'); 
			output.flush();
			serialPort.close();
		}
		catch (Exception e) {
			System.err.println(e.toString());
			}
	}
	
	//OPENBCI function to convert 24bit eeg data to 32bit int
	int interpret24bitAsInt32(byte[] byteArray) {     
	    int newInt = (  
	     ((0xFF & byteArray[0]) << 16) |  
	     ((0xFF & byteArray[1]) << 8) |   
	     (0xFF & byteArray[2])  
	    );  
	    if ((newInt & 0x00800000) > 0) {  
	      newInt |= 0xFF000000;  
	    } else {  
	      newInt &= 0x00FFFFFF;  
	    }  
	    return newInt;  
	}  
	
	//OPENBCI fucntion to convert 16bit acceleration data to 32bit int
	int interpret16bitAsInt32(byte[] byteArray) {
	    int newInt = (
	      ((0xFF & byteArray[0]) << 8) |
	       (0xFF & byteArray[1])
	      );
	    if ((newInt & 0x00008000) > 0) {
	          newInt |= 0xFFFF0000;
	    } else {
	          newInt &= 0x0000FFFF;
	    }
	    return newInt;
	  }
	
	public OutputStream getWriter(){
		return output;
	}
	
	public static void main(String[] args){
//		for testing purposes only
		
		
//		DataStreaming teststream = new DataStreaming();
//		teststream.setupconnection("Newtester.txt");
//		Thread t=new Thread() {
//			public void run() {
//			//the following line will keep this app alive for 1000 seconds,
//			//waiting for events to occur and responding to them (printing incoming messages to console).
//			try {
//				Thread.sleep(10000);
//				//teststream.endstream();
//				System.out.println("Finished");
//			} 
//			catch (InterruptedException ie) {}
//			}
//		};
//		t.start();
//		System.out.println("Started1");
	}
}

