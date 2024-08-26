FROM maven:3.8-openjdk-17 as build-mail-whitelisting-plugin
RUN git clone --depth=1 -b v1.1.0 https://github.com/askask/mail-whitelisting.git
WORKDIR mail-whitelisting
RUN mvn clean install

#FROM alpine:3.14 as build-theme
#RUN apk add --no-cache git zip
#COPY keycloak-themes.json /META-INF/
#RUN mkdir theme
#WORKDIR theme
#RUN git clone --depth=1 -b main https://github.com/NUM-Forschungsdatenplattform/keycloak-theme-num.git ce
#WORKDIR /
#RUN zip -r ce.jar META-INF theme/ce/account theme/ce/email theme/ce/login theme/ce/welcome theme/ce/common


FROM quay.io/keycloak/keycloak:24.0.3

COPY --from=build-mail-whitelisting-plugin /mail-whitelisting/target/mail-whitelisting-1.1.0-SNAPSHOT.jar /opt/keycloak/providers/
#COPY --from=build-theme /ce.jar /opt/keycloak/providers/
ADD ./crr-with-user.json /opt/keycloak_import/realm.json
RUN /opt/keycloak/bin/kc.sh import --file /opt/keycloak_import/realm.json
CMD ["start-dev"]
