#include <vector>

#include <MQTT.h>
#include <WiFi.h>
#include <algorithm>
#ifdef __SMCE__
#include <OV767X.h>
#endif

#include <Smartcar.h>

// value taken from POSIX according to TA Ryan
#define PI 3.14159265358979323846264338327950288

#ifndef __SMCE__
WiFiClient net;
#endif
MQTTClient mqtt;

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

SimpleCar car(control);

const auto oneSecond = 1000UL;
const auto triggerPin = 6;
const auto echoPin = 7;
const auto maxDistance = 400;
SR04 front(arduinoRuntime, triggerPin, echoPin, maxDistance);

std::vector<char> frameBuffer;

void setup() {
  Serial.begin(9600);
#ifdef __SMCE__
  Camera.begin(QVGA, RGB888, 15);
  frameBuffer.resize(Camera.width() * Camera.height() * Camera.bytesPerPixel());
  mqtt.begin("trenavix.com", 1883, WiFi); //NA based broker, use a EU broker like aerostun.dev if in Europe for faster connection!
  // mqtt.begin(WiFi); // Will connect to localhost
#else
  mqtt.begin(net);
#endif
  if (mqtt.connect("arduino", "public", "public")) {
    mqtt.subscribe("smartcar/#", 1);
    mqtt.onMessage([](String topic, String message) {
      if (topic == "smartcar/analog/") {
        int commaIdx = message.indexOf(",");
        float x = message.substring(0, commaIdx - 1).toFloat();
        float y =  message.substring(commaIdx+1).toFloat();
        joystick(x,y);
        Serial.println(topic + " " + message);
      } 
      else {
        Serial.println(topic + " " + message);
      }
    });
  }
}

void loop() {
  if (mqtt.connected()) {
    mqtt.loop();
    const auto currentTime = millis();
#ifdef __SMCE__
    static auto previousFrame = 0UL;
    if (currentTime - previousFrame >= 65) {
      previousFrame = currentTime;
      Camera.readFrame(frameBuffer.data());
      //mqtt.publish("smartcar/camera", frameBuffer.data(), frameBuffer.size(), //Publish camera data
      //             false, 0);
    }
#endif
    static auto previousTransmission = 0UL;
    if (currentTime - previousTransmission >= oneSecond) {
      previousTransmission = currentTime;
      const auto distance = String(front.getDistance());
      mqtt.publish("smartcar/ultrasound/front", distance); //publish US data
    }
  }
#ifdef __SMCE__
  // Avoid over-using the CPU if we are running in the emulator
  delay(35);
#endif
}

/*ArduinoRuntime arduinoRuntime;
SR04 front(arduinoRuntime, 6, 7, 200);
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

SimpleCar car(control);*/

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
