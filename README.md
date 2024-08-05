# helidon-react-template
This repository contains a full stack web application template with a Java Helidon SE backend and React frontend.
The intention with the template is to allow users to get going faster with new projects. The following has been set up:

- Security module with cookie based access control
- Password-less authentication where a login code is emailed to user (valid for 5 minutes)
- Role based authorization
- Flyway database migration and schema management
- Local session and user caching
- Cache invalidation by use of Postgres notify (to allow for horizontal scaling)
- Frontend routing with react-router
- Toast/flash messages for user notifications
- Same site serving of frontend and backend through the use of `frontend-maven-plugin`
- TailwindCSS and daisyUI for styling

## Local development
- Create a postgres database (eg. in docker), and add connection details to `applicaiton.yaml`
- Start backend from Intellij
- cd to `src/main/frontend` and run `npm run dev`
- Open browser and navigate to `localhost:5173` (only in dev mode)
- When building the project either as jar or native image, frontend and backend will be served from port `8080` (default)

## Production build
App can be built either as a jar or native image. However, as of the time of writing this Flyway does not work with native images.

```bash
mvn package
java -jar target/helidon-quickstart-se.jar
```
Navigate to `localhost:8080`

## Deploy to fly.io
Obviously you need a Fly.io account with their CLI installed in your terminal. Fly.toml and GitHub Action script will be created automatically when running below commands:
- fly launch -r arn --name app-name
- fly postgres create -r arn --name app-name-db
- fly postgres attach --app app-name app-name-db
- fly secrets set DB_CONNECTION_URL=jdbc:postgresql://app-name-db.flycast:5432/app_name?useSSL=false -a app-name
- fly secrets set DB_CONNECTION_USERNAME=xxx -a app-name
- fly secrets set DB_CONNECTION_PASSWORD=xxx -a app-name
- fly secrets set AWS_REGION=eu-north-1 -a app-name
- fly secrets set AWS_ACCESS_KEY_ID=xxx -a app-name
- fly secrets set AWS_SECRET_ACCESS_KEY=xxx -a app-name
- fly secrets set APP_PROFILE=PROD -a app-name
- fly deploy --local-only
- fly tokens create deploy -x 999999h
- Add token to gh actions with name FLY_API_TOKEN
- Update fly-deploy.yml line 16 to `- run: flyctl deploy --local-only` to build the image on gh machine instead of the fly machine (more memory)

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

## Native image troubleshooting
If for whatever reason the native image is failing to be built try the following
- See if more `--initialize-at-build-time` needs to added to `native-image.properties`
- Package a jar normally: `mvn clean install`
- Run the following: `java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar target/helidon-quickstart-se.jar`
- Copy output in the META-INF created in root to the META-INF folder in the `/resources` folder
- Switch to GraalVM and try building again `mvn clean package -Pnative-image -DskipTests`
- Try running it locally `./target/helidon-quickstart-se`


