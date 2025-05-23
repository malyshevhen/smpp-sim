# SMPP Simulator

A simple SMPP (Short Message Peer-to-Peer) protocol simulator written in Java, based on OpenSmpp. This project is designed for testing and development of SMPP clients and integrations.

## Features
- SMPP server simulator with configurable users
- Supports transmitter, receiver, and transceiver binds
- Message submission and delivery
- Unicode and long message support
- Dockerized for easy deployment
- JUnit 5 test suite for integration testing

## Requirements
- Java 21+
- Gradle (wrapper included)
- Docker (optional, for containerized usage)

## Getting Started

### Build with Gradle

```sh
./gradlew clean shadowJar
```

### Run Locally

```sh
java -jar build/libs/smpp-sim-1.0-SNAPSHOT-all.jar [PORT] [USERS_FILE]
```
- `PORT` (optional): Port to listen on (default: 2775)
- `USERS_FILE` (optional): Path to users config file (default: loads `users.cfg` from classpath)

### Example

```sh
java -jar build/libs/smpp-sim-1.0-SNAPSHOT-all.jar 2776 src/main/resources/users.cfg
```

### Docker Usage

Build the Docker image:
```sh
docker build -t smpp-sim .
```

Run the container:
```sh
docker run -p 2775:2775 \
  -e SMPP_SIM_PORT=2775 \
  -e SMPP_SIM_USERS_FILE=/app/users.cfg \
  smpp-sim
```

- The image will use `/app/users.cfg` by default. You can mount your own config with `-v` if needed.

### Configuration: users.cfg

The users file defines SMPP users and their permissions. Example:

```
name=boromir
password=dfsew
timeout=unlimited

name=faramir
password=prtgljrg
timeout=5
bound=t,r
```

- Each user is separated by a blank line.
- `name` and `password` are required.
- `bound` can be `t` (transmitter), `r` (receiver), or `t,r`.
- `timeout` is in minutes or `unlimited`.

### Running Tests

```sh
./gradlew test
```

## Graceful Shutdown
The simulator handles SIGTERM and Ctrl+C gracefully, ensuring all sessions are closed and the port is released.

## Project Structure
- `src/main/java/com/github/malyshevhen/SmppSim.java` - Main simulator logic
- `src/main/java/com/github/malyshevhen/App.java` - Application entry point
- `src/main/resources/users.cfg` - Example users file
- `src/test/java/com/github/malyshevhen/SmppSimTest.java` - Integration tests

## License
MIT or Apache 2.0 (specify your actual license here)

---

For questions or contributions, please open an issue or pull request.
