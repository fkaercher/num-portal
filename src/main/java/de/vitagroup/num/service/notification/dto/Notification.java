package de.vitagroup.num.service.notification.dto;

import de.vitagroup.num.service.email.MessageSourceWrapper;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class Notification {

  protected static final String COPYRIGHT_KEY = "num.copyright";

  protected String recipientEmail;
  protected String recipientFirstName;
  protected String recipientLastName;

  protected String adminEmail;
  protected String adminFullName;

  public String getNotificationRecipient() {
    return recipientEmail;
  }

  public abstract String getNotificationBody(MessageSourceWrapper messageSource, String url);

  public abstract String getNotificationSubject(MessageSourceWrapper messageSource);

  public String getProjectPreviewUrl(String portalUrl, Long projectId) {
    String baseUrl = getBaseUrl(portalUrl);
    if (StringUtils.isNotEmpty(baseUrl)) {
      return String.format("%s%s%d%s", baseUrl, "/projects/", projectId, "/editor?mode=preview");
    } else {
      return "-";
    }
  }

  public String getProjectExplorerUrl(String portalUrl, Long projectId) {
    String baseUrl = getBaseUrl(portalUrl);
    if (StringUtils.isNotEmpty(baseUrl)) {
      return String.format("%s%s%d", baseUrl, "/data-explorer/projects/", projectId);
    } else {
      return "-";
    }
  }

  private String getBaseUrl(String portalUrl) {
    if (StringUtils.isNotEmpty(portalUrl)) {
      try {
        URL url = new URL(portalUrl);
        return url.getProtocol() + "://" + url.getHost();
      } catch (MalformedURLException e) {
        log.warn("Cannot extract base url");
      }
    }
    return StringUtils.EMPTY;
  }
}
