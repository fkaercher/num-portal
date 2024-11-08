FROM maven:3.8-openjdk-17 as build-mail-whitelisting-plugin
RUN git clone --depth=1 -b v1.1.0 https://github.com/askask/mail-whitelisting.git
WORKDIR mail-whitelisting
ARG MAVEN_OPTS="-Dhttp.proxyHost=${http_proxy_host} -Dhttp.proxyPort=${http_proxy_port} -Dhttps.proxyHost=${https_proxy_host} -Dhttps.proxyPort=${https_proxy_port}"
RUN mvn clean install


FROM tencentcom/envsubst as realm

ARG WEB_URL
ARG CLIENT_SECRET
ADD ./crr-with-user.json /crr-with-user.json
RUN envsubst '$WEB_URL,$CLIENT_SECRET' < /crr-with-user.json > /realm.json


FROM quay.io/keycloak/keycloak:26.0

COPY --from=build-mail-whitelisting-plugin /mail-whitelisting/target/mail-whitelisting-1.1.0-SNAPSHOT.jar /opt/keycloak/providers/
COPY --from=realm /realm.json /opt/keycloak_import/realm.json
RUN /opt/keycloak/bin/kc.sh import --file /opt/keycloak_import/realm.json
CMD ["start-dev"]
