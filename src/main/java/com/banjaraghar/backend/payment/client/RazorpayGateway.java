package com.banjaraghar.backend.payment.client;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.PaymentGatewayException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
@Slf4j
public class RazorpayGateway {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final AppProperties appProperties;

    private RazorpayClient client;

    public String createOrder(long amountPaise, String receipt) {
        try {
            JSONObject request = new JSONObject();
            request.put("amount", amountPaise);
            request.put("currency", "INR");
            request.put("receipt", receipt);

            Order order = client().orders.create(request);
            return order.get("id");
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for receipt {}", receipt, e);
        throw new PaymentGatewayException("Payment service is unavailable. Please try again", e);
        }
    }

    public String refund(String paymentId, long amountPaise) {
        try {
            JSONObject request = new JSONObject();
            request.put("amount", amountPaise);

            Refund refund = client().payments.refund(paymentId, request);
            return refund.get("id");
        } catch (RazorpayException e) {
            log.error("Razorpay refund failed for payment {}", paymentId, e);
            throw new PaymentGatewayException("Refund could not be started. Please try again", e);
        }
    }

    public boolean isValidPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        return matches(payload.getBytes(StandardCharsets.UTF_8),
                appProperties.getRazorpay().getKeySecret(), signature);
    }

    public boolean isValidWebhookSignature(byte[] rawBody, String signature) {
        return matches(rawBody, appProperties.getRazorpay().getWebhookSecret(), signature);
    }

    private boolean matches(byte[] data, String secret, String signature) {
        if (secret == null || secret.isBlank() || signature == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            String expected = HexFormat.of().formatHex(mac.doFinal(data));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signature.trim().getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not compute the HMAC signature", e);
        }
    }

    private synchronized RazorpayClient client() throws RazorpayException {
        if (client == null) {
            AppProperties.Razorpay config = appProperties.getRazorpay();
            if (config.getKeyId() == null || config.getKeyId().isBlank()
            || config.getKeySecret() == null || config.getKeySecret().isBlank()) {
                throw new RazorpayException("Razorpay keys are not configured");
            }
            client = new RazorpayClient(config.getKeyId(), config.getKeySecret());
        }
        return client;
    }
}
