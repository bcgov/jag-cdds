FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN chgrp -R 0 /app && \
    chmod -R g=u /app

COPY ./target/cdds-test-runner.jar .

ENTRYPOINT ["java","-Xss2048k","-jar","cdds-test-runner.jar"]
