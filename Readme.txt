OpenBCI Model Training and Prediction
-------------------------------------
by. Jack Lin and Ryan Yu
**************************************

Project Overview
-----------------
	-The code in this project is part of our research with Colgate University CS department.
	
	-The ultimate goal of the research is create a relatively inexpensive system which will
	learn and interpret brain patterns to control a robotic arm.
	
	-The robotic arm is 3D printed and designed by InMoov: http://inmoov.fr/hand-and-forarm/
	
	-The device used in this project was purchased from OpenBCI: http://openbci.com/
	
	-Data recorded from the EEG are filtered and processed in our RawFilter.java
	
	-Using the processed data from this device, we trained multiple support vector machine (SVM)
	models using LIBSVM: https://www.csie.ntu.edu.tw/~cjlin/libsvm/
	
	-Taking the predictions from the SVM models, we use an Arduino Board to control the robotic arm
**************************************

Setup/Installation
------------------
LIBSVM:
	-LIBSVM is the SVM used in this project (LIBSVM documentation: https://www.csie.ntu.edu.tw/~r94100/libsvm-2.8/README)
	
	-LIBSVM can be downloaded for free at: https://www.csie.ntu.edu.tw/~cjlin/libsvm/
		under "Download LIBSVM" header
	
	-You will need to add the LIBSVM directory path to your classpath (as the code calls Windows Command Prompt); follow the link 
	  below for instructions if unsure
		Link: http://introcs.cs.princeton.edu/java/15inout/classpath.html

OpenBCI GUI:
	-This GUI is only used to make sure the headset has a good enough connection to the scalp before actual recording
	
	-the OpenBCI GUI can be downloaded at: http://openbci.com/downloads
		under "OpenBCI GUI" tab
**************************************

Getting Started
------------------
1. Recording data:
	a.Set up the OpenBCI Cyton Board:
		-Check out OpenBCI tutorial on setting up the board: http://docs.openbci.com/Tutorials/01-Cyton_Getting%20Started_Guide
		
	b.Open the OpenBCI GUI and click "start live streaming":
		-Make note of which COM port it recognizes
		-Adjust headset (tighten or loosen nodes) untill the 0-15 microvolts are displayed on each channel
	
	c.Close OpenBCI GUI
	
	d.Run filterGUI.java and click "Run Experiment"
		-Change file path, file name, and COM port as needed
		-see Experiment.java breakdown for how to record properly

2. Filtering data:
	a. Run filterGUI.java:
		-set appropriate directories
		-click "Run Mu Filter"

3. Training SVM models:
	-Incomplete-

Rest of "Getting Started" still incomplete
**************************************
Experimental Files Breakdown (INCOMPLETE):
------------------

DataStreaming.java:
	Connects to comPort and initilizes datastreaming from
	OpenBci wireless reciever at 256Hz (256 datasamples/s).
	Includes parsing function that simultaenously translate 
	raw OpenBCI data to readable EEG data as the data is being
	read in.  Data are written to an empty txt file passed in 
	as a parameter when setting up connection to port.
	
Experiment.java:
	Handles experimental section (Experimental design and 
	presentation).  
		Design:
			Image of closed/open fist
		Presentation (one trail):
			Blank (2s delay)
			Prompt image (2s delay)
			Blank (2s delay)
			Experimental image (2s delay)
			Blank (2s delay)
	Call the DataStreaming class for simultaneous streaming
	with experiment.
**************************************

PreProcessing Files Breakdown:
------------------

**************************************

Auxiliary Files:
------------------
Complex.java:
	Complex number class type that is used when passing in
	data to be trained by SVM.
Lock.java:
	Lock/Unlock mechanism to provide for concurrency and
	synchronization of datastreaming and experimental
	design (trial timing and insertion of trial "markers"
	in streaming data.
**************************************

Files not in use:
------------------
ArduinoArm.java
Channel.java
Trail.java
DescisionTree.java
train_and_predict.java
**************************************

Citations:
	Chih-Chung Chang and Chih-Jen Lin, LIBSVM : a library for support vector machines. ACM Transactions on Intelligent Systems and 	
		Technology, 2:27:1--27:27, 2011. Software available at http://www.csie.ntu.edu.tw/~cjlin/libsvm
	Sedgewick, R. and Wayne, K., Algorithims 4th Edition (2011). Boston, MA: Addison-Wesley Professional.
