# Build stage
#
FROM maven:3.8.4-openjdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
# sudo docker build -t hibernate-qsar-model-building .

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/hibernate-qsar-model-building-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
# run example
# sudo docker run --rm -it jsb-qsar-models:latest --db.user=Dan --db.name=postgres --db.pass=Password1!
