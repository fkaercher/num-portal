package org.highmed.numportal.integrationtesting.config;

import org.mockserver.client.MockServerClient;
import org.mockserver.model.*;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import static org.highmed.numportal.integrationtesting.tests.IntegrationTest.IDENTITY_PROVIDER_TOKEN_ENDPOINT;

public class NumKeycloakMockContainer extends MockServerContainer {
    private static final String IMAGE_VERSION = "mockserver/mockserver:5.15.0";
    public static final String USERS_COUNT = "/admin/realms/Num/users/count";
    private static NumKeycloakMockContainer container;

    private NumKeycloakMockContainer() {
        super(DockerImageName.parse(IMAGE_VERSION));
    }

    public static NumKeycloakMockContainer getInstance() {
        if (container == null) {
            container = new NumKeycloakMockContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("KEYCLOAK_URL", "http://localhost:" + container.getServerPort());

        MockServerClient client = new MockServerClient("localhost", container.getServerPort());
        Header header = new Header("Content-Type", "application/json; charset=utf-8");
        Header authHeader = new Header("Authorization", "Bearer {{randomValue length=20 type='ALPHANUMERIC'}}");

        client
                .when(HttpRequest.request().withMethod("GET").withQueryStringParameter("enabled", "true").withPath(USERS_COUNT))
                .respond(HttpResponse.response().withStatusCode(HttpStatusCode.OK_200.code()).withHeaders(authHeader).withBody("2", MediaType.JSON_UTF_8));
        client
                .when(HttpRequest.request().withMethod("GET").withQueryStringParameter("enabled", "false").withPath(USERS_COUNT))
                .respond(HttpResponse.response().withStatusCode(HttpStatusCode.OK_200.code()).withHeaders(authHeader).withBody("0", MediaType.JSON_UTF_8));
        client
                .when(HttpRequest.request().withMethod("POST").withPath(IDENTITY_PROVIDER_TOKEN_ENDPOINT))
                .respond(HttpResponse.response().withStatusCode(HttpStatusCode.OK_200.code()).withHeaders(header).withBody("{\"token_type\": \"Bearer\",\"access_token\":\"{{randomValue length=20 type='ALPHANUMERIC'}}\"}"));
    }

    @Override
    public void stop() {
        super.stop();
    }
}
