FROM eclipse-temurin:17 AS builder

WORKDIR /tmp/cinephobia
COPY . .

RUN ./mvnw install

FROM eclipse-temurin:17

RUN ["useradd", "-r", "cinephobia"]
USER cinephobia

WORKDIR /opt/cinephobia
COPY --from=builder /tmp/cinephobia/target/cinephobia-*.jar ./cinephobia-app.jar

CMD ["java", "-jar", "/opt/cinephobia/cinephobia-app.jar"]
