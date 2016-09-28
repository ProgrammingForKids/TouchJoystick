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

void setup()
{
  wheels.begin();

  Log::begin();

  outlook_head.begin();
  BT.begin(38400);
  
  Log("Ready");
  
  idleConstrain.set(0);
  outlookConstrain.set(0);
}

void CalcSpeedFactor( unsigned short sector, short& left, short& right)
{
  const static short lut[12][2] = {
    {2, 2},
    {2, 1},
    {2, 0},
    {2, -2},
    {-2, 0},
    {-2, -1},
    {-2, -2},
    {-1, -2},
    { 0, -2},
    { -2, 2},
    { 0, 2},
    { 1, 2}
  };

  sector %= 12;
  left = lut[sector][0];
  right = lut[sector][1];
}

short last_left_speed_factor = 0;
short last_right_speed_factor = 0;
unsigned short last_speed = 0;

#include "Response.h"

void loop()
{
  Response resp;
  
  if (BT.available())
  {
    short left_speed_factor = 0;
    short right_speed_factor = 0;
    idleConstrain.set(1000);
    byte op = BT.read();
    Command c{op};
    Log("Read speed=")(c._speed)(" sector=")(c._sector);

    if (c._speed > 0)
    {
      CalcSpeedFactor(c._sector, left_speed_factor, right_speed_factor);
    }
    Log("LEFT ")(left_speed_factor)(" RIGHT ")(right_speed_factor);

    if ( // direction changed
        ((last_left_speed_factor != left_speed_factor)
        ||
        (last_right_speed_factor != right_speed_factor))
       )
    {
      outlookConstrain.set(0);
    }
    
    last_left_speed_factor = left_speed_factor;
    last_right_speed_factor = right_speed_factor;
    last_speed = c._speed;
    resp = {last_left_speed_factor, last_right_speed_factor};
  }

  if (last_left_speed_factor + last_right_speed_factor != 0)  // neither idle nor pivoting on the center
  {
    if (outlookConstrain.check())
    {
      if (max(last_left_speed_factor, last_right_speed_factor) == 2) // going forward
      {
        if (outlook_head.isInRange())
        {
          resp = Response::HeadObstacle{};
          ///...
        }
      }

      if (min(last_left_speed_factor, last_right_speed_factor) == -2) // going reverse
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
    last_left_speed_factor = 0;
    last_right_speed_factor = 0;
    last_speed = 0;
  }
  
  if ((last_left_speed_factor | last_right_speed_factor) != 0)
  {
    if (idleConstrain.check())
    {
      Log("Idle on timeout");

      last_left_speed_factor = 0;
      last_right_speed_factor = 0;
      last_speed = 0;
      outlookConstrain.never();
      resp = {0, 0};
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
      wheels.Go(last_speed, 0xf, last_left_speed_factor, last_right_speed_factor);
    }
  }

 
}


