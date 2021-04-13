#include <Smartcar.h>

// value taken from POSIX according to TA Ryan
#define PI 3.14159265358979323846264338327950288

ArduinoRuntime arduinoRuntime;

const int FRONT_US_PIN_6 = 6;
const int FRONT_US_PIN_7 = 7;
SR04 frontUS(arduinoRuntime, FRONT_US_PIN_6, FRONT_US_PIN_7, 200);

const int FRONT_IR_PIN_0 = 0;
const int BACK_IR_PIN_3 = 3;
GP2D120 frontIR(arduinoRuntime, FRONT_IR_PIN_0);
GP2D120 backIR(arduinoRuntime, BACK_IR_PIN_3);

BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);

DifferentialControl control(leftMotor, rightMotor);

SimpleCar car(control);

unsigned int minDistance = 0;
unsigned int obstacleAvoidDistance = 100;

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(280);
}

void loop() {
  car.setSpeed(100);
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

void preventCrash(){
  preventCrashWithUSandIR(frontUS);
  preventCrashWithUSandIR(frontIR);
  preventCrashWithUSandIR(backIR);
}

// General method to detect obstacles
void preventCrashWithUSandIR(DistanceSensor& distanceSensor) {
  unsigned int distance = distanceSensor.getDistance();
  if (distance > minDistance && distance < obstacleAvoidDistance) {
      Serial.print(distanceSensor.getDistance());
      Serial.println(" Obstacle detected. ");
      car.setSpeed(0);
      delay(500);
      car.overrideMotorSpeed(50, -100);
      delay (800);
      car.setSpeed(0);
      delay (200);
    }
}
