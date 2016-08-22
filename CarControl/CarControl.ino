const unsigned long MAX_DISTANCE = 35;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"
#include "Outlook.h"
#include "TimeConstrain.h"
/*
 * Ultrasonic sensor
  - Pin 10 ---> echo // yellow
  - Pin 11 ---> trig // green
// blue - vcc
*/
Outlook outlook(10, 11, MAX_DISTANCE);

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
TB6612FNG wheels(3, 4, 5, 6, 7, 8, 9);

int pinLed = 13;


TimeConstrain wheelsConstrain;
TimeConstrain actionConstrain;
TimeConstrain outlookConstrain;

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

bool bStopped;
char ongoingOp;

void setup()
{
  wheels.begin();

#ifdef USE_SERIAL_MONITOR
  Serial.begin(9600);
#endif

  //configure pin modes
  pinMode(pinLed, OUTPUT);
  digitalWrite(pinLed, LOW);


  outlook.begin();
  BT.begin(38400);

  Serial.println("Ready");
  bStopped = true;
  ongoingOp = '\0';

  wheelsConstrain.set(0);
  actionConstrain.set(0);
  outlookConstrain.set(0);
}

static const unsigned long MotionTime = 1000;

void loop()
{
  if ( (! bStopped) && outlookConstrain.check() )
  {
    outlookConstrain.set(50);
    if ( outlook.isInRange() )
    { 
      wheels.Brake();
      bStopped = true;
      BT.print('O');
      Serial.println("Obstacle");
      return;
    }
  }

  char reply = '\0';

  if ( ongoingOp == '\0' && actionConstrain.check() )
  {
    actionConstrain.set(MotionTime);
    if (BT.available())
    {
      ongoingOp = BT.read();
      reply = ongoingOp + 'A' - 'a';
    }
    else if (! bStopped)
    {
      ongoingOp = 's';
      reply = 'S';
    }
  }

  switch (ongoingOp)
  {
  case 'h':
    actionConstrain.set(0);
    ongoingOp = '\0';
    break;

  case 'f':
    if (wheelsConstrain.check())
    {
      if (wheels.Forward())
      {
        ongoingOp = '\0';
      }
      else
      {
        wheelsConstrain.set(20);
      }
    }
    bStopped  = false;
    break;

  case 'b':
    if (wheelsConstrain.check())
    {
      if (wheels.Back())
      {
        ongoingOp = '\0';
      }
      else
      {
        wheelsConstrain.set(20);
      }
    }
    bStopped  = false;
    break;

  case 'l':
    if (wheelsConstrain.check())
    {
      if (wheels.Left())
      {
        ongoingOp = '\0';
      }
      else
      {
        wheelsConstrain.set(20);
      }
    }
    bStopped  = false;
    break;

  case 'r':
    if (wheelsConstrain.check())
    {
      if (wheels.Right())
      {
        ongoingOp = '\0';
      }
      else
      {
        wheelsConstrain.set(20);
      }
    }
    bStopped  = false;
    break;
    
  case 's':
    if (wheelsConstrain.check())
    {
      if (wheels.Stop())
      {
        ongoingOp = '\0';
        bStopped  = true;
      }
      else
      {
        wheelsConstrain.set(20);
      }
    }
    break;

  case '\0':
    break;

  default:
    reply = 'X';
    ongoingOp = '\0';
    actionConstrain.set(0);
    break;
  }  

  if (reply != '\0')
  {
    BT.print(reply);
  }

}

