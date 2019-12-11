FROM maven:latest
EXPOSE 3000
COPY . /tmp/
WORKDIR /tpm/

RUN mvn -B -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
RUN mvn exec:java -Dexec.mainClass="trocadilho.ServerGRPC"
RUN mvn exec:java -Dexec.mainClass="trocadilho.Client"
