#include <vector>

#include <MQTT.h>
#include <WiFi.h> 
#ifdef __SMCE__
#include <OV767X.h>
#endif

#include <Smartcar.h>

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
const int FRONT_US_PIN_6 = 6;
const int FRONT_US_PIN_7 = 7;
SR04 frontUS(arduinoRuntime, FRONT_US_PIN_6, FRONT_US_PIN_7, 200);
const int FRONT_IR_PIN_0 = 0;
const int LEFT_IR_PIN_1 = 1;
const int RIGHT_IR_PIN_2 = 2;
const int BACK_IR_PIN_3 = 3;
GP2D120 frontIR(arduinoRuntime, FRONT_IR_PIN_0);
GP2D120 leftIR(arduinoRuntime,LEFT_IR_PIN_1);
GP2D120 backIR(arduinoRuntime,BACK_IR_PIN_3);

const int NON_VALID_MEASUREMENT = 0;
boolean finishedCleaning = true;
boolean manualControl = true;

unsigned int minDistance = 0;
unsigned int obstacleAvoidDistance = 100;

std::vector<char> frameBuffer;

void setup() {
  Serial.begin(9600);
#ifdef __SMCE__
  Camera.begin(QVGA, RGB888, 15);
  frameBuffer.resize(Camera.width() * Camera.height() * Camera.bytesPerPixel());
  mqtt.begin("aerostun.dev", 1883, WiFi);
  // mqtt.begin(WiFi); // Will connect to localhost
#else
  mqtt.begin(net);
#endif
  if (mqtt.connect("arduino", "public", "public")) {
    mqtt.subscribe("/smartcar/control/#", 1);
    mqtt.onMessage([](String topic, String message) {
      if (topic == "/smartcar/control/throttle") {
        car.setSpeed(message.toInt());
      } else if (topic == "/smartcar/control/steering") {
        car.setAngle(message.toInt());
      } else if(topic == "/smartcar/control/autoclean"){
        if(message.toInt()== 1){
          cleanSurfaces();
        }
      } else {
         Serial.println(topic + " " + message);
      }    
    });
  }
}

 


void loop() {
     if (!manualControl && !finishedCleaning) { //if not manual control and not finished cleaning, run cleanSurfaces function
        finishedCleaning = cleanSurfaces();
     }

  if (mqtt.connected()) {
    mqtt.loop();
    const auto currentTime = millis();
#ifdef __SMCE__
    static auto previousFrame = 0UL;
    if (currentTime - previousFrame >= 65) {
      previousFrame = currentTime;
      Camera.readFrame(frameBuffer.data());
      mqtt.publish("/smartcar/camera", frameBuffer.data(), frameBuffer.size(),
                   false, 0);
    }
#endif
    static auto previousTransmission = 0UL;
    if (currentTime - previousTransmission >= oneSecond) {
      previousTransmission = currentTime;
      const auto distance = String(frontUS.getDistance());
      mqtt.publish("/smartcar/ultrasound/front", distance);
    }
  }
#ifdef __SMCE__
  // Avoid over-using the CPU if we are running in the emulator
  delay(35);
#endif
}

// //angle input is 0 to 359 degrees, beginning at 0 to the right (analog stick)
//void joystick(float magnitude, int angle) {
//  if(angle <= 180) {
//    angle-=90;
//  } 
//  else {
//    magnitude *= -1.f;
//    angle -= 270;
//  }
//  if(magnitude > 0) angle *= -1;
//  car.setSpeed(magnitude*100.0f);
//  car.setAngle(angle);
//  //delay(10); //delay controls, disabled for now
//}

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

boolean orientCar(){
   unsigned int maxDistance = 39;
   unsigned int lateralRange = readSensor(leftIR);
   unsigned int orientingMaxAttempts = 155;
   boolean oriented = false;
   Serial.print("Orienting car. ");
   Serial.print(lateralRange);
   unsigned int count = 0;
   while (lateralRange >= maxDistance || lateralRange == NON_VALID_MEASUREMENT){
     lateralRange = readSensor(leftIR);
     turnLeft();
     Serial.println(lateralRange);
     Serial.print("Orienting attempt count: "); 
     Serial.println(count);
     ++count;
     if (count > orientingMaxAttempts){
        break;
     }  
   }
   car.setSpeed(0);
   if (count < orientingMaxAttempts){
    oriented = true;
   }
   return oriented;
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
  delay(500);
  car.setSpeed(0);
}
 
boolean followSurface(){
  unsigned int minDistance = 10;
  unsigned int maxDistance = 39;
  unsigned int lateralRange = readSensor(leftIR);
  unsigned int frontalRange = readSensor(frontUS);
  unsigned int minUSDistance = 20;
  while (lateralRange > minDistance && lateralRange < maxDistance){
    car.setSpeed(10);
    Serial.println("Going forward (followSurface). ");
    lateralRange = readSensor(leftIR);
    frontalRange = readSensor(frontUS);
    Serial.println(lateralRange);
    Serial.println(frontalRange);
    if (frontalRange != NON_VALID_MEASUREMENT && frontalRange < minUSDistance){
      break;
    }
    if (lateralRange == NON_VALID_MEASUREMENT){
      break;
    }
  }
  car.setSpeed(0);
  lateralRange = readSensor(leftIR);
  boolean goToNextSurface = false;
  if (lateralRange >= maxDistance || lateralRange == NON_VALID_MEASUREMENT){
    boolean oriented = orientCar();
    if (oriented == false){
      turnRight();
      goToNextSurface = true;
      Serial.println("Going to the next surface. ");
    }
  }
  else if(frontalRange < minUSDistance){
    Serial.println("Frontal range less than minimum ");
    car.setSpeed(0);
    delay(500);
    car.overrideMotorSpeed(100, -100);
    delay(200);
    car.setSpeed(0);
    delay(200);
    goToNextSurface = true; 
    Serial.println("Going to next surface. ");
  }
  return goToNextSurface;
}

boolean cleanSurfaces(){
  const int MAX_CLEANING_TIME = 100;
  unsigned int count = 0;
  while(count < MAX_CLEANING_TIME){
     goToSurface();
     orientCar();
     boolean goToNextSurface = false;
     while(goToNextSurface == false){
        goToNextSurface = followSurface();
     }
     ++count;
  }
  Serial.println("Finish Cleaning. ");
  return true;
}
