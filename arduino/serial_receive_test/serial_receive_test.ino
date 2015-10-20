#include <Servo.h>                          

const int LED_pin = 13;

Servo right_servo;
Servo left_servo;

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
      digitalWrite(LED_pin, HIGH);
      right_servo.write(90-15);
      left_servo.write(90+15);
    }
    else if (received_byte == 'b') {
      digitalWrite(LED_pin, LOW);
      right_servo.write(90+15);
      left_servo.write(90-15);
    }
    else if (received_byte == 'c') {
      digitalWrite(LED_pin, LOW);
      right_servo.write(90);
      left_servo.write(90);
    }
  }
} 
