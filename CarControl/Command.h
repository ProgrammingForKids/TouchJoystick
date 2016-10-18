#ifndef _ProgrammingForKids_TouchJoystick__Command_h__
#define _ProgrammingForKids_TouchJoystick__Command_h__

#include "Speeds.h"

struct Command
{
  enum eProtocol { 
    protoPolar = B00,
    protoTank = B01
  };
  
  const eProtocol _protocol : 2;

  static Command& parse(byte b);

  virtual Speeds MotorsSpeed() = 0;

protected:
  Command(eProtocol prot)
  : _protocol(prot)
  {
  }
};

struct CommandPolar : public Command
{
  const uint8_t _sector : 3;// 0 - means forward, counting clockwise
  const uint8_t _speed : 3;//

  CommandPolar(byte b);

  Speeds MotorsSpeed() override;
};

struct CommandTank : public Command
{
  struct Track
  {
    const bool _reverse : 1;
    const uint8_t _speed : 2;
    Track(byte b);

    short MotorSpeed() const;
  };
  
  const Track _left;
  const Track _right;

  CommandTank(byte b);
  
  Speeds MotorsSpeed() override;
};

#endif // _ProgrammingForKids_TouchJoystick__Command_h__
