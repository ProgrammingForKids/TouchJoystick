#ifndef _ProgrammingForKids_TouchJoystick__Command_h__
#define _ProgrammingForKids_TouchJoystick__Command_h__

#pragma pack(push, 1)
struct command
{
  //76543210
  //  rectilinear part is 5 bits
  int fwd_rev_sign : 1; // 0 for forward, 1 for reverse msb(7 bit)
  int direction : 1; // 0 for right, 1 for left 6 bit
  
  int linear_speed : 4;   // 5,4,3,2 bit
  //  rotational part is 3 bits
  int angular_speed : 2; //1,0 bit
};
#pragma pack(pop)

#endif // _ProgrammingForKids_TouchJoystick__Command_h__
