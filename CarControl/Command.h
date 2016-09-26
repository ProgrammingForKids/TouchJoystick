#ifndef _ProgrammingForKids_TouchJoystick__Command_h__
#define _ProgrammingForKids_TouchJoystick__Command_h__

#pragma pack(push, 1)
struct command
{
  int speed : 4;// 
  int sector : 4;// 0 - means forward, counting clockwise
};
#pragma pack(pop)

#endif // _ProgrammingForKids_TouchJoystick__Command_h__
