######################################
OpenBCI Model Training and Prediction
######################################
by. Jack Lin and Ryan Yu
**************************************

######################################
Experimental Files Breakdown:
**************************************
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

######################################
PreProcessing Files Breakdown:
**************************************

######################################
Auxiliary Files:
**************************************
Complex.java:
	Complex number class type that is used when passing in
	data to be trained by SVM.
Lock.java:
	Lock/Unlock mechanism to provide for concurrency and
	synchronization of datastreaming and experimental
	design (trial timing and insertion of trial "markers"
	in streaming data.
	
######################################
Files not in use:
**************************************
ArduinoArm.java
Channel.java
Trail.java
DescisionTree.java
train_and_predict.java
######################################