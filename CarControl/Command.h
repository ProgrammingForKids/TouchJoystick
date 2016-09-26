#ifndef _ProgrammingForKids_TouchJoystick__Command_h__
#define _ProgrammingForKids_TouchJoystick__Command_h__

struct Command
{
  const unsigned short _sector : 4;// 0 - means forward, counting clockwise
  const unsigned short _speed : 4;//

  Command(byte b)
  : _sector(b&0x0f)
  , _speed((b>>4)&0x0f)
  {
  }
};

#endif // _ProgrammingForKids_TouchJoystick__Command_h__
