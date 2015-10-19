#include <Serial.h>

const int LED_pin = 13;
boolean LED_state = false;

void setup() 
{ 
  Serial.begin(9600);
  pinMode(LED_pin, OUTPUT);
  digitalWrite(LED_pin, LED_state);
} 
 
void loop() 
{ 
  if (Serial.available() > 0) {
    Serial.read();
    LED_state ^= 1;
    digitalWrite(LED_pin, LED_state);
  }
  delay(200);
} 
