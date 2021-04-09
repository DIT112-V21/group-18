#include <Smartcar.h>

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
