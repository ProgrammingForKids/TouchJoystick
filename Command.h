#ifndef _ProgrammingForKids_TouchJoystick__Command_h__
#define _ProgrammingForKids_TouchJoystick__Command_h__

#pragma pack(push, 1)
struct command
{
  //  rectilinear part is 5 bits
  int fwd_rev_sign : 1; // 0 for forward, 1 for reverse
  int linear_speed : 4;
  //  rotational part is 3 bits
  int direction : 1; // 0 for right, 1 for left
  int angular_speed : 2;
};
#pragma pack(pop)

#endif // _ProgrammingForKids_TouchJoystick__Command_h__
