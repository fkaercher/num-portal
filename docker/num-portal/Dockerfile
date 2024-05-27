FROM sapmachine:21-jdk-headless-ubuntu AS build

ENV HOME=/opt/num-portal
WORKDIR $HOME
RUN mkdir -p $HOME

ADD mvnw $HOME
ADD .mvn/wrapper $HOME/.mvn/wrapper
ADD pom.xml $HOME
RUN /opt/num-portal/mvnw verify --fail-never

COPY . $HOME
RUN /opt/num-portal/mvnw clean package -Dmaven.test.skip=true


FROM sapmachine:21-jdk-headless-ubuntu

COPY --from=build /opt/num-portal/target/num-portal-*.jar /opt/num-portal/num-portal.jar
CMD ["java", "-jar", "/opt/num-portal/num-portal.jar"]
