package com.banjaraghar.backend.auth.client;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.UnauthorizedException;
import com.nimbusds.jose.util.JSONObjectUtils;

import java.net.http.HttpClient;
import java.text.ParseException;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class FacebookTokenVerifier {

    private static final String GRAPH_URL = "https://graph.facebook.com/v26.0";

    private final RestClient restClient;
    private final String appId;
    private final String appSecret;

    public FacebookTokenVerifier(AppProperties appProperties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(GRAPH_URL)
                .requestFactory(requestFactory)
                .build();
        this.appId = appProperties.getFacebook().getAppId();
        this.appSecret = appProperties.getFacebook().getAppSecret();
    }

    public SocialProfile verify(String accessToken) {
        try {
            // 1. Ask Facebook whether this token is valid and was issued to OUR app
            Map<String, Object> debug = getJson(
                    "/debug_token?input_token={token}&access_token={appToken}",
                    accessToken, appId + "|" + appSecret);

            if (!(debug.get("data") instanceof Map<?, ?> data)
                    || !Boolean.TRUE.equals(data.get("is_valid"))
                    || !appId.equals(String.valueOf(data.get("app_id")))) {
                throw new UnauthorizedException("Invalid Facebook token");
            }
            String userId = String.valueOf(data.get("user_id"));

            // 2. Fetch the profile
            Map<String, Object> me = getJson("/me?fields=id,name,email&access_token={token}", accessToken);

            if (!userId.equals(String.valueOf(me.get("id")))) {
                throw new UnauthorizedException("Invalid Facebook token");
            }

            Object email = me.get("email");
            if (email == null || email.toString().isBlank()) {
                throw new BadRequestException(
                        "Your Facebook account has no email address. Please sign in another way.");
            }
            Object name = me.get("name");

            return new SocialProfile(userId, email.toString().trim().toLowerCase(),
                    name == null ? null : name.toString());
        } catch (RestClientException e) {
            // Log only the class name: the exception text can contain the request URL, which holds the app secret
            log.warn("Facebook token verification failed: {}", e.getClass().getSimpleName());
            throw new UnauthorizedException("Invalid Facebook token");
        }
    }

    /**
     * Facebook labels its JSON replies as text/javascript, which Spring cannot convert automatically.
     * So the body is read as plain text and parsed here.
     */
    private Map<String, Object> getJson(String uriTemplate, Object... uriVariables) {
        String body = restClient.get().uri(uriTemplate, uriVariables).retrieve().body(String.class);
        try {
            if (body == null || body.isBlank()) {
                throw new UnauthorizedException("Invalid Facebook token");
            }
            return JSONObjectUtils.parse(body);
        } catch (ParseException e) {
            log.warn("Facebook returned an unreadable response");
            throw new UnauthorizedException("Invalid Facebook token");
        }
    }
}