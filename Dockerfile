# syntax=docker/dockerfile:1

# ===========================================================================
# Étape 1 — construction
# ===========================================================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# ===========================================================================
# Étape 2 — exécution
# ===========================================================================
FROM eclipse-temurin:21-jre

RUN useradd --system --uid 10001 --shell /usr/sbin/nologin alerte

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN chown -R alerte:alerte /app

USER 10001
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
