// This #include statement was automatically added by the Particle IDE.
#include "MQTT/MQTT.h"

// Configure the incoming signal pin for the moisture sensor.
int sensorPin = A0;

// Configure the periodic power output pin
int powerOut = D0;  

// Configure the Diagnostic LED
int diagnosticLed = D7;

// Unique identifier for the device
String deviceId="/garden/aisle/1/moisture/1";

// Configure the IP address of the MQTT Client 
byte server[] = { 192,168,117,86 };

// Iniialize the MQTT Client for port 1833
MQTT client(server, 1883, NULL);

// This gets executed  before the loop defined below. 
void setup() {

  //Set the pin mode to output
  pinMode(diagnosticLed, OUTPUT);

  // Configure the input pin for sensor input signals
  pinMode(sensorPin, INPUT);

  // Configure the output pin for sending power to the sensor.
  pinMode(powerOut, OUTPUT); 

  // Configure the serial interface for debugging.
  Serial.begin(9600);

  Serial.println("Connecting to MQTT server ");

  // Connect the the spark MQTT service.
  client.connect("sparkclient");

}


// This is the loop that gets repeatedly called after the setup code has completed. 
void loop() {
    
  blinkDiagnostic(1, 100);    
    
  // Turn on the power to the sebnsor by seting the output pin to high. 
  digitalWrite(powerOut, HIGH);    
    
  // Read a value from the signal line
  int sensorValue = analogRead(sensorPin);

  // Send the moisture reading for this device to the particle cloud service
  bool result = Particle.publish("moisture", String(sensorValue));    
  Serial.println("Publish of Sensor value " + String(sensorValue) + " to the Particle Cloud returned " + String(result));


  // Check if the MQTT client is connected  
  if (! client.isConnected()) {
      // Display three diagnostic led pulses to notify that we are unable to connect to the MQTT server.
      blinkDiagnostic(3, 100);

      // Attempt or Re-attempt a connect the the spark MQTT service.
      client.connect("sparkclient");
  }

// Send the moisture reading for this device to the MQTT service
  bool mqttResult = client.publish(deviceId, String(sensorValue));
  Serial.println("Publish of Sensor value " + String(sensorValue) + " to the MQTT server returned " + String(mqttResult));


  // Turn the power on the sensor back off to prevent the sensor from degrading
  // and to conserve battery life. 
  digitalWrite(powerOut, LOW);    

  // Pause for 60 seconds until the next reading
  delay(60000);

}

//The function that handles the event from IFTTT
void blinkDiagnostic(int numberOfBlinks, int duration){
  int cnt = 1;

  while(cnt &lt;= numberOfBlinks) {
    // We&#039;ll turn the LED on
    digitalWrite(diagnosticLed, HIGH);
  
    Serial.println(&quot;=== Turn On&quot;);

    delay(duration);
    
    // Then we&#039;ll turn it off...
    digitalWrite(diagnosticLed, LOW);

    Serial.println(&quot;=== Turn Off&quot;);

    delay(duration);

    cnt += 1;
    
    Serial.println(&quot;=== Count &quot; + String(cnt));
  }
}