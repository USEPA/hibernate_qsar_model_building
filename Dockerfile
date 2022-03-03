# Build stage
#
FROM maven:3.8.4-openjdk-11 AS build
EXPOSE 9090
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
# sudo docker build -t hibernate-qsar .
# ?? figure out env variable stuff

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/jsb-qsar-models-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
# run example
# sudo docker run -p 9090:9090 --rm -it hibernate-qsar:latest
