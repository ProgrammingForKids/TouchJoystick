const unsigned long MAX_DISTANCE = 35;
const unsigned long MAX_DURATION = MAX_DISTANCE * 1000 * 1000 * 2 / 100 / 340;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"



//Ultrasonic sensor
int pinEcho = 10; // yellow
int pinTrig = 11; // green
// blue - vcc

int pinLed = 13;


#define USE_SERIAL_MONITOR

void Delay(int ms)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Delay ");
  Serial.print(ms);
  Serial.println(" ms");
#endif
  delay(ms);
}


/* 
 
 Bluetooth
  - Pin 12 ---> TX // purple
  - Pin 2  ---> RX // orange
// ground - blk
// vcc - white, gray - ground
*/
SoftwareSerial BT(12, 2);

/*
  Connections:
  Motor driver
  - Pin 3 ---> PWMA
  - Pin 4 ---> AIN2
  - Pin 5 ---> AIN1
  - Pin 6 ---> STBY
  - Pin 7 ---> BIN1
  - Pin 8 ---> BIN2
  - Pin 9 ---> PWMB
  */
TB6612FNG wheels(3,4,5,6,7,8,9);


void setup()
{
  wheels.begin();

#ifdef USE_SERIAL_MONITOR
  Serial.begin(9600);
#endif

  //configure pin modes
  pinMode(pinLed, OUTPUT);
  digitalWrite(pinLed, LOW);


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
  duration = pulseIn(pinEcho, HIGH, MAX_DURATION * 10);

  // Serial.println(duration);

  if (duration == 0)
    return false;
  if (duration > MAX_DURATION )
    return false;
  return true;
}

int speed = 0;

bool obstacle = false;
char  recent_state = 's'; // Stopped

void loop()
{
  bool isClose = isTooClose();

  if ( (!obstacle) && isClose )
  {
    digitalWrite(pinLed, HIGH);
    BT.println("Obstacle! Emergency stop");
    obstacle = true;

    wheels.Brake();
  }


  if ( (!isClose) && obstacle)
  {
    digitalWrite(pinLed, LOW);
    BT.println("Obstacle removed");
    obstacle = false;
  }

  int BluetoothData = 's';

  if (BT.available())
  {
    BluetoothData = BT.read();
    Serial.print("Received ");
    Serial.println((char)BluetoothData);
  }

  char report = '\0';

  if (recent_state != BluetoothData)
  {
    wheels.Stop();
    report = 'S';
    delay(100);
  }

  recent_state = BluetoothData;

  int aftermath = 30;

  switch (BluetoothData)
  {
    case 'f':
      if (obstacle)
      {
        BT.println("Cant go forward, obstacle");
        wheels.Brake();
        recent_state = 's';
      }
      else
      {
        wheels.Forward();
        recent_state = 'f';
        report = 'F';
        aftermath = 100;
      }
      break;

    case 'b':
      wheels.Back();
      recent_state = 'b';
      report = 'B';
      aftermath = 100;
      break;

    case 'l':
      wheels.Left();
      recent_state = 'l';
      report = 'L';
      aftermath = 100;
      break;

    case 'r':
      wheels.Right();
      recent_state = 'r';
      report = 'R';
      aftermath = 100;
      break;

    case 's':
      break;

    default:
      Serial.println("Unknown command");
      report = 'X';
      break;
  }

  if (report != '\0')
  {
    BT.print(report);
  }

  delay(aftermath);// prepare for next data ...
}

