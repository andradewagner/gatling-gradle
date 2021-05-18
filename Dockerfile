FROM openjdk:8u151-jdk-alpine3.7

RUN apk add -U tzdata && \
    cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime

ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 4.10

RUN set -o errexit -o nounset && \
    echo "Instalando dependências" && \
        apk add --no-cache unzip && \
        echo "Baixando Gradle" && \
        wget -O gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
        echo "Instalando Gradle" && \
        unzip gradle.zip && \
        rm gradle.zip && \
        mkdir /opt && \
        mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" && \
        ln -s "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle && \
        apk del unzip wget

WORKDIR /gatling

COPY src/ /gatling/src
COPY gradle/ /gatling/gradle
COPY build.gradle /gatling
COPY settings.gradle /gatling
COPY gradlew /gatling

RUN gradle compileGatlingScala