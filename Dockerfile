FROM bellsoft/liberica-openjdk-alpine:17.0.8.1
COPY fat/build/libs/sip-generation-newspapers-fat-all-1.2.3-SNAPSHOT.jar /app/sip-generation-newspapers.jar
COPY run.sh /app/run.sh
RUN chmod +x /app/run.sh
WORKDIR /app
ENTRYPOINT ["/app/run.sh"]