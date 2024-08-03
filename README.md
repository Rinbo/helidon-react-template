# helidon-quickstart-se

# TODO
- [x] Batch update (roles)
- [x] Unit testing
- [x] Integration testing
- [x] Pagination (improved version)
- [x] Authentication (magic link)
- [x] Authorization
- [x] Fix react-router browser router
- [x] Login with code instead of magic link.
- [x] Frontend layout
- [x] Implement awesome navigation
- [x] Toast
- [x] Max retries in login_passcode
- [x] Write repository integration test for authentication brute force attacks 
- [x] User cache
- [x] Error boundary
- [x] Db cleanup jobs with FOR UPDATE SKIP LOCKED - Intention to only run on leader
- [x] User CRUD
- [x] Postgres notify
- [ ] Security Integration Test
- [ ] Add Last login/authentication to user table
- [x] Email Service - log locally - attach role to EC2 Instance

Sample Helidon SE project that includes multiple REST operations.

## Build and run

With JDK21
```bash
mvn package
java -jar target/helidon-quickstart-se.jar
```


## Try metrics

```
# Prometheus Format
curl -s -X GET http://localhost:8080/observe/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/observe/metrics
{"base":...
. . .
```

## Try health

This example shows the basics of using Helidon SE Health. It uses the
set of built-in health checks that Helidon provides plus defines a
custom health check.

Note the port number reported by the application.

Probe the health endpoints:

```bash
curl -X GET http://localhost:8080/observe/health
curl -X GET http://localhost:8080/observe/health/ready
```

## Building a Native Image

The generation of native binaries requires an installation of GraalVM 22.1.0+.

You can build a native binary using Maven as follows:

```
mvn -Pnative-image install -DskipTests
```

The generation of the executable binary may take a few minutes to complete depending on
your hardware and operating system. When completed, the executable file will be available
under the `target` directory and be named after the artifact ID you have chosen during the
project generation phase.

Make sure you have GraalVM locally installed:

```
$GRAALVM_HOME/bin/native-image --version
```

Build the native image using the native image profile:

```
mvn package -Pnative-image
```

This uses the helidon-maven-plugin to perform the native compilation using your installed copy of GraalVM. It might take a while to complete.
Once it completes start the application using the native executable (no JVM!):

```
./target/helidon-quickstart-se
```

Yep, it starts fast. You can exercise the application’s endpoints as before.


## Building the Docker Image

```
docker build -t helidon-quickstart-se .
```

## Running the Docker Image

```
docker run --rm -p 8080:8080 helidon-quickstart-se:latest
```
## Deploy to fly.io

1. Create application in fly.io
   - fly
2. Add Dockerfile as listed in root (update with app name)
2. 

