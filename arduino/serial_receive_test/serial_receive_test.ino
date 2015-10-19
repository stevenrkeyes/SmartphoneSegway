const int LED_pin = 13;

void setup() 
{ 
  Serial.begin(9600);
  pinMode(LED_pin, OUTPUT);
  digitalWrite(LED_pin, LOW);
} 
 
void loop() 
{ 
  if (Serial.available() > 0) {
    int received_byte = Serial.read();
    if (received_byte == 'a') {
      digitalWrite(LED_pin, HIGH);
    }
    else if (received_byte == 'b') {
      digitalWrite(LED_pin, LOW);
    }
  }
} 
