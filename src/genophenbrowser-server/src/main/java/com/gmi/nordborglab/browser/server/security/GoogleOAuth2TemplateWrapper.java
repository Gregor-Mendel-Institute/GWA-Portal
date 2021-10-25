package com.gmi.nordborglab.browser.server.security;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.util.MultiValueMap;

public class GoogleOAuth2TemplateWrapper implements OAuth2Operations{

    private final OAuth2Operations oauth2Template;

    public GoogleOAuth2TemplateWrapper(OAuth2Operations oauth2Template) {
        this.oauth2Template = oauth2Template;
    }

    private String fixRedirectUrl(String redirectUrl) {
        if (redirectUrl.contains("http://") && !redirectUrl.contains("localhost")) {
            redirectUrl = redirectUrl.replace("http://", "https://");
        }
        return redirectUrl;
    }
    private OAuth2Parameters fixRedirectUrl(OAuth2Parameters parameters) {
        String redirectUrl = fixRedirectUrl(parameters.getRedirectUri());
        parameters.setRedirectUri(redirectUrl);
        return parameters;
    }

    @Override
    public String buildAuthorizeUrl(OAuth2Parameters parameters) {
        return oauth2Template.buildAuthorizeUrl(fixRedirectUrl(parameters));
    }
    @Override
    public String buildAuthorizeUrl(GrantType grantType, OAuth2Parameters parameters) {
        return oauth2Template.buildAuthorizeUrl(grantType, fixRedirectUrl(parameters));
    }
    @Override
    public String buildAuthenticateUrl(OAuth2Parameters parameters) {
        return oauth2Template.buildAuthenticateUrl(fixRedirectUrl(parameters));
    }
    @Override
    public String buildAuthenticateUrl(GrantType grantType, OAuth2Parameters parameters) {
        return oauth2Template.buildAuthenticateUrl(grantType, fixRedirectUrl(parameters));
    }
    @Override
    public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri,
            MultiValueMap<String, String> additionalParameters) {
        return oauth2Template.exchangeForAccess(authorizationCode, fixRedirectUrl(redirectUri), additionalParameters);
    }
    @Override
    public AccessGrant exchangeCredentialsForAccess(String username, String password,
            MultiValueMap<String, String> additionalParameters) {
        return oauth2Template.exchangeCredentialsForAccess(username, password, additionalParameters);
    }
    @Override
    public AccessGrant refreshAccess(String refreshToken, String scope,
            MultiValueMap<String, String> additionalParameters) {
        return oauth2Template.refreshAccess(refreshToken, scope, additionalParameters);
    }
    @Override
    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {
        return oauth2Template.refreshAccess(refreshToken, additionalParameters);
    }
    @Override
    public AccessGrant authenticateClient() {
        return oauth2Template.authenticateClient();
    }
    @Override
    public AccessGrant authenticateClient(String scope) {
        return oauth2Template.authenticateClient(scope);
    }
}