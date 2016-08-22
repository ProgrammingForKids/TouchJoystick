const unsigned long MAX_DISTANCE = 35;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"
#include "Outlook.h"
#include "TimeConstrain.h"
#include "Log.h"

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


bool bStopped;
char ongoingOp;

void setup()
{
  wheels.begin();

  Log::begin();

  //configure pin modes
  pinMode(pinLed, OUTPUT);
  digitalWrite(pinLed, LOW);


  outlook.begin();
  BT.begin(38400);

  Log("Ready");
  bStopped = true;
  ongoingOp = '\0';

  wheelsConstrain.set(0);
  actionConstrain.set(0);
  outlookConstrain.set(0);
}

static const unsigned long RunningTime = 500;
static const unsigned long RotationTime = 150;

bool bOutlookRequired = false;

bool ProbeOutlook()
{
  if (bOutlookRequired && outlookConstrain.check() && ongoingOp == 'f')
  {
    if ( outlook.isInRange() )
    { 
      wheels.Brake();
      bStopped = true;
      bOutlookRequired = false;
      BT.print('O');
      Log("Obstacle");
      ongoingOp = '\0';
      return false;
    }
    else
    {
      outlookConstrain.set(50);    
    }
  }

  return true;
}

void loop()
{
  if ( ! ProbeOutlook() )
  {
    Log("Stopping before obstacle");
    return;
  }

  char reply = '\0';
  bool bSetActionConstrain = false;
  
  if ( ongoingOp == '\0' && actionConstrain.check() )
  {
    if (BT.available())
    {
      bSetActionConstrain = true;
      ongoingOp = BT.read();
      reply = ongoingOp + 'A' - 'a';
      Log("Fetched command ")(ongoingOp);
    }
    else if (! bStopped)
    {
      Log("No command received, stopping");
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
    bOutlookRequired = true;
    if ( ! ProbeOutlook() )
    {
      return;
    }
    if (bSetActionConstrain)
    {
      actionConstrain.set(RunningTime);
    }
    if (wheelsConstrain.check())
    {
      if (wheels.Forward())
      {
        ongoingOp = '\0';
        Log("Accomplished Forward");
      }
      else
      {
        wheelsConstrain.set(10);
      }
    }
    bStopped  = false;
    break;

  case 'b':
    bOutlookRequired = false;
    if (bSetActionConstrain)
    {
      actionConstrain.set(RunningTime);
    }
    if (wheelsConstrain.check())
    {
      if (wheels.Back())
      {
        ongoingOp = '\0';
        Log("Accomplished Back");
      }
      else
      {
        wheelsConstrain.set(10);
      }
    }
    bStopped  = false;
    break;

  case 'l':
    bOutlookRequired = false;
    if (bSetActionConstrain)
    {
      actionConstrain.set(RotationTime);
    }
    if (wheelsConstrain.check())
    {
      if (wheels.Left())
      {
        ongoingOp = '\0';
        Log("Accomplished Left");
      }
      else
      {
        wheelsConstrain.set(2);
      }
    }
    bStopped  = false;
    break;

  case 'r':
    bOutlookRequired = false;
    if (bSetActionConstrain)
    {
      actionConstrain.set(RotationTime);
    }
    if (wheelsConstrain.check())
    {
      if (wheels.Right())
      {
        Log("Accomplished Right");
        ongoingOp = '\0';
      }
      else
      {
        wheelsConstrain.set(2);
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
        bOutlookRequired = false;
        Log("Accomplished Stop");
      }
      else
      {
        wheelsConstrain.set(5);
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

