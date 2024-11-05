FROM maven:3.8-openjdk-17 as build-mail-whitelisting-plugin
RUN git clone --depth=1 -b v1.1.0 https://github.com/askask/mail-whitelisting.git
WORKDIR mail-whitelisting
ARG MAVEN_OPTS="-Dhttp.proxyHost=${http_proxy_host} -Dhttp.proxyPort=${http_proxy_port} -Dhttps.proxyHost=${https_proxy_host} -Dhttps.proxyPort=${https_proxy_port}"
RUN mvn clean install


FROM quay.io/keycloak/keycloak:26.0

COPY --from=build-mail-whitelisting-plugin /mail-whitelisting/target/mail-whitelisting-1.1.0-SNAPSHOT.jar /opt/keycloak/providers/
ADD ./crr-with-user.json /opt/keycloak_import/realm.json
RUN /opt/keycloak/bin/kc.sh import --file /opt/keycloak_import/realm.json
CMD ["start-dev"]
