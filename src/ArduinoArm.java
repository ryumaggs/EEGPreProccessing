import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;


public class ArduinoArm implements SerialPortEventListener {
	SerialPort serialPort;
	/*input stream to the port*/
	BufferedReader input;
	/* The output stream to the port */
	OutputStream output;
	/* Milliseconds to block while waiting for port open */
	static final int TIME_OUT = 2000;
	/* Default bits per second for COM port. */
	static final int DATA_RATE = 9600;
	/* flag to track if arduino response was received or not*/
	int recieved;

	/*
	 * Function that:
	 * -selects and sets up correct serial port
	 * -establishes input and output streams
	 * -adds serial port listeners
	 */
	public void initialize() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		recieved = 0;
		//Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			portId = (CommPortIdentifier)portEnum.nextElement();
			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL && portId.getName().equals("COM3"))
				System.out.println("Selected port: COM3");
				break;
		}
		
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),TIME_OUT);
			System.out.println("initializing");
			Thread.sleep(4000);
			System.out.println("done!");
			
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/*
	 * This should be called when you stop using the port.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/*
	 * Handle an event on the serial port.
	 * Triggers 'received' flag is Arduino receives a command
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				if (recieved == 0){
					String inputLine=input.readLine();
				if (inputLine.equals("close hand") || inputLine.equals("open hand"))
					recieved = 1;
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}
	
	/*
	 * Function that will write open and close command to the Arduino.
	 * Parameters: com - (0,1) will write open or close, respectively.
	 */
	public void writeMessage(int com){
		char close = 'v';
		char open = 'o';
		if (recieved == 1){
			System.out.println("arduino got the command");
			recieved = 0;
		}
		try{
			if (com == 1){
				output.write(close);
				output.flush();
			}
			if (com == 0){
				output.write(open);
				output.flush();
			}
		}catch(IOException e){e.printStackTrace();}
	}
	
	/*
	 * main function for testing purposes only
	 */
	public static void main(String[] args) throws Exception {
		//ArduinoArm main = new ArduinoArm();
		//main.initialize();
		/*Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
		System.out.println("Started");
		main.writeMessage();
		System.out.println("finished writing message");
		main.output.flush();
		main.close();*/
		//main.writeMessage(1);
	}
}