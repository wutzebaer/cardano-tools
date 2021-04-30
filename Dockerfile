FROM adoptopenjdk/openjdk11:ubuntu-jre
RUN apt-get update && apt-get install -y \
  docker.io \
  && rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
#-v /var/run/docker.sock:/var/run/docker.sock