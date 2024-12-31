# Async Scala Producer-Consumer

## 🚀 Project Overview

A robust, concurrent producer-consumer implementation in Scala demonstrating asynchronous message processing with configurable parameters.

### Message Packet Flow Visualization
```Bash

Producers             Queue                   Consumers
---------          -------------             -----------

P1 ──┐             │           │             ┌── C1
     │             │           │             │
P2 ──┤ ──► FIFO ──►│  Messages │ ──► Consume ┤── C2
     │             │           │             │
P3 ──┤             │           │             ├── C3
     │             │           │             │
P4 ──┘             │           │             └── C4

Legend:
P = Producer
C = Consumer
► = Message Flow
```

### More Detailed Flow:
```Bash
Producer Flow                                Consumer Flow
--------------                              --------------

   ┌─────────────┐                          ┌─────────────┐
   │ Generate ID │                          │   Process   │
   │  (UUID)     │                          │   Message   │
   └─────┬───────┘                          └─────┬───────┘
         │                                        │
         ▼                                        ▼
┌─────────────────┐                     ┌─────────────────┐
│ Add to Channel  │                     │ Remove from     │
│ (Blocking Queue)│ ────────────────►   │ Channel         │
└─────────────────┘                     └─────────────────┘
         │                                        │
         ▼                                        ▼
┌─────────────────┐                     ┌─────────────────┐
│ Track Timestamp │                     │ Calculate       │
│ & Metrics       │                     │ Processing Time │
└─────────────────┘                     └─────────────────┘
```

### Comprehensive Flow Diagram:
```Bash
[Producers]                [Shared Channel]                [Consumers]
    │                         │                               │
    ▼                         ▼                               ▼
┌───────────┐           ┌───────────────┐             ┌───────────────┐
│ Generate  │  ──push── │ LinkedBlocking│  ──poll──   │   Process     │
│ Message   │           │    Queue      │             │   Message     │
│ (UUID)    │           │ (Max Size 100)│             │ (With Delay)  │
└───────────┘           └───────────────┘             └───────────────┘
    │                         │                               │
    ▼                         ▼                               ▼
┌───────────┐           ┌───────────────┐             ┌───────────────┐
│ Log       │           │ Track Message │             │ Log Processing│
│ Produce   │           │   Metrics     │             │   Metrics     │
└───────────┘           └───────────────┘             └───────────────┘
```

### Metrics Tracking Flow:
```Bash
[Message Creation]  ──►  [Queued]  ──►  [Consumed]  ──►  [Processed]
       │                    │              │               │
       ▼                    ▼              ▼               ▼
┌─────────────┐    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Generate ID │    │ Timestamp   │  │ Start       │  │ Calculate   │
│             │    │ Enqueue     │  │ Processing  │  │ Total Time  │
└─────────────┘    └─────────────┘  └─────────────┘  └─────────────┘
```

### Performance Metrics Capture:
```Bash
Metric Capture Points:
   ┌───────────────┐
   │ Message ID    │
   │ (UUID)        │
   └───┬───────────┘
       │
       ▼
┌─────────────────┐
│ Produce Time    │
│ (Timestamp)     │
└───┬─────────────┘
    │
    ▼
┌─────────────────┐
│ Queue Wait Time │
│                 │
└───┬─────────────┘
    │
    ▼
┌─────────────────┐
│ Consume Time    │
│                 │
└───┬─────────────┘
    │
    ▼
┌─────────────────┐
│ Processing Time │
│ (Total Latency) │
└─────────────────┘
```


### ✨ Features

- Concurrent message production and consumption
- Configurable number of producers and consumers
- Dynamic message generation
- Detailed performance metrics
- Flexible delay ranges
- Comprehensive logging

## 📋 Prerequisites

- Java 11+ (Recommended: Java 17 or 21)
- Scala 2.13.x
- SBT 1.9.x

## 🛠 Technology Stack

- Scala
- SBT (Build Tool)
- ScalaTest (Unit Testing)
- Logback (Logging)
- Scopt (CLI Parsing)

## 📂 Project Structure
```Bash
async-scala/
├── src/
│ ├── main/
│ │ ├── scala/
│ │ │ └── com/example/
│ │ │ ├── Main.scala
│ │ │ ├── ProducerConsumer.scala
│ │ │ ├── ProducerConsumerConfig.scala
│ │ │ └── ProducerConsumerMetrics.scala
│ │ └── resources/
│ │ └── logback.xml
│ └── test/
│ └── scala/
│ └── com/example/
│ └── ProducerConsumerSpec.scala
├── project/
│ └── build.properties
└── build.sbt
```

## 🔧 Setup & Installation

### 1. Clone the Repository
```Bash
git clone https://github.com/yourusername/async-scala.git
cd async-scala
```

### 2. Install Dependencies

```Bash
#Install SBT (if not already installed)
#macOS
brew install sbt
```

### 3. Compile the Project
```Bash
sbt compile
```

### Run tests
```Bash
sbt test
```


## 🚀 Running the Application

### Command Line Options
```Bash
sbt run
```

### Custom configuration

```Bash

### CLI Parameters

- `--producers` (default: 10): Number of producer threads
- `--consumers` (default: 5): Number of consumer threads
- `--messages` (default: 5): Messages per producer
```

### Create Executable JAR

```Bash
sbt assembly
```

### Run the JAR
```Bash
java -jar target/scala-2.13/async-scala-assembly-0.1.0.jar
```

## 🔧 Configuration

Edit `ProducerConsumerConfig.scala` to modify default settings:
```Bash
scala
ProducerConsumerConfig(
producers = 10,
consumers = 5,
messagesPerProducer = 5,
maxQueueSize = 100,
producerDelayRange = (10, 100),
consumerDelayRange = (10, 100)
)
```


## 📝 Logging

Logging is configured in `logback.xml`. Logs are printed to console with timestamps.

## 📊 Performance Metrics

The application tracks:
- Total messages processed
- Individual message processing times
- Producer and consumer performance

## 🧪 Testing

Run comprehensive test suite:
```Bash
sbt test
```


Tests cover:
- Message processing
- Concurrent scenarios
- Edge cases

## 🛠 Troubleshooting

- Ensure Java 11+ is installed
- Check SBT and Scala versions
- Verify dependencies in `build.sbt`

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

[MIT License]

## 🔍 Additional Resources

- [Scala Documentation](https://docs.scala-lang.org/)
- [SBT Documentation](https://www.scala-sbt.org/documentation.html)
- [ScalaTest Documentation](https://www.scalatest.org/)
