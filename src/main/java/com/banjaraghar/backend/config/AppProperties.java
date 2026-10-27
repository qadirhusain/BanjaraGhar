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
    private Storage storage = new Storage();
    private Shipping shipping = new Shipping();
    private Shiprocket shiprocket = new Shiprocket();
    private Returns returns = new Returns();

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

    @Getter
    @Setter
    public static class Storage {
        private String uploadDir = "uploads";
    }

    @Getter
    @Setter
    public static class Shipping {
        private long flatFee = 8900;
        private long freeAbove = 9900;
    }

    @Getter
    @Setter
    public static class Shiprocket {
        private boolean enabled = false;
        private String baseUrl = "https://api-sandbox.shiprocket.in";
        private String email;
        private String password;
        private String pickupLocation = "Primary";
        private String webhookToken;
        private int maxAttempts = 8;
        private Seller seller = new Seller();
    }

    @Getter
    @Setter
    public static class Seller {
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
        private String email;
    }

    @Getter
    @Setter
    public static class Returns {
        private int windowDays = 7;
    }
}
