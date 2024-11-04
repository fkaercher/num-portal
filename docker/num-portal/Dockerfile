FROM sapmachine:21-jdk-headless-ubuntu AS build

ENV HOME=/opt/num-portal
WORKDIR $HOME
RUN mkdir -p $HOME

ARG MAVEN_OPTS="-Dhttp.proxyHost=${http_proxy_host} -Dhttp.proxyPort=${http_proxy_port} -Dhttps.proxyHost=${https_proxy_host} -Dhttps.proxyPort=${https_proxy_port}"

ADD mvnw $HOME
ADD .mvn/wrapper $HOME/.mvn/wrapper
ADD pom.xml $HOME
RUN /opt/num-portal/mvnw verify --fail-never

COPY . $HOME
RUN /opt/num-portal/mvnw clean package -Dmaven.test.skip=true


FROM sapmachine:21-jdk-headless-ubuntu

COPY --from=build /opt/num-portal/target/num-portal-*.jar /opt/num-portal/num-portal.jar
EXPOSE 8080/tcp
CMD ["java", "-jar", "/opt/num-portal/num-portal.jar"]
