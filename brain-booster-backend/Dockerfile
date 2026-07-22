FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY src ./src

RUN ./gradlew clean bootJar -x test --no-daemon


FROM eclipse-temurin:25-jre

WORKDIR /app

# create a system group and user without root privileges / g:u
RUN addgroup --system springuser && adduser --system --ingroup springuser springuser

# copy the built JAR from the build stage and set ownership to the new user / u:g
COPY --chown=springuser:springuser --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

# switch to the non-root user before running the application
USER springuser:springuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]