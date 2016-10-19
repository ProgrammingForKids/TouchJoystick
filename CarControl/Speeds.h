#ifndef __ProgrammableCar__CarControl__Speeds_H__
#define __ProgrammableCar__CarControl__Speeds_H__

// Missing std::pair make the pair of our own
struct Speeds
{
  short _l;
  short _r;

  static const short Max = 255;
  static const short Min = -Max;

  static short Trim(short val)
  {
    return min(max(val,Min),Max);
  }
  
  Speeds(short l, short r)
  : _l(Trim(l)), _r(Trim(r))
  {
  }

  Speeds() : Speeds(0,0) {}

  static int8_t Sign(short val)
  {
    if (val == 0)
      return 0;
    else if (val > 0)
      return 1;
    else
      return -1;
  }
  
  bool VectorChanged(Speeds last) const
  {
    if (Sign(last._l) != Sign(this->_l))
      return true;
    if (Sign(last._r) != Sign(this->_r))
      return true;
  
    // l/r==ll/lr  ==> l*lr==ll*r
    // Checking l*lr==ll*r instead l/r==ll/lr can help avoiding division by zero.
    // This condition alone is necessary but not satisfactory because two of the members may be zero or of the opposite sign
    //  for example if last=(0,1) and the this=(0,-1) or (0,0) the condition will hold though the vector is different
    //  or if last=(1,-1) and this is (-1,1) the condition will hold again.
    // However such cases will be filtered out by the above Sign test
    int32_t left_mul_lastright = last._r * this->_l;
    int32_t lastleft_mul_right = last._l * this->_r;
    return (left_mul_lastright != lastleft_mul_right);
  }

  bool isZero() const
  {
    return (_l|_r)==0 ;
  }
};
#endif // ! __ProgrammableCar__CarControl__Speeds_H__

