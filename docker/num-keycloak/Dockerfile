FROM quay.io/keycloak/keycloak:24.0.3

ADD ./crr-with-user.json /opt/keycloak_import/realm.json
RUN /opt/keycloak/bin/kc.sh import --file /opt/keycloak_import/realm.json
CMD ["start-dev"]
