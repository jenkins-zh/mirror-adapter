FROM openjdk:8-jdk-stretch

COPY target/appassembler/ mirror-adapter/

RUN chmod u+x mirror-adapter/bin/app

ENTRYPOINT mirror-adapter/bin/app
