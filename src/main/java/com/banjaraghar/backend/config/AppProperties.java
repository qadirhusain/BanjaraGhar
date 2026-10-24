package com.banjaraghar.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private List<String> adminEmails = new ArrayList<>();
    private Razorpay razorpay = new Razorpay();
    private Google google = new Google();
    private Facebook facebook = new Facebook();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenMinutes;
        private long refreshTokenDays;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Razorpay {
        private String keyId;
        private String keySecret;
        private String webhookSecret;
    }

    @Getter
    @Setter
    public static class Google {
        private List<String> clientIds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Facebook {
        private String appId;
        private String appSecret;
    }
}
