FROM maven:3.9.11-eclipse-temurin-24 AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package

FROM eclipse-temurin:24-jre-noble

RUN useradd --system --uid 10001 --create-home wettkampf
WORKDIR /app
COPY --from=build /workspace/target/wettkampf-server-*.jar /app/wettkampf-server.jar

USER wettkampf
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/wettkampf-server.jar"]
