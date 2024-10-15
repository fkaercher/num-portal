package org.highmed.numportal.service.aft;

import org.apache.http.impl.client.CloseableHttpClient;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClientConfig;
import org.ehrbase.openehr.sdk.client.openehrclient.defaultrestclient.DefaultRestClient;
import org.ehrbase.openehr.sdk.webtemplate.templateprovider.TemplateProvider;

public class AftRestClient extends DefaultRestClient {
    public AftRestClient(OpenEhrClientConfig config, TemplateProvider templateProvider, CloseableHttpClient httpClient) {
        super(config, templateProvider, httpClient);
    }
}
