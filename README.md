
# Overview

This is the first in a series of projects to demonstrate building out an Internet of Things (IOT) application with Scala and Akka. The project we will eventually build out is an application that can monitor devices that detect moisture levels in the earth in order to control valves to water the garden when the soil becomes dry. This application will demonstrate bi-directional interactions with small devices. Small devices typically communicate via the [MQTT](http://mqtt.org/) protocol. Its low overhead and small client libraries make it a good fit for IOT applications. This initial stage of the application server will recieve and process messages from an MQTT broker. At this stage, a script is provided to send mock device data to the application server. 

# Requirements

* Install Java 8

	See this [link](https://java.com/en/download/) for information on stalling java.

	Test your installation by executing the following from your command line:

	```
	java -version
	```

* Install Scala

	For Windows or Linux see this [guide](http://www.scala-lang.org/download/install.html).
	
	For OSX, see this [guide](http://sourabhbajaj.com/mac-setup/Scala/README.html).	This install requires that you install [HomeBrew](http://brew.sh/) first.

	Test your installation by executing the following from your command line:

	```
	scala -version
	```

* Install SBT

	See this [link](http://www.scala-sbt.org/) for the download to install SBT
	
	Test your installation by executing the following from your command line:

	```
	sbt -version
	```
	
* Install the Mosquitto MQTT broker	

	See this [link](http://mosquitto.org/download/) for the download and instructions on installing the MQTT broker,

	For OSX, you can install it using homebrew:
	
	```
	brew install mosquitto

	```
	Make sure that the mosquitto binaries are in your path then execute the following command to start up the broker:
	
	```
	mosquitto
	```
	If you had instsalled it using HomeBrew then it should be located in /usr/local/sbin/mosquitto.
	
* Install Node.js

	This is used to run the script to generate mock device data.

	See this [link](https://docs.npmjs.com/getting-started/installing-node) for directions on installing Node.js.
	

## Running the application

* Start the Broker
	
	```
	mosquitto
	```
* Test the Broker

	In a terminal, execute the mosquitto subscribe command to subscribe to the mock device queue.

	```
	 mosquitto_sub -t "devices/mock/"
	```

	In another terminal, go to the util directory under the directory you checked the project out to from github and execute the following command to install the script:
	
	```
	npm install
	```
	
	Next, execute the script to start generating mock device data.
	
	```
	node device.js
	```
	
	At this point you should be seeing the mock device data appear in the terminal in which you executed the subscribe command. If this is working, you can continue. 
	
* Running the application

  Navigate to the "server" subdirectory underneith the directory you checked out from github and execute the following:
  
  ```
  sbt run
  ```	

	Logging has been configured to debug level in the logback.xml file so you should now see log messages that contain messages similar to the following:
	
	```
	Received device data 31638
	```

	If you see those messages, the application is running and receiving the mock data from the MQTT broker. If you don't see those messages, check for errors in the startup of the application and make sure that your MQTT broker is running. 


# Notes:

This project contains an unmanaged library under the lib directory that was built off of a fork to the [paho-akka](https://github.com/giabao/paho-akka) library which resolves a reconnect issue. A [pull request](https://github.com/giabao/paho-akka/pull/6) has been created to the original project but has not yet been merged so I've added it as an unmanaged dependency to keep this project simple. If you wish to build this forked project for yourself, you can access the modified source [here](https://github.com/aiacovella/paho-akka).