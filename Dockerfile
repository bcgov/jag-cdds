FROM openjdk:11-jre-slim

COPY ./target/cdds-application.jar cdds-application.jar

ENTRYPOINT ["java","-jar","/cdds-application.jar"]
