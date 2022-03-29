FROM openjdk:latest
#RUN apt-get update && apt-get install -y maven && apt-get install -y asciinema
COPY ./target/webpos-0.0.1-SNAPSHOT.jar /app/
WORKDIR /app/
EXPOSE 8080
ENTRYPOINT java -jar ./webpos-0.0.1-SNAPSHOT.jar
