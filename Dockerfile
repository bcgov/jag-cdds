FROM eclipse-temurin:17-jre-alpine

COPY ./target/cdds-application.jar cdds-application.jar

ENTRYPOINT ["java","-jar","/cdds-application.jar"]
