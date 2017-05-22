FROM openjdk 
ARG jar
VOLUME /tmp
ADD $jar actionsvc.jar
RUN sh -c 'touch /actionsvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /actionsvc.jar" ]
