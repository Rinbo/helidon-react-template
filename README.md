# helidon-quickstart-se

# TODO
- [x] Batch update (roles)
- [x] Unit testing
- [x] Integration testing
- [x] Pagination (improved version)
- [ ] Authentication (magic link)
- [ ] Authorization

# AUTH
- [x] Add login endpoint where a user can provide their email - add long or short polling and reroute if a valid accessToken is obtained
- [x] Add Database schema for inserting a timed login token - loginToken, userId, expiry
- [x] Add scheduled job for deleting old login tokens
   - [ ] Figure out a way to only run on one instance at a time
- [ ] Add email link authentication route - path param is login token. If it matches in database and not expired we create a jwt and refresh token and set cookie headers
- [ ] Implement authentication provider, validate token and refresh flag

Refresh flag lives in a cache and is only updated if some change happens to a user or his roles
When set to true the cache is invalidated, and the next time a user tries to log in we don't even check the expiry of the access token
we send 401 right away to force a refresh. Refresh always makes a lookup in database. There is one issue however,
a user may have many access tokens. And only the first time the user tries to use the first one will refresh be triggered. It might not be 
a big problem if I set access token expiry to just 5 minutes. But it is yet another half measure. 

- Filter
  - "/", "/register", "/authenticate" -> proceed
  1. Check if there is an entry in cache for refresh token and if refreshing token is required
     - If required reach into db and validate refresh token. 
     - Grab user roles from db. 
     - Create a new accessToken and refresh token, persist and add to headers
     - Notify other instances to invalidate cache.
  2. Validate 5 min auth token
  3. Proceed

If role change occurs or user is deleted, cache has to be invalidated. 
If we cannot find in cache we look in db.
If we cannot find in db we throw 403
If we find in db are we happy? We just reuse it? Yes, but there will be one more field "reauthenticate" true/false.
If this field is true, we must produce new refresh token and accessToken. 
Persist, set reauthenticate flag to false, notify other instances to invalidate.
Add new Set-Cookie headers, add new tokens to cache.

How do I lookup the token? Is it a uuid?


Sample Helidon SE project that includes multiple REST operations.

## Build and run

With JDK21
```bash
mvn package
java -jar target/helidon-quickstart-se.jar
```

## Exercise the application

Basic:
```
curl -X GET http://localhost:8080/simple-greet
Hello World!
```


JSON:
```
curl -X GET http://localhost:8080/greet
{"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
{"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
{"message":"Hola Jose!"}
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

Exercise the application as described above.
                                

## Run the application in Kubernetes

If you don’t have access to a Kubernetes cluster, you can [install one](https://helidon.io/docs/latest/#/about/kubernetes) on your desktop.

### Verify connectivity to cluster

```
kubectl cluster-info                        # Verify which cluster
kubectl get pods                            # Verify connectivity to cluster
```

### Deploy the application to Kubernetes

```
kubectl create -f app.yaml                              # Deploy application
kubectl get pods                                        # Wait for quickstart pod to be RUNNING
kubectl get service  helidon-quickstart-se                     # Get service info
kubectl port-forward service/helidon-quickstart-se 8081:8080   # Forward service port to 8081
```

You can now exercise the application as you did before but use the port number 8081.

After you’re done, cleanup.

```
kubectl delete -f app.yaml
```


## Building a Custom Runtime Image

Build the custom runtime image using the jlink image profile:

```
mvn package -Pjlink-image
```

This uses the helidon-maven-plugin to perform the custom image generation.
After the build completes it will report some statistics about the build including the reduction in image size.

The target/helidon-quickstart-se-jri directory is a self contained custom image of your application. It contains your application,
its runtime dependencies and the JDK modules it depends on. You can start your application using the provide start script:

```
./target/helidon-quickstart-se-jri/bin/start
```

Class Data Sharing (CDS) Archive
Also included in the custom image is a Class Data Sharing (CDS) archive that improves your application’s startup
performance and in-memory footprint. You can learn more about Class Data Sharing in the JDK documentation.

The CDS archive increases your image size to get these performance optimizations. It can be of significant size (tens of MB).
The size of the CDS archive is reported at the end of the build output.

If you’d rather have a smaller image size (with a slightly increased startup time) you can skip the creation of the CDS
archive by executing your build like this:

```
mvn package -Pjlink-image -Djlink.image.addClassDataSharingArchive=false
```

For more information on available configuration options see the helidon-maven-plugin documentation.
                                
