# Concurrent Producer-Consumer Message Processing System

## 🚀 Project Overview

This Java-based application demonstrates a sophisticated concurrent message processing system using the producer-consumer design pattern. It showcases advanced concurrent programming techniques, thread management, and configurable message processing.

## 🔧 System Architecture

### Architectural Diagram
```
+-------------------+     +-------------------+
|    Producers      |     |    Consumers      |
+-------------------+     +-------------------+
| - Generate msgs   |     | - Process msgs    |
| - Put in channel  | --> | - Consume from    |
+-------------------+     |   shared channel  |
                          +-------------------+
                          |   Blocking Queue  |
                          +-------------------+
```

### Message Flow Diagram
```
 Producer 1                Producer 2                Producer N
     |                         |                        |
     v                         v                        v
+--------------------+   +--------------------+   +--------------------+
| Generate Message   |   | Generate Message   |   | Generate Message   |
+--------------------+   +--------------------+   +--------------------+
     |                         |                        |
     |                         |                        |
     v                         v                        v
+------------------------------------------------------------+
|                   Blocking Message Queue                   |
|   [Message 1]  [Message 2]  [Message 3]  ...  [Message N]  |
+------------------------------------------------------------+
     |                         |                        |
     |        Concurrent       |                        |
     |       Consumption       |                        |
     v                         v                        v
+--------------------+   +--------------------+   +--------------------+
| Consumer 1         |   | Consumer 2         |   | Consumer N         |
| - Process Message  |   | - Process Message  |   | - Process Message  |
| - Track Duration   |   | - Track Duration   |   | - Track Duration   |
+--------------------+   +--------------------+   +--------------------+
     |                         |                        |
     v                         v                        v
+------------------------------------------------------------+
|                   Processed Message Results                |
|   [Processed Msg] [Processed Msg] [Processed Msg]          |
+------------------------------------------------------------+
```


### Key Components

1. **Producers**: 
   - Generate unique messages
   - Add messages to a shared blocking queue
   - Simulate variable message creation time

2. **Consumers**:
   - Remove messages from the shared queue
   - Process messages with variable processing time
   - Track message processing duration

3. **Shared Channel**:
   - Thread-safe `ArrayBlockingQueue`
   - Manages message transfer between producers and consumers

## 🌟 Features

- Configurable number of producers and consumers
- Dynamic thread pool management
- Message processing time tracking
- Graceful thread shutdown
- Comprehensive logging
- Command-line configuration

## 🛠 Technical Details

### Core Classes

- `MessageProcessor`: Orchestrates message processing
- `Message`: Immutable message representation
- `Duration`: Processing time tracking
- `ProducerConsumerApp`: CLI entry point

### Concurrency Mechanisms

- `ExecutorService` for thread management
- `ConcurrentHashMap` for thread-safe tracking
- `BlockingQueue` for inter-thread communication
- Configurable queue capacity

## 📦 Technology Stack

- **Language**: Java 17
- **Concurrency**: Java Concurrent APIs
- **CLI**: Picocli
- **Logging**: SLF4J
- **Testing**: JUnit 5

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 8.x

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/producer-consumer-app.git
cd producer-consumer-app
```
2. Build the project
```bash
./gradlew clean build
```
3. Run the application
```bash
# Default configuration
./gradlew run
```

```bash
./gradlew run --args="--numProducers 5 --numConsumers 5 --queueCapacity 100 --messageCreationDelay 1000 --messageProcessingDelay 500"
```

### Command-line Options

- `-p, --producers`: Number of producers (default: 10)
- `-c, --consumers`: Number of consumers (default: 5)
- `-m, --messages`: Messages per producer (default: 5)

## 🧪 Testing

Run comprehensive test suite:
```bash
./gradlew test
```


## 📊 Performance Characteristics

- Dynamic thread pool scaling
- Non-blocking message processing
- Configurable processing delays
- Detailed performance logging

## 🔍 Logging

Utilizes SLF4J for comprehensive logging:
- INFO level for system events
- DEBUG level for detailed message tracking
- ERROR level for exception handling

## 🚧 Potential Improvements

- Advanced metrics collection
- Configurable backoff strategies
- Enhanced error handling
- Prometheus/Micrometer integration for monitoring

## 📜 License

MIT License

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request
