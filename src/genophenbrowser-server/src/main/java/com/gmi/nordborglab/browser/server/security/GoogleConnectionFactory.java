package com.gmi.nordborglab.browser.server.security;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleAdapter;
import org.springframework.social.google.connect.GoogleOAuth2Template;
import org.springframework.social.google.connect.GoogleServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;

/**
 * Custom Google ConnectionFactory implementation to workround https://jira.spring.io/browse/SOCIAL-447.
 *
 *
 * @author Uemit Seren
 */
public class GoogleConnectionFactory extends OAuth2ConnectionFactory<Google> {

  private final GoogleOAuth2TemplateWrapper oauth2Template;

  public GoogleConnectionFactory(final String clientId, final String clientSecret) {
    super("google", new GoogleServiceProvider(clientId, clientSecret),
      new GoogleAdapter());
      oauth2Template = new GoogleOAuth2TemplateWrapper(new GoogleOAuth2Template(clientId, clientSecret));
  }

  public OAuth2Operations getOAuthOperations() {
    return oauth2Template;
  }

  @Override
  protected String extractProviderUserId(final AccessGrant accessGrant) {
    final Google api = ((GoogleServiceProvider) getServiceProvider()).getApi(accessGrant.getAccessToken());
    final UserProfile userProfile = getApiAdapter().fetchUserProfile(api);
    return userProfile.getUsername();
  }
}