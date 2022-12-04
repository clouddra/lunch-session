FROM eclipse-temurin:17 as build
WORKDIR /workspace/app
COPY . /workspace/app
RUN ./mvnw clean install

FROM build as deployable
ENTRYPOINT ["java","-jar", "target/lunchsession-0.0.1-SNAPSHOT.jar"]
