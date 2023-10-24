FROM openjdk:17
EXPOSE 8080
ADD out/artifacts/http_server_jar/http-server.jar dockerapp.jar
ENTRYPOINT ["java","-jar","dockerapp.jar"]