const unsigned long MAX_DISTANCE = 35;

#include <SoftwareSerial.h>// import the serial library

#include "TB6612FNG.h"
#include "Outlook.h"
#include "TimeConstrain.h"
#include "Speeds.h"
#include "Log.h"

#include "Command.h"

/*
 * Ultrasonic sensor
  - Pin 16 (A2) ---> echo // yellow
  - Pin 15 (A1) ---> trig // green
// blue - vcc
  - Pin LED_BUILTIN ---> optional signal led
*/
Outlook outlook_head("Head", 16, 15, MAX_DISTANCE, LED_BUILTIN);

/*
 * Ultrasonic sensor 2
  - Pin 11  ---> echo // white
  - Pin 10  ---> trig // brown
// red - vcc, black - gnd
  - Pin LED_BUILTIN ---> optional signal led
*/
Outlook outlook_tail("Tail", 11, 10, MAX_DISTANCE, LED_BUILTIN);

/*  Bluetooth
  - Pin 14(A0) ---> TX // purple
  - Pin 17(A3)  ---> RX // orange
  // ground - blk
  // vcc - white, gray - ground
*/
SoftwareSerial BT(14, 17);

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

void setup()
{
  wheels.begin();

  Log::begin();

  outlook_head.begin();
  outlook_tail.begin();
  BT.begin(38400);
  
  Log("Ready");
  
  idleConstrain.set(0);
  outlookConstrain.set(0);
}

Speeds lastSpeeds;

#include "Response.h"

void loop()
{
  Response resp;
  
  if (BT.available())
  {    
    idleConstrain.set(1000);
    
    byte op = BT.read();
    
    Command & c = Command::parse(op);
    Speeds curSpeeds = c.MotorsSpeed();

    if ( curSpeeds.VectorChanged(lastSpeeds) )
    {
      outlookConstrain.set(0);
    }

    lastSpeeds = curSpeeds;
    resp = {lastSpeeds};
  }

  int32_t dominant_direction = lastSpeeds._l + lastSpeeds._r;
  if (dominant_direction != 0)  // neither idle nor pivoting on the center
  {
    if (outlookConstrain.check())
    {
      if (dominant_direction > 0) // going forward
      {
        if (outlook_head.isInRange())
        {
          resp = Response::HeadObstacle{};
          ///...
        }
      }
      else // if (dominant_direction < 0) // going reverse
      {
        if (outlook_tail.isInRange())
        {
          resp = Response::TailObstacle{};
          ///...
        }
      }
      outlookConstrain.set(50);
    }
  }

  if (resp.isObstacle())
  {
    wheels.Brake();
    idleConstrain.never();
    lastSpeeds = {0,0};
  }
  
  if (lastSpeeds.isZero())
  {
    if (idleConstrain.check())
    {
      Log("Idle on timeout");

      lastSpeeds = {0,0};
      outlookConstrain.never();
      resp = {lastSpeeds};
    }
  }

  if (resp.isSet())
  {
    byte b = resp;
    BT.write(b);
    Log("Sent 0x")(b, HEX);
    resp.ToLog();
    if (! resp.isObstacle() )
    {
      wheels.Go(lastSpeeds._l, lastSpeeds._r);
    }
  }

 
}


