FROM openjdk:8-jdk
ARG jar
VOLUME /tmp
COPY $jar actionsvc.jar
RUN sh -c 'touch /actionsvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /actionsvc.jar" ]
