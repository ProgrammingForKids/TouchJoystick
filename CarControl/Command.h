#ifndef _ProgrammingForKids_TouchJoystick__Command_h__
#define _ProgrammingForKids_TouchJoystick__Command_h__

struct Command
{
  enum eProtocol { 
    protoPolar = B00,
    protoTank = B01
  };
  
  const eProtocol _protocol : 2;

protected:
  Command(eProtocol prot)
  : _protocol(prot)
  {
  }
};

struct CommandPolar
{
  const uint8_t _sector : 3;// 0 - means forward, counting clockwise
  const uint8_t _speed : 3;//

  CommandPolar(byte b)
  : Command(eProtocol::protoPolar)
  , _sector((b&B00111000)>>3)
  , _speed(b&B00000111)
  {
  }
};

struct CommandTank
{
  struct Track
  {
    const bool _reverse : 1;
    const uint8_t _speed : 2;
    Track(byte b)
    : _reverse(b&B100)
    , _speed(b&B011)
    {}
  };
  
  const Track _left;
  const Track _right;

  CommandTank(byte b)
  : Command(eProtocol::protoTank)
  , _left((b&B00111000)>>3)
  , _right(b&B00000111)
  {
  }
};
#endif // _ProgrammingForKids_TouchJoystick__Command_h__
