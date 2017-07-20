FROM openjdk:8u121-jre
MAINTAINER Kieran Wardle <kieran.wardle@ons.gov.uk>
ARG jar
VOLUME /tmp
COPY $jar actionsvc.jar
RUN sh -c 'touch /actionsvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /actionsvc.jar" ]
