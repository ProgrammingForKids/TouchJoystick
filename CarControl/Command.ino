#include "Command.h"

CommandPolar::CommandPolar(byte b)
: Command(Command::eProtocol::protoPolar)
, _sector((b&B00111000)>>3)
, _speed(b&B00000111)
{
}

CommandTank::Track::Track(byte b)
: _reverse(b&B100)
, _speed(b&B011)
{
}

CommandTank::CommandTank(byte b)
: Command(Command::eProtocol::protoTank)
, _left((b&B00111000)>>3)
, _right(b&B00000111)
{
}

