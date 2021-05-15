#include <vector>
#include <MQTT.h>
#include <WiFi.h> 
#include <algorithm>
#ifdef __SMCE__
//#include <OV767X.h>
#endif

#include <Smartcar.h>

// value taken from POSIX according to TA Ryan
#define PI 3.14159265358979323846264338327950288

#ifndef __SMCE__
WiFiClient net;
#endif
MQTTClient mqtt;

ArduinoRuntime arduinoRuntime;
const auto oneSecond = 1000UL;
const int FRONT_US_PIN_6 = 6;
const int FRONT_US_PIN_7 = 7;
SR04 frontUS(arduinoRuntime, FRONT_US_PIN_6, FRONT_US_PIN_7, 200);

const int FRONT_IR_PIN_0 = 0;
const int LEFT_IR_PIN_1 = 1;
const int RIGHT_IR_PIN_2 = 2;
const int BACK_IR_PIN_3 = 3;
GP2D120 frontIR(arduinoRuntime, FRONT_IR_PIN_0);
GP2D120 leftIR(arduinoRuntime,LEFT_IR_PIN_1);

const int NON_VALID_MEASUREMENT = 0;
boolean finishedCleaning = true;

BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);

DifferentialControl control(leftMotor, rightMotor);
SimpleCar car(control);

unsigned int minDistance = 0;
unsigned int obstacleAvoidDistance = 100;

std::vector<char> frameBuffer;

void setup() {
  Serial.begin(100);
  Serial.setTimeout(200);
  Serial.begin(9600);
#ifdef __SMCE__
//  Camera.begin(QVGA, RGB888, 15);
//  frameBuffer.resize(Camera.width() * Camera.height() * Camera.bytesPerPixel());
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
  if (finishedCleaning == false){
     finishedCleaning = cleanSurfaces();
  }
  
  if (mqtt.connected()) {
   mqtt.loop();
    const auto currentTime = millis();
#ifdef __SMCE__
    static auto previousFrame = 0UL;
    if (currentTime - previousFrame >= 65) {
      previousFrame = currentTime;
      //Camera.readFrame(frameBuffer.data());
      //mqtt.publish("smartcar/camera", frameBuffer.data(), frameBuffer.size(), //Publish camera data (disabled for now)
      //             false, 0);
    }
#endif
    static auto previousTransmission = 0UL;
    if (currentTime - previousTransmission >= oneSecond) {
      previousTransmission = currentTime;
      const auto distance = String(frontUS.getDistance());
      mqtt.publish("smartcar/ultrasound/front", distance); //publish US data
    }
  }
#ifdef __SMCE__
  // Avoid over-using the CPU if we are running in the emulator
  delay(35);
#endif
}

 //assuming normalized values between -1 and 1
 //Todo: reimplement this function with angle and magnitude since the android joystick natively uses this
void joystick(float x, float y) {
  // pythagoras for magnitude of the vector
  float magnitude = sqrt(x*x + y*y);
  // dot product of forward vector [0, 1] and [x, y] divided by the magnitudes of the two vectors, then take arccos for the angle and convert from radians to degrees
  double degree = (acos(-y / magnitude)) * 180 / PI - 90;

  // figure out if we're going forward or backward, also amplify value from a normalized -1 to 1 input
  if (y >= 0)
    magnitude = magnitude * -100;
  else 
    magnitude = magnitude * 100;

  // check if we're going left
  if (x < 0){
    degree = abs(degree) * -1;
  }
  else {
    degree = abs(degree);
  }

  car.setSpeed(magnitude);
  if(degree > -20 && degree < 20){
    car.setAngle((int)degree);
  }
  delay(10);
}

unsigned int readSensor(DistanceSensor& distanceSensor){
  unsigned int distance = distanceSensor.getDistance();
  delay(20);
  return distance;
}

void goToSurface() {
  unsigned int maxDistance = 60;
  unsigned int distance = readSensor(frontUS);
  Serial.println(distance);
  while(distance > maxDistance || distance == NON_VALID_MEASUREMENT) {
    car.setSpeed(50);
    distance = readSensor(frontUS);
    Serial.println(distance);
    Serial.println("Going forward. ");
  }
  car.setSpeed(0);
  delay(100);
  Serial.print("Stopping. ");
  Serial.println(distance);
  delay(100); 
}

void orientCar(){
   unsigned int maxDistance = 39;
   unsigned int lateralRange = readSensor(leftIR);
   Serial.print("Orienting car. ");
   Serial.print(lateralRange);
   while (lateralRange >= maxDistance || lateralRange == NON_VALID_MEASUREMENT){
     lateralRange = readSensor(leftIR);
     turnLeft();
     Serial.println(lateralRange); 
   }
   lateralRange = readSensor(leftIR);
   car.setSpeed(0);
}

void turnLeft(){
  car.overrideMotorSpeed(-24,12);
  Serial.println("Turning Left. ");
  delay(100);
  car.setSpeed(0);
}

void turnRight(){
  car.overrideMotorSpeed(12, -24);
  Serial.println("Turning Right. ");
  delay(100);
  car.setSpeed(0);
}
 
void followSurface(){
  unsigned int minDistance = 10;
  unsigned int maxDistance = 39;
  unsigned int lateralRange = readSensor(leftIR);
  unsigned int frontalRange = readSensor(frontIR);
  while (lateralRange > minDistance && lateralRange < maxDistance && frontalRange < minDistance ){
    car.setSpeed(10);
    Serial.println("Going forward (followSurface). ");
    lateralRange = readSensor(leftIR);
    Serial.println(lateralRange);
  }
  car.setSpeed(0);
  lateralRange = readSensor(leftIR);
  if (lateralRange >= maxDistance){
    unsigned int maxDistance = 39;
    unsigned int lateralRange = readSensor(leftIR);
    Serial.print("Orienting car. ");
    Serial.print(lateralRange);
    while (lateralRange >= maxDistance){
      lateralRange = readSensor(leftIR);
      turnLeft();
      Serial.println(lateralRange); 
    }
    lateralRange = readSensor(leftIR);
    car.setSpeed(0);
    }  
}

boolean cleanSurfaces(){
  const int MAX_CLEANING_TIME = 100;
  unsigned int count = 0;
  while(count < MAX_CLEANING_TIME){
     goToSurface();
     orientCar();
     for (int i = 0; i < 100; ++i){
        followSurface();
     }
     ++count;
  }
  Serial.println("Finish Cleaning. ");
  return true;
}
