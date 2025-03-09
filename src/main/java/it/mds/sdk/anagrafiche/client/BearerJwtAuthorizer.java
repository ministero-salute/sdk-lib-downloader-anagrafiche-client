/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class BearerJwtAuthorizer implements Authorizer<HttpHeaders> {

    static final String TOKEN_HEADER_FORMAT = "%s %s";

    private static final long THRESHOLD = 10000;

    private String tokenIssuerUrl;
    private String grantType;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String scope;



    private RestTemplate restTemplate;

    public BearerJwtAuthorizer(Properties conf) {
        this.tokenIssuerUrl = conf.getProperty("rest.authorizer.token-issuer.url", null);
        this.grantType = conf.getProperty("rest.authorizer.token-issuer.grant_type", null);
        this.username = conf.getProperty("rest.authorizer.token-issuer.username", null);
        this.password = conf.getProperty("rest.authorizer.token-issuer.password", null);
        this.clientId = conf.getProperty("rest.authorizer.token-issuer.client_id", null);
        this.clientSecret = conf.getProperty("rest.authorizer.token-issuer.client_secret", null);
        this.scope = conf.getProperty("rest.authorizer.token-issuer.scope", null);
        restTemplate = restTemplate(conf);
    }


    private RestTemplate restTemplate(Properties conf){
        return new RestTemplate(ignoreSslVerificationRequestFactory());
    }



    private ClientHttpRequestFactory ignoreSslVerificationRequestFactory() {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        requestFactory.setReadTimeout(5000);
        requestFactory.setConnectTimeout(10000);

        return requestFactory;
    }


    @Override
    public void authorize(HttpHeaders entity) {

        TokenResponse token = getToken();
        entity.set(HttpHeaders.AUTHORIZATION, String.format(TOKEN_HEADER_FORMAT, StringUtils.capitalize(token.getTokenType()), token.getAccessToken()));
        entity.set("mds-client-id", UUID.randomUUID().toString());
    }


    private TokenResponse getToken() {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        addParam("grant_type", grantType, form);
        addParam("username", username, form);
        addParam("password", password, form);
        addParam("client_id", clientId, form);
        addParam("client_secret", clientSecret, form);
        addParam("scope", scope, form);

        return getToken(form);
    }

    private void addParam(String paramName, String paramValue, MultiValueMap<String, Object> form) {
        Optional.ofNullable(paramValue).filter(StringUtils::isNotBlank).ifPresent(v -> form.add(paramName, v));
    }

    private TokenResponse getToken(MultiValueMap<String, Object> form) {
        Objects.requireNonNull(form);
        TokenResponse token = newToken(form);
        return token;
    }


    /**
     * Calculates if token is expired
     *
     * @param token
     * @return
     */
    private boolean expired(TokenResponse token) {
        return token == null || (token.getExpiresAt() - System.currentTimeMillis() <= 0);
    }


    public TokenResponse newToken(MultiValueMap<String, Object> form) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, httpHeaders);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenIssuerUrl, requestEntity, TokenResponse.class);

        TokenResponse token = Optional.ofNullable(response).map(HttpEntity::getBody).orElseThrow(() -> new RuntimeException("No response"));

        return token;
    }


}
