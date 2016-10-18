#include "Command.h"

CommandPolar::CommandPolar(byte b)
: Command(Command::eProtocol::protoPolar)
, _sector((b&B00111000)>>3)
, _speed(b&B00000111)
{
}


Speeds CommandPolar::MotorsSpeed()
{
  if (_speed == 0)
    return {0,0}; // no matter what's the sector


  struct WheelProps
  {
    const int8_t sign : 1; // 0 for positive, 1 for negative
    const int8_t shift : 1; // 1 for slow, 0 for fast

    short Sign() const { return (short)1 - sign - sign; }
  };

  static const WheelProps FastFwd {0, 0};
  static const WheelProps SlowFwd {0, 1};
  static const WheelProps FastRev {1, 0};
  static const WheelProps SlowRev {1, 1};
  
  const static WheelProps sector_factor[8][2] = {
    {FastFwd, FastFwd},
    {FastFwd, SlowFwd},
    {SlowFwd, SlowRev},
    {FastRev, SlowRev},
    {FastRev, FastRev},
    {SlowRev, FastRev},
    {SlowRev, SlowFwd},
    {SlowFwd, FastFwd}
  };

  // the supplied speed is in range 1..7 , since speed==0 is already handled above.
  // The scale of speeds 0..7 takes in account 
  // sector factor adds one more step, meaning speed[Slow] == speed[Fast-1]
  const static short speed_lut[8] = { 128, 146, 164, 182, 200, 218, 236, Speeds::Max };
  
  const WheelProps & left = sector_factor[_sector][0];
  short left_speed = speed_lut[_speed - left.shift];
  left_speed *= left.Sign();
  
  const WheelProps & right = sector_factor[_sector][1];
  short right_speed = speed_lut[_speed - right.shift];
  right_speed *= right.Sign();

  return { left_speed , right_speed };
}

CommandTank::Track::Track(byte b)
: _reverse(b&B100)
, _speed(b&B011)
{
}

short 
CommandTank::Track::MotorSpeed() const
{
  //  map 00, 01, 10, 11 to speeds
  static const short speeds[4] = { 0, 128, 192, Speeds::Max };
  short retval = speeds[_speed];
  if (_reverse)
  {
    retval = -retval;
  }
  return retval;
}


CommandTank::CommandTank(byte b)
: Command(Command::eProtocol::protoTank)
, _left((b&B00111000)>>3)
, _right(b&B00000111)
{
}

Speeds CommandTank::MotorsSpeed()
{
  return { _left.MotorSpeed(), _right.MotorSpeed() };
}


Command& Command::parse(byte b)
{
  static CommandTank tank{eProtocol::protoTank<<6};
  static CommandPolar polar{eProtocol::protoPolar<<6};

  eProtocol proto = eProtocol(b>>6);
  if (proto == eProtocol::protoTank)
  {
    tank = CommandTank{b};
    return tank;
  }
  else //if (proto == eProtocol::protoPolar)
  {
    polar = CommandPolar{b};
    return polar;
  }
}


