const unsigned long MAX_DISTANCE = 15;
const unsigned long MAX_DURATION = MAX_DISTANCE*1000*1000*2/100/340;

#include <SoftwareSerial.h>// import the serial library


//Pins 5 and 6: controlled by timer0 (8 bits)
//Pins 9 and 10: controlled by timer1 (16 bits)
//Pins 11 and 3: controlled by timer2 (8 bits)

//Define Pins

//Motor A
int pinA1 = 2;    //Yellow
int pinA2 = 3;    // Green

//Motor B
int pinB1 = 4;    // Blue
int pinB2 = 5;    // Purple->Orange


// Bluetooth
int pinTx = 7; // Yellow
int pinRx = 8; // Green
// int pinEn=9; // Brown

//Ultrasonic sensor
int pinEcho = 11; // White
int pinTrig = 12; // Blue



int pinLed=13;

int HIGH_LIMIT=255;
int LOW_LIMIT=130;
int STEP=15;


void pwm_go(int A, int B)
{
  if (A>0)
  {
      digitalWrite(pinA1, HIGH);  
      digitalWrite(pinA2, LOW);    
  }
  else if (A<0)
  {
      digitalWrite(pinA2, HIGH);  
      digitalWrite(pinA1, LOW);    
  }
  else
  {
      digitalWrite(pinA1, LOW);  
      digitalWrite(pinA2, LOW);    
  }
  
  if (B>0)
  {
      digitalWrite(pinB2, HIGH);  
      digitalWrite(pinB1, LOW);    
  }
  else if (B<0)
  {
      digitalWrite(pinB1, HIGH);  
      digitalWrite(pinB2, LOW);    
  }
  else
  {
      digitalWrite(pinB1, LOW);  
      digitalWrite(pinB2, LOW);    
  }
}

SoftwareSerial BT(pinTx, pinRx);

void setup()
{
  Serial.begin (9600);

  //configure pin modes
  pinMode(pinLed, OUTPUT);
  digitalWrite(pinLed, LOW);
  
  pinMode (pinA1, OUTPUT);
  pinMode (pinA2, OUTPUT);
  pinMode (pinB1, OUTPUT);
  pinMode (pinB2, OUTPUT);

  pinMode(pinTrig, OUTPUT);
  pinMode(pinEcho, INPUT);

  BT.begin(38400);
  BT.println("Bluetooth is Ready");

  Serial.println("Ready");

}



bool isTooClose()
{
  long duration;

  // The sensor is triggered by a HIGH pulse of 10 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  digitalWrite(pinTrig, LOW);
  delayMicroseconds(5);
  digitalWrite(pinTrig, HIGH);
  delayMicroseconds(10);
  digitalWrite(pinTrig, LOW);
 
  // Read the signal from the sensor: a HIGH pulse whose
  // duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  duration = pulseIn(pinEcho, HIGH, MAX_DURATION*10);

  Serial.println(duration);

  if (duration == 0)
    return false;
  if (duration > MAX_DURATION )
    return false;
  return true;
}

int speed = 0;

void test()
{
  Serial.println("Left FWD");
  pwm_go(1,0);
  delay(2000);
  Serial.println("Left STOP");
  pwm_go(0,0);
  delay(500);
  Serial.println("Left REV");
  pwm_go(-1,0);
  delay(2000);
  Serial.println("Left STOP");
  pwm_go(0,0);
  delay(500);
  
  Serial.println("Right FWD");
  pwm_go(0,1);
  delay(2000);
  Serial.println("Right STOP");
  pwm_go(0,0);
  delay(500);
  Serial.println("Right REV");
  pwm_go(0, -1);
  delay(2000);
  Serial.println("Right STOP");
  pwm_go(0,0);
  delay(500);

  Serial.println("Both FWD");
  pwm_go(1,1);
  delay(2000);
  Serial.println("Both STOP");
  pwm_go(0,0);
  delay(500);
  Serial.println("Both REV");
  pwm_go(-1,-1);
  delay(2000);
  Serial.println("Both STOP");
  pwm_go(0,0);
  delay(500);
  
}

bool obstacle = false;

void loop()
{
   bool isClose = isTooClose();
   
   if ( (!obstacle) && isClose )
   {
      digitalWrite(pinLed, HIGH);
      BT.println("Obstacle! Emergency stop");
      obstacle = true;
   }

   if ( (!isClose) && obstacle)
   {
      digitalWrite(pinLed, LOW);
      BT.println("Obstacle removed");
      obstacle = false;    
   }
   
   
   if (BT.available())
   {
      int BluetoothData=BT.read();
      Serial.print("Received");
      Serial.println(BluetoothData, HEX);

      switch (BluetoothData)
      {
        case 'F': case 'f':
          if (obstacle)
          {
            BT.println("Cant go forward, obstacle");
            pwm_go(0,0);
            break;
          }
          pwm_go(0,0);
          delay(100);
          pwm_go(1,1);
          delay(400);
          pwm_go(0,0);          
          break;

        case 'B': case 'b':
          pwm_go(0,0);
          delay(100);
          pwm_go(-1,-1);
          delay(400);
          pwm_go(0,0);          
          break;

        case 'L': case 'l':
          pwm_go(0,0);
          delay(100);
          pwm_go(-1,1);
          delay(400);
          pwm_go(0,0);          
          break;

        case 'R': case 'r':
          pwm_go(0,0);
          delay(100);
          pwm_go(1,-1);
          delay(400);
          pwm_go(0,0);          
          break;

        case 'S': case 's':
          pwm_go(0,0);
          break;

        default:
          Serial.println("Unknown command");
          break;
      }
     }

     if (isTooClose())
     {
      pwm_go(0,0);
      digitalWrite(pinLed, HIGH);
      BT.println("Obstacle! Emergency stop");
     }
      
     delay(100);// prepare for next data ...
}
