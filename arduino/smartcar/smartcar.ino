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
  handleInput();
  preventCrash();
}

void handleInput() {
  if (Serial.available()) {
    String input = Serial.readStringUntil('\n');
    if (input.startsWith("m")) {
      int throttle = input.substring(1).toInt();
      car.setSpeed(throttle);
    }
    else if (input.startsWith("t")) {
      int deg = input.substring(1).toInt();
      car.setAngle(deg);
    }
    else if (input.startsWith("r")) {
      control.overrideMotorSpeed(100, -100);
    }
  }
}

void preventCrash() {
  unsigned int fDistance = front.getDistance();
  if (fDistance > 0) {
    car.setSpeed(0);
    delay(500);
    control.overrideMotorSpeed(100, -100);
    delay(1250);
    car.setSpeed(0);
  }
}
