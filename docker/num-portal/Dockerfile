FROM sapmachine:21-jdk-headless-ubuntu AS build

COPY . /opt/num-portal
WORKDIR /opt/num-portal
RUN /opt/num-portal/mvnw clean package -Dmaven.test.skip=true


FROM sapmachine:21-jdk-headless-ubuntu

COPY --from=build /opt/num-portal/target/num-portal-*.jar /opt/num-portal/num-portal.jar
CMD ["java", "-jar", "/opt/num-portal/num-portal.jar"]
