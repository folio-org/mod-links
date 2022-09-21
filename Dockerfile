FROM folioci/alpine-jre-openjdk17:latest

USER root

# Copy your fat jar to the container
ENV APP_FILE mod-entity-links.jar
# - should be a single jar file
ARG JAR_FILE=./target/*.jar
# - copy
COPY ${JAR_FILE} ${JAVA_APP_DIR}/${APP_FILE}

# Install latest patch versions of packages: https://pythonspeed.com/articles/security-updates-in-docker/
RUN apk upgrade --no-cache
USER folio

# Expose this port locally in the container.
EXPOSE 8081
