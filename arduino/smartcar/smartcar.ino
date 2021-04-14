#include <Smartcar.h>

// value taken from POSIX according to TA Ryan
#define PI 3.14159265358979323846264338327950288

ArduinoRuntime arduinoRuntime;
SR04 front(arduinoRuntime, 6, 7, 200);
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

SimpleCar car(control);

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(280);
}

void loop() {
  car.setSpeed(90);
  preventCrash();
}

// assuming normalized values between -1 and 1
void joystick(float x, float y) {
  // pythagoras for magnitude of the vector
  float magnitude = sqrt(x*x + y*y);
  // dot product of forward vector [0, 1] and [x, y] divided by the magnitudes of the two vectors, then take arccos for the angle and convert from radians to degrees
  double degree = (acos(y / magnitude)) * 180 / PI;

  // figure out if we're going forward or backward, also amplify value from a normalized -1 to 1 input
  if (y >= 0)
    magnitude = magnitude * 100;
  else 
    magnitude = magnitude * -100;

  // check if we're going left
  if (x < 0)
    degree = degree * -1;

  car.setSpeed(magnitude);
  car.setAngle((int)degree);
  delay(10);
}

void preventCrash() {
  unsigned int fDistance = front.getDistance();
  if (fDistance > 0 && fDistance < 200) {
    car.setSpeed(0);
    delay(500);
    control.overrideMotorSpeed(100, -100);
    delay(800);
    car.setSpeed(0);
    delay(200);
  }
}
