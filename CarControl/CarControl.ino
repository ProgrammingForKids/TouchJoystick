const unsigned long MAX_DISTANCE = 35;
const unsigned long MAX_DURATION = MAX_DISTANCE*1000*1000*2/100/340;

#include <SoftwareSerial.h>// import the serial library

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

int pinLed=13;


#define USE_SERIAL_MONITOR


void motorDrive(boolean motorNumber, boolean motorDirection, int motorSpeed)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Drive motor ");
  Serial.print((int)motorNumber + 1);
  Serial.print(' ');
  if (motorDirection == turnCCW)
    Serial.print("counter-");
  Serial.print("clockwise with speed ");
  Serial.println(motorSpeed);
#endif

  /*
  This Drives a specified motor, in a specific direction, at a specified speed:
    - motorNumber: motor1 or motor2 ---> Motor 1 or Motor 2
    - motorDirection: turnCW or turnCCW ---> clockwise or counter-clockwise
    - motorSpeed: 0 to 255 ---> 0 = stop / 255 = fast
  */

  boolean pinIn1;  //Relates to AIN1 or BIN1 (depending on the motor number specified)

  //Specify the Direction to turn the motor
  //Clockwise: AIN1/BIN1 = HIGH and AIN2/BIN2 = LOW
  //Counter-Clockwise: AIN1/BIN1 = LOW and AIN2/BIN2 = HIGH
  if (motorDirection == turnCW)
    pinIn1 = HIGH;
  else
    pinIn1 = LOW;

//Select the motor to turn, and set the direction and the speed
  if(motorNumber == motor1)
  {
    digitalWrite(pinAIN1, pinIn1);
    digitalWrite(pinAIN2, !pinIn1);  //This is the opposite of the AIN1
    analogWrite(pinPWMA, motorSpeed);
  }
  else
  {
    digitalWrite(pinBIN1, pinIn1);
    digitalWrite(pinBIN2, !pinIn1);  //This is the opposite of the BIN1
    analogWrite(pinPWMB, motorSpeed);
  }
   
 
//Finally , make sure STBY is disabled - pull it HIGH
  digitalWrite(pinSTBY, HIGH);

}

void motorBrake(boolean motorNumber)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Break motor ");
  Serial.println((int)motorNumber + 1);
#endif
/*
This "Short Brake"s the specified motor, by setting speed to zero
*/

  if (motorNumber == motor1)
    analogWrite(pinPWMA, 0);
  else
    analogWrite(pinPWMB, 0);
   
}


void motorStop(boolean motorNumber)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Stop motor ");
  Serial.println((int)motorNumber + 1);
#endif
  /*
  This stops the specified motor by setting both IN pins to LOW
  */
  if (motorNumber == motor1) {
    digitalWrite(pinAIN1, LOW);
    digitalWrite(pinAIN2, LOW);
  }
  else
  {
    digitalWrite(pinBIN1, LOW);
    digitalWrite(pinBIN2, LOW);
  } 
}


void motorsStandby()
{
#ifdef USE_SERIAL_MONITOR
  Serial.println("STANDBY");
#endif
  /*
  This puts the motors into Standby Mode
  */
  digitalWrite(pinSTBY, LOW);
}

void Delay(int ms)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Delay ");
  Serial.print(ms);
  Serial.println(" ms");
#endif
  delay(ms);  
}



int HIGH_LIMIT=255;
int LOW_LIMIT=130;
int STEP=15;

SoftwareSerial BT(pinTx, pinRx);

void setup()
{
//Set the PIN Modes
  pinMode(pinPWMA, OUTPUT);
  pinMode(pinAIN1, OUTPUT);
  pinMode(pinAIN2, OUTPUT);

  pinMode(pinPWMB, OUTPUT);
  pinMode(pinBIN1, OUTPUT);
  pinMode(pinBIN2, OUTPUT);

  pinMode(pinSTBY, OUTPUT);

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
  duration = pulseIn(pinEcho, HIGH, MAX_DURATION*10);

 // Serial.println(duration);

  if (duration == 0)
    return false;
  if (duration > MAX_DURATION )
    return false;
  return true;
}

int speed = 0;

bool obstacle = false;
char  recent_state='s'; // Stopped

void loop()
{
   bool isClose = isTooClose();
   
   if ( (!obstacle) && isClose )
   {
      digitalWrite(pinLed, HIGH);
      BT.println("Obstacle! Emergency stop");
      obstacle = true;

      motorBrake(motor1);
      motorBrake(motor2);
      motorsStandby();
   }


   if ( (!isClose) && obstacle)
   {
      digitalWrite(pinLed, LOW);
      BT.println("Obstacle removed");
      obstacle = false;    
   }
   
   int BluetoothData='s';
   
   if (BT.available())
   {
      BluetoothData=BT.read();
      Serial.print("Received ");
      Serial.println((char)BluetoothData);
   }
   
    char report='\0';
    
    if (recent_state != BluetoothData)
    {
      motorStop(motor1);
      motorStop(motor2);
      motorsStandby();
        report='S';
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
          motorBrake(motor1);
          motorBrake(motor2);
       motorsStandby();
         recent_state='s';
        }
        else
        {
          motorDrive(motor1, turnCW, 255);
          motorDrive(motor2, turnCW, 255);
          recent_state='f';
          report = 'F';
          aftermath = 100;
        }
        break;

      case 'b':
          motorDrive(motor1, turnCCW, 255);
          motorDrive(motor2, turnCCW, 255);
        recent_state='b';
        report = 'B';
        aftermath = 100;
        break;

      case 'l':
          motorDrive(motor1, turnCCW, 255);
          motorDrive(motor2, turnCW, 255);
        recent_state='l';
        report = 'L';
        aftermath = 100;
        break;

      case 'r':
          motorDrive(motor1, turnCW, 255);
          motorDrive(motor2, turnCCW, 255);
        recent_state='r';
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


/*
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
*/

