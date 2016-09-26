#ifndef _ProgrammingForKids_TouchJoystick__Response_h__                                                                                                                  
#define _ProgrammingForKids_TouchJoystick__Response_h__                                                                                                                  
                                                                                                                                                                        
#pragma pack(push, 1)                                                                                                                                                   
struct response                                                                                                                                                          
{
  int obstacle1 : 1; // MSB
  int obstacle2 : 1;
  
  enum eSpeed
  {
    FullRev = 0,
    SlowRev = 1,
    Idle = 2,
    SlowFwd = 3,
    FullFwd = 4
  };
  
  int left : 3; // left motor speed (see enum)                                                                                                                                             
  int right : 3;// right motor speed (see enum)
};                                                                                                                                                                      
#pragma pack(pop)                                                                                                                                                       
                                                                                                                                                                        
#endif // _ProgrammingForKids_TouchJoystick__Response_h__                                                                                                                
