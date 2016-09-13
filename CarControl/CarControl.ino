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

bool bStopped;
char ongoingOp;

void setup()
{
  wheels.begin();

  Log::begin();

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
static const unsigned long WheelsSpeedStepTime = 50;
static const unsigned long pauseBetweenDirectionChanges = 500;

bool bOutlookRequired = false;

bool ProbeOutlook()
{
  if (bOutlookRequired && outlookConstrain.check())
  {
    if ( outlook.isInRange() )
    { 
      wheels.Brake();
      bStopped = true;
      bOutlookRequired = false;
      while (BT.available())
      {
        BT.read();
      }
      BT.print('O');
      Log("Obstacle");
      ongoingOp = '\0';
      return false;
    }
    else
    {
      outlookConstrain.set(20);    
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
  void operator()(bool bSetActionConstrain)
  {
    if (bSetActionConstrain)
    {
      actionConstrain.set(Traits::ActionTime());
    }

    if (wheelsConstrain.check())
    {
      const Wheels::eCompletion completion = Traits::WheelsMotion(wheels);
      if (completion == Wheels::DONE)
      {
        ongoingOp = '\0';
        Log("Accomplished ")(Traits::Name());
      }
      else
      {
        if (completion == Wheels::STOPPED)
        {
         // wheelsConstrain.set(pauseBetweenDirectionChanges);
        }
        else
        {
          wheelsConstrain.set(WheelsSpeedStepTime);
          if (completion == Wheels::STOPPING)
          {
            actionConstrain.set(Traits::ActionTime());
          }
        }
      }
    }
    bStopped  = false;
  }
};

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
      //if (ongoingOp != 'h')
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
    //actionConstrain.set(0);
    ongoingOp = '\0';
    break;

  case 'f':
    bOutlookRequired = true;
    if ( ! ProbeOutlook() )
    {
      return;
    }
    MoveOp<ForwardTraits>()(bSetActionConstrain);
    break;

  case 'b':
    bOutlookRequired = false;
    MoveOp<BackTraits>()(bSetActionConstrain);
    break;

  case 'l':
    bOutlookRequired = false;
    MoveOp<LeftTraits>()(bSetActionConstrain);
    break;

  case 'r':
    bOutlookRequired = false;
    MoveOp<RightTraits>()(bSetActionConstrain);
    break;
    
  case 's':
    if (wheelsConstrain.check())
    {
      if (wheels.Stop() == Wheels::DONE)
      {
        ongoingOp = '\0';
        bStopped  = true;
        bOutlookRequired = false;
        Log("Accomplished Stop");
      }
      else
      {
        wheelsConstrain.set(WheelsSpeedStepTime);
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

