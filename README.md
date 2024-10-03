**Weather Aggregation System - Assignment 2**


**1. Project Structure**
•	AggregationServer.java: The server that handles GET and PUT requests for weather data aggregation..
•	ContentServer.java: Sends PUT requests to upload weather data to the Aggregation Server.
•	GETClient.java: Sends GET requests to retrieve weather data from the Aggregation Server.
•	WeatherData.java: Stores weather data and includes a custom JSON parser.
•	LamportClock.java: Implements Lamport clocks for synchronized event handling.
•	TestLamportClock.java: A simple test class to verify the functionality of the Lamport clock.
•	AggrServerTest.java: A test class that simulates client requests and tests server functionality.


**2.Libraries Used**
•	Gson (gson-2.11.0.jar) Used for JSON parsing and serialization.
•	JUnit (junit-4.10.jar) For unit testing the project.
•	Hamcrest (hamcrest-core-1.1.jar) For writing matchers in tests.
•	OpenTest4J (opentest4j-1.2.0.jar) A support library for JUnit.
•	JSR305 (jsr305-3.0.1.jar) Provides annotations for code quality.


**3.How to Run the Project**

**Compilation**
Before running the project, compile all the Java files using the following command:
javac -cp ".:lib/*" AggregationServer.java ContentServer.java GETClient.java LamportClock.java WeatherData.java TestLamportClock.java AggrServerTest.java
We have to ensure that all the required JAR files (json, JUnit, etc.) are in the lib folder or add the appropriate class path.

**Running the Aggregation Server**
To start the Aggregation Server, use the following command:
java -cp ".:lib/*" AggregationServer [port_number]
By default, the server runs on port 4567

**Running the Content Server**
To run the Content Server and send weather data to the Aggregation Server, provide the server address, port number, and the file path containing the weather data:
java -cp ".:lib/*" ContentServer http://localhost 4567 data/weather_data.json

**Running the GET Client**
To retrieve weather data from the Aggregation Server, run the GET Client. 
java -cp ".:lib/*" GETClient http://localhost 4567 

**Testing the System**
The system has been tested using the following commands:
To test the Lamport Clock:
java -cp ".:lib/*" TestLamportClock
To run tests simulating multiple concurrent clients and validate GET/PUT operations:
java -cp ".:lib/*" org.junit.runner.JUnitCore AggrServerTest

**Testing the AggrServerTest**
We need to have all the necessary libraries in our project:
•	gson-2.11.0.jar
•	junit-4.10.jar (or your preferred version)
•	hamcrest-core-1.1.jar
•	Other dependencies required for JUnit or testing.


**4.To compile the code** 
javac -cp ".:lib/*" AggrServerTest.java
to run the test
java -cp ".:lib/*" org.junit.runner.JUnitCore AggrServerTest
Before running the test, make sure our AggregationServer is running on the expected port (e.g., 4567). We can start the server using: java -cp ".:lib/*" AggregationServer 4567

**5.Custom JSON Parsing (Bonus)**
The WeatherData.java class contains a custom JSON parser that converts raw data into JSON format. This parser was implemented from scratch to earn bonus marks.

