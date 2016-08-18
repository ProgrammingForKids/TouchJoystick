const unsigned long MAX_DISTANCE = 35;
const unsigned long MAX_DURATION = MAX_DISTANCE * 1000 * 1000 * 2 / 100 / 340;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"

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

  - Motor 1: A01 and A02
  - Motor 2: B01 and B02

*/

//Define the Pins

//Motor 1
int pinPWMA = 3; //Speed
int pinAIN2 = 4; //Direction
int pinAIN1 = 5; //Direction

//Standby
int pinSTBY = 6;

//Motor 2
int pinBIN1 = 7; //Direction
int pinBIN2 = 8; //Direction
int pinPWMB = 9; //Speed

//Constants to help remember the parameters
static boolean turnCW = 0;  //for motorDrive function
static boolean turnCCW = 1; //for motorDrive function
static boolean motor1 = 0;  //for motorDrive, motorStop, motorBrake functions
static boolean motor2 = 1;  //for motorDrive, motorStop, motorBrake functions

// Bluetooth
int pinTx = 12; // purple
int pinRx = 2; // orange
// ground - blk
// vcc - white, gray - ground

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



int HIGH_LIMIT = 255;
int LOW_LIMIT = 130;
int STEP = 15;

SoftwareSerial BT(pinTx, pinRx);

TB6612FNG wheels;


void setup()
{
  //Set the PIN Modes
  /*

    pinMode(pinPWMA, OUTPUT);
    pinMode(pinAIN1, OUTPUT);
    pinMode(pinAIN2, OUTPUT);

    pinMode(pinPWMB, OUTPUT);
    pinMode(pinBIN1, OUTPUT);
    pinMode(pinBIN2, OUTPUT);

    pinMode(pinSTBY, OUTPUT);
  */
  wheels.begin(pinPWMA, pinAIN2, pinAIN1, pinSTBY, pinBIN1, pinBIN2, pinPWMB);

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

