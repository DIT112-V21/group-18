# Group 18

## What are you going to make?
We are going to make a vehicle that sanitises the surfaces from an indoor environment with the help of a UV-light. It will take as input an existent 2D map of the environment and a predefined path that will follow in order to clean such surfaces. We assume that the vehicle is able to know where it is respect to the map at every moment, as well as the starting point.       


## Why will you make it?
There is a current need to keep surfaces clean from dangerous microorganisms such that people can interact without risking to get infected from them. 
	

## What problem does it solve?
We are providing a cost effective solution to the problem of disinfecting surfaces since our solution will be able to run in a consumer-grade hardware which could be more affordable than other systems.


## How you are going to make it?
It will take a 2D map, the vehicle's initial position and trajectory to follow (setup predefined positions or waypoint positions). In the beginning it will select its closest waypoint and go to it. Once it arrives to the waypoint, it will wait a given time so it make sure that the surface is cleaned. Next, it will find the next waypoint to visit, according to the planned trajectory, and go to it. It will repeat this steps until it has visited all planned waypoints.   


## What kind of technology are you going to use?
We are going to implement this in Arduino, using the SmartCar platform. We will make use of different languages such as C#, C++, Java, Kotlin. 

## Demo video

https://www.youtube.com/watch?v=IYBenmeBbfA
