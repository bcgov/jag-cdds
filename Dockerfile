FROM eclipse-temurin:11-jre-jammy

COPY ./target/cdds-application.jar cdds-application.jar

ENTRYPOINT ["java","-jar","/cdds-application.jar"]
