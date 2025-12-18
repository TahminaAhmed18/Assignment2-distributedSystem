Distributed Weather Aggregation System

A fault-tolerant distributed system built in Java that aggregates and serves weather data using REST-style GET/PUT APIs, Lamport logical clocks for event ordering, and a custom JSON parser.

⭐ Bonus: Implemented a custom JSON parser instead of relying solely on third-party libraries.

🔍 Why this project matters

Modern distributed systems must handle:

Concurrent clients

Network delays and partial failures

Correct ordering of events across nodes

This project simulates a real-world distributed data aggregation service, demonstrating how logical clocks, event-driven design, and testing can be used to build reliable systems.

🧠 System Architecture

The system consists of three main components:

Aggregation Server
Central server that handles GET and PUT requests and maintains aggregated weather data.

Content Server
Sends weather data to the Aggregation Server using PUT requests.

Client (GETClient)
Retrieves aggregated weather data from the server using GET requests.

Lamport clocks are used across components to ensure correct event ordering in a distributed environment.

✨ Key Features

REST-style GET and PUT APIs

Lamport logical clock–based event ordering

Fault-tolerant request handling

Custom JSON parsing logic

Concurrent client simulation

Unit and integration testing with JUnit

🛠️ Tech Stack

Language: Java (JDK 11+)

Distributed Systems: Lamport Logical Clocks

Networking: HTTP-based communication

Testing: JUnit 4, Hamcrest

Libraries: Gson (JSON), OpenTest4J, JSR305

📁 Project Structure
src/
├── AggregationServer.java   # Handles GET/PUT requests
├── ContentServer.java       # Sends weather data to server
├── GETClient.java           # Retrieves weather data
├── WeatherData.java         # Data model + custom JSON parser
├── LamportClock.java        # Logical clock implementation

tests/
├── TestLamportClock.java    # Unit tests for Lamport clock
├── AggrServerTest.java      # Integration tests (GET/PUT, concurrency)

lib/
└── *.jar                    # External dependencies

🚀 How to Run
1️⃣ Compile the Project

Make sure all required JAR files are in the lib/ directory.

javac -cp ".;lib/*" src/*.java tests/*.java

2️⃣ Start the Aggregation Server
java -cp ".;lib/*" AggregationServer 4567


(Default port: 4567)

3️⃣ Run the Content Server

Uploads weather data to the Aggregation Server.

java -cp ".;lib/*" ContentServer http://localhost 4567 data/weather_data.json

4️⃣ Run the GET Client

Retrieves aggregated weather data.

java -cp ".;lib/*" GETClient http://localhost 4567

🧪 Testing
Test Lamport Clock
java -cp ".;lib/*" TestLamportClock

Run Integration Tests

Make sure the Aggregation Server is running, then:

java -cp ".;lib/*" org.junit.runner.JUnitCore AggrServerTest

🎯 What I Learned

Designing event-driven distributed systems

Applying Lamport logical clocks for synchronization

Implementing REST-style communication without frameworks

Writing testable, modular Java code

Simulating concurrency and validating system correctness

📌 Notes

The custom JSON parser in WeatherData.java was implemented manually to demonstrate understanding of data parsing and earn bonus marks.

Screenshots and demo outputs are available in the screenshots/ directory.

👤 Author

Tahmina Ahmed
Master of Computer Science — University of Adelaide
Interests: Distributed Systems, Networking, Software Engineering