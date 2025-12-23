# Multi-stage build for Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Optional proxy settings for restricted networks (passed via build args)
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY
ENV http_proxy=${HTTP_PROXY}
ENV https_proxy=${HTTPS_PROXY}
ENV no_proxy=${NO_PROXY}

# Tencent Cloud Maven mirror (recommended on Tencent Cloud hosts)
ARG MAVEN_MIRROR="https://mirrors.cloud.tencent.com/nexus/repository/maven-public/"

RUN set -eux; \
  printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"' \
    '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
    '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>tencent-maven-public</id>' \
    '      <mirrorOf>central</mirrorOf>' \
    '      <name>Tencent Cloud Maven Public</name>' \
    "      <url>${MAVEN_MIRROR}</url>" \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' \
    > /tmp/settings.xml; \
  test -s /tmp/settings.xml

# Set working directory
WORKDIR /app

# Copy pom files
COPY pom.xml ./
COPY pet-common/pom.xml pet-common/
COPY pet-pojo/pom.xml pet-pojo/
COPY pet-service/pom.xml pet-service/
COPY pet-web/pom.xml pet-web/

# Download dependencies (this layer will be cached)
ENV MAVEN_MIRROR=${MAVEN_MIRROR}
RUN mvn -s /tmp/settings.xml dependency:go-offline -DskipTests -B

# Copy source code
COPY pet-common/src pet-common/src
COPY pet-pojo/src pet-pojo/src
COPY pet-service/src pet-service/src
COPY pet-web/src pet-web/src

# Build the application
RUN mvn -s /tmp/settings.xml clean package -DskipTests -B

# Production stage
FROM busybox:1.36.1-musl AS tools

FROM eclipse-temurin:21-jre-jammy

# Optional proxy settings for restricted networks (passed via build args)
ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY
ENV http_proxy=${HTTP_PROXY}
ENV https_proxy=${HTTPS_PROXY}
ENV no_proxy=${NO_PROXY}

COPY --from=tools /bin/busybox /bin/busybox
RUN set -eux; \
  mkdir -p /usr/bin; \
  ln -sf /bin/busybox /usr/bin/wget; \
  ln -sf /bin/busybox /usr/bin/grep; \
  ln -sf /bin/busybox /usr/bin/nc

# Create application user
RUN groupadd -r petapp && useradd -r -g petapp petapp

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/pet-web/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R petapp:petapp /app

# Switch to non-root user
USER petapp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD sh -c "nc -z 127.0.0.1 8080"

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
ENV SPRING_PROFILES_ACTIVE="docker"

# Run the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
