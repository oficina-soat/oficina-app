FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
ARG QUARKUS_DATASOURCE_DB_KIND=postgresql
ARG QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT=import.sql
COPY . .
RUN chmod +x mvnw && \
    QUARKUS_DATASOURCE_DB_KIND=${QUARKUS_DATASOURCE_DB_KIND} \
    QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT=${QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT} \
    ./mvnw -B -DskipTests package
FROM eclipse-temurin:25-jre
WORKDIR /work
COPY --from=build /workspace/target/quarkus-app/ /work/quarkus-app/
EXPOSE 8080
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-jar", "/work/quarkus-app/quarkus-run.jar"]
