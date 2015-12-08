#include <Servo.h>                          

const int LED_pin = 13;

Servo right_servo;
Servo left_servo;

String receive_buffer = "";

// set a speed in radians per second, with positive being forward
void setServoSpeed(float radsPerSec)
{
  // constrain the values
  radsPerSec = max(radsPerSec, -2.8);
  radsPerSec = min(radsPerSec, 2.8);
  // 15 corresponds to 2.8 rad/s
  float servoValue = 15*radsPerSec/2.8;
  int stoppedValue = 91;
  right_servo.write(stoppedValue - (int)servoValue);
  left_servo.write(stoppedValue + (int)servoValue);
}

void setup() 
{ 
  Serial.begin(9600);
  pinMode(LED_pin, OUTPUT);
  digitalWrite(LED_pin, LOW);
  right_servo.attach(5);
  left_servo.attach(3);
} 
 
void loop() 
{ 
  if (Serial.available() > 0) {
    int received_byte = Serial.read();
    if (received_byte == 'a') {
      receive_buffer = "";
    }
    else if (received_byte == 'z') {
      // parse the buffer
      float parsed_buffer = receive_buffer.toFloat();
      setServoSpeed(parsed_buffer);
    }
    else if (received_byte == 'c') {
      // clear the buffer (for debugging)
      receive_buffer = "";
      setServoSpeed(0);
    }
    else { // any other character is added to the buffer
      receive_buffer += (char)received_byte;
    }
  }
} 
