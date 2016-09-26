const unsigned long MAX_DISTANCE = 35;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"
#include "Outlook.h"
#include "TimeConstrain.h"
#include "Log.h"

#include "Command.h"

/*
 * Ultrasonic sensor
  - Pin 10 ---> echo // yellow
  - Pin 11 ---> trig // green
// blue - vcc
  - Pin LED_BUILTIN ---> optional signal led
*/
Outlook outlook_head(10, 11, MAX_DISTANCE, LED_BUILTIN);

/*

  Bluetooth
  - Pin 12 ---> TX // purple
  - Pin 2  ---> RX // brown
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


TimeConstrain idleConstrain("Wheels");
TimeConstrain outlookConstrain("Outlook");

bool bIdle;
char ongoingOp;
char reply;
bool bOutlookRequired;

void setup()
{
  wheels.begin();

  Log::begin();

  outlook_head.begin();
  BT.begin(38400);

  Log("Ready");
  bIdle = true;
  ongoingOp = '\0';
  reply = '\0';
  
  idleConstrain.set(0);
  outlookConstrain.set(0);
}

static const unsigned long RunningTime = 500;
static const unsigned long RotationTime = 120;
static const unsigned long WheelsSpeedStepTime = 50;
static const unsigned long pauseBetweenDirectionChanges = 300;


void CalcSpeedFactor( unsigned short sector, short& left, short& right)
{
  if (sector <= 4)
  {
    left = 2;
    right = (2 - sector);
  }
  else if (sector <= 8)
  {
    left = -2;
    right = (6 - sector);
  }
  else if (sector <= 11)
  {
    left = (sector - 10);
    right = -2;
  }
  else if (sector <= 15)
  {
    left = (sector - 14);
    right = 2;
  }

  else
  {
    Log("BAD SECTOR ")(sector);
  }
}

short last_left_speed_factor = 0;
short last_right_speed_factor = 0;
unsigned short last_speed = 0;

#include "Response.h"

void loop()
{
  short left_speed_factor;
  short right_speed_factor;

  Response resp;
  
  if (BT.available())
  {
    idleConstrain.set(1000);
    byte op = BT.read();
    Command c{op};
    Log("Read speed=")(c._speed)(" sector=")(c._sector);

    CalcSpeedFactor(c._sector, left_speed_factor, right_speed_factor);
    Log("LEFT ")(left_speed_factor)(" RIGHT ")(right_speed_factor);

    if ( // direction changed
        ((last_left_speed_factor ^ left_speed_factor)
        |
        (last_right_speed_factor ^ right_speed_factor))
        != 0
        )
    {
      outlookConstrain.set(0);
    }
    
    last_left_speed_factor = left_speed_factor;
    last_right_speed_factor = right_speed_factor;
    last_speed = c._speed;
    resp = {last_left_speed_factor, last_right_speed_factor};
  }

  if (outlookConstrain.check())
  {
    if (left_speed_factor + right_speed_factor != 0)  // neither idle nor pivoting on the center
    {
      if (max(left_speed_factor, right_speed_factor) == 2) // going forward
      {
        if (outlook_head.isInRange())
        {
          resp = Response::HeadObstacle{};
          ///...
        }
      }

      if (min(left_speed_factor, right_speed_factor) == -2) // going reverse
      {
        if (/*outlook_tail.isInRange()*/false)
        {
          resp = Response::TailObstacle{};
          ///...
        }
      }
    }

    outlookConstrain.set(50);
  }

  if (resp.isObstacle())
  {
    wheels.Brake();
    idleConstrain.never();
    last_left_speed_factor = left_speed_factor = 0;
    last_right_speed_factor = right_speed_factor = 0;
    last_speed = 0;
  }
  
  if ((last_left_speed_factor | last_right_speed_factor) != 0)
  {
    if (idleConstrain.check())
    {
      Log("Idle on timeout");
      last_left_speed_factor = left_speed_factor = 0;
      last_right_speed_factor = right_speed_factor = 0;
      last_speed = 0;
      outlookConstrain.never();
      resp = {0, 0};
    }
  }

  if (resp.isSet())
  {
    BT.print(static_cast<byte>(resp));
    resp.ToLog();
    if (! resp.isObstacle() )
    {
      wheels.Go(last_speed, 0xf, left_speed_factor, right_speed_factor);
    }
  }

 
}


