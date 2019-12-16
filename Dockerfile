FROM openjdk:8-jdk-alpine
RUN apk add --no-cache curl tar bash procps
RUN apk --no-cache add ca-certificates wget
RUN wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-2.30-r0.apk
RUN apk add glibc-2.30-r0.apk
RUN apk add --no-cache git openssh

# Downloading and installing Maven
# 1- Define a constant with the version of maven you want to install
ARG MAVEN_VERSION=3.6.3         

# 2- Define a constant with the working directory
ARG USER_HOME_DIR="/root"

# 4- Define the URL where maven can be downloaded from
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

# 5- Create the directories, download maven, validate the download, install it, remove downloaded file and set links
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
    && echo "Downloading maven" \
    && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    \
    && echo "Unziping maven" \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    \
    && echo "Cleaning and setting links" \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# 6- Define environmental variables required by Maven, like Maven_Home directory and where the maven repo is located
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

EXPOSE 7000:8000
WORKDIR /tmp

RUN git clone https://github.com/Fziliotti/TrocadilhoSistemasDistribuido

WORKDIR /tmp/TrocadilhoSistemasDistribuido

RUN mvn clean install

RUN cp /tmp/TrocadilhoSistemasDistribuido/target/*.jar .
RUN rm -rf servers_online.txt
RUN touch servers_online.txt