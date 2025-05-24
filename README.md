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
java -jar build/libs/smpp-sim-1.0-SNAPSHOT-all.jar [API_PORT] [SMPP_PORT] [USERS_FILE]
```

- `API_PORT` (optional): REST API port to listen on (default 8080);
- `SMPP_PORT` (optional): Port to listen on (default: 2775)
- `USERS_FILE` (optional): Path to users config file (default: loads `users.cfg` from classpath)

### Example

```sh
java -jar build/libs/smpp-sim-1.0-SNAPSHOT-all.jar 8080 2776 src/main/resources/users.cfg
```

### Docker Usage

Build the Docker image:

```sh
docker build -t smpp-sim .
```

Run the container:

```sh
docker run -p 2775:2775 \
  -p 8080:8080 \
  -e API_PORT=8080 \
  -e SMPP_SIM_PORT=2775 \
  -e SMPP_SIM_USERS_FILE=/app/users.cfg \
  smpp-sim
```

- The image will use `/app/users.cfg` by default. You can mount your own config with `-v` if needed.

### Configuration: users.cfg

The users file defines SMPP users and their permissions. Example:

```config
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

## REST API for SMPP Introspection

The simulator exposes a REST API for inspecting received SMPP requests and messages. This API is useful for integration testing, monitoring, and debugging.

### OpenAPI Specification

The OpenAPI (Swagger) spec for the REST API is available at:

```yaml
api/openapi.yaml
```

You can use this file with tools like [Swagger UI](https://swagger.io/tools/swagger-ui/) or [Redoc](https://redocly.com/) to explore and test the API interactively.

### API Endpoints

- `GET  /api/v1/messages/short-messages` — List all single SubmitSM (short message) requests received by the simulator.
- `GET  /api/v1/messages/multi-messages` — List all SubmitMultiSM requests received by the simulator.
- `GET  /api/v1/bind-requests` — List all SMPP bind requests received by the simulator.
- `POST /api/v1/clean` - Restore service state.

All endpoints return JSON arrays of objects matching the schema in the OpenAPI spec.

#### Example Usage

Fetch all single SubmitSM requests:

```sh
curl http://localhost:8080/api/v1/requests/single_sm
```

Fetch all bind requests:

```sh
curl http://localhost:8080/api/v1/requests/bind
```

#### API Server

The REST API server is started automatically with the simulator and listens on port `8080` by default. You can change the port in the source code if needed.

## License

MIT or Apache 2.0

---

For questions or contributions, please open an issue or pull request.
