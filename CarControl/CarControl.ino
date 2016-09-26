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
  - Pin 13 ---> optional signal led
*/
Outlook outlook(10, 11, MAX_DISTANCE, 13);

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


TimeConstrain wheelsConstrain("Wheels");
TimeConstrain actionConstrain("Action");
TimeConstrain outlookConstrain("Outlook");

bool bIdle;
char ongoingOp;
char reply;
bool bOutlookRequired;

void setup()
{
  wheels.begin();

  Log::begin();

  outlook.begin();
  BT.begin(38400);

  Log("Ready");
  bIdle = true;
  ongoingOp = '\0';
  reply = '\0';
  
  wheelsConstrain.set(0);
  actionConstrain.set(0);
  outlookConstrain.set(0);
  bOutlookRequired = false;
}

static const unsigned long RunningTime = 500;
static const unsigned long RotationTime = 120;
static const unsigned long WheelsSpeedStepTime = 50;
static const unsigned long pauseBetweenDirectionChanges = 300;

bool ProbeOutlook()
{
  if (bOutlookRequired && outlookConstrain.check())
  {
    if ( outlook.isInRange() )
    { 
      wheels.Brake();
      bIdle = true;
      bOutlookRequired = false;
      while (BT.available())
      {
        BT.read();
      }
      BT.print('O');
      Log("Obstacle");
      reply = '\0';
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

struct ForwardTraits
{
  static Wheels::eCompletion WheelsMotion(Wheels& w) { return w.Forward(); }
  static constexpr unsigned long ActionTime() { return RunningTime; }
  static constexpr bool OutlookRequired() { return true; }
  static constexpr const char* Name()  { return "Forward"; }
};

struct BackTraits
{
  static Wheels::eCompletion WheelsMotion(Wheels& w) { return w.Back(); }
  static constexpr unsigned long ActionTime() { return RunningTime; }
  static constexpr bool OutlookRequired() { return false; }
  static constexpr const char* Name()  { return "Back"; }
};

struct TurnTraits
{
  static constexpr unsigned long ActionTime() { return RotationTime; }
  static constexpr bool OutlookRequired() { return false; }  
};

struct LeftTraits : public TurnTraits
{
  static Wheels::eCompletion WheelsMotion(Wheels& w) { return w.Left(); }
  static constexpr const char* Name()  { return "Left"; }
};

struct RightTraits : public TurnTraits
{
  static Wheels::eCompletion WheelsMotion(Wheels& w) { return w.Right(); }
  static constexpr const char* Name()  { return "Right"; }
};


template <typename Traits>
struct MoveOp
{
  void operator()(bool bNewCommand)
  {
    if (bNewCommand)
    {
      wheelsConstrain.set(0);
      actionConstrain.set(Traits::ActionTime());
    }
    
    if (bNewCommand || wheelsConstrain.check())
    {
      const auto wheelsState = Traits::WheelsMotion(wheels);
      if (wheelsState == Wheels::STARTING)
      {
        if (bIdle)
        {
          // Going from Idle to Starting triggers action timer
          actionConstrain.set(Traits::ActionTime());
        }
      }

        // New action timer is set when the state goes from STOPPED to STARTING,
        // hence during the STOPPING phase the actionConstrain is still expired
        // after the previous operation. We have to prevent another read from BT
        // until the STARTING begins.
      if ((wheelsState == Wheels::STOPPING)
          ||
          ( ( ! bIdle ) && (wheelsState == Wheels::STOPPED) )
          )
      {
        actionConstrain.set(max(Traits::ActionTime(), WheelsSpeedStepTime));
      }
      
      bIdle = false;
            
      if (wheelsState == Wheels::DONE)
      {
        ongoingOp = '\0';
        Log("Accomplished ")(Traits::Name());
      }
      else if (wheelsState == Wheels::STOPPED)
      {
        wheelsConstrain.set(pauseBetweenDirectionChanges);
        actionConstrain.set(max(Traits::ActionTime(),pauseBetweenDirectionChanges));
        bIdle = true;
      }
      else
      {
        wheelsConstrain.set(WheelsSpeedStepTime);
      }
    }
  }
};

unsigned long last_bt_timestamp=0;

void CalcWheelsSpeed(int speed, int sector, int& left_factor, int& right_factor)
{
  if (sector <= 4)
  {
    left_factor = 2 * speed;
    right_factor = (2 - sector)*speed;
  }
  else if (sector <= 8)
  {
    left_factor = -2 * speed;
    right_factor = (6 - sector)*speed;
  }
  else if (sector <= 11)
  {
    left_factor = (sector - 10)*speed;
    right_factor = -2 * speed;
  }
  else if (sector <= 15)
  {
    left_factor = (sector - 14)*speed;
    right_factor = 2 * speed;
  }

  else
  {
    Log("BAD SECTOR ")(sector);
  }
}

void loop()
{
  if (BT.available())
  {
    last_bt_timestamp = millis();
    byte op = BT.read();
    command c = *reinterpret_cast<command*>(&op);
    Log("Read speed=")(c.speed)(" sector=")(c.sector);

    int left_factor;
    int right_factor;
    CalcWheelsSpeed(c.speed, c.sector, left_factor, right_factor);
  }
}


