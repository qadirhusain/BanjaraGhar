package com.banjaraghar.backend.shipping.client;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.ShippingGatewayException;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ShiprocketGateway {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Kolkata"));
    private static final Duration TOKEN_LIFETIME = Duration.ofDays(9);

    private final AppProperties.Shiprocket config;
    private final RestClient restClient;

    private String token;
    private Instant tokenExpiresAt = Instant.EPOCH;

    @Getter
    @AllArgsConstructor
    public static class SrOrder {
        private long orderId;
        private long shipmentId;
    }

    @Getter
    @AllArgsConstructor
    public static class SrAwb {
        private String awbCode;
        private String courierName;
    }

    public ShiprocketGateway(AppProperties appProperties) {
        this.config = appProperties.getShiprocket();

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    // ---------------------------------------------------------------- shipments

    public SrOrder createOrder(Order order, Parcel parcel) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order_id", order.getOrderNumber());
        body.put("order_date", ORDER_DATE.format(order.getCreatedAt()));
        body.put("pickup_location", config.getPickupLocation());
        body.put("billing_customer_name", order.getShipName());
        body.put("billing_address", order.getShipLine1());
        if (order.getShipLine2() != null && !order.getShipLine2().isBlank()) {
            body.put("billing_address_2", order.getShipLine2());
        }
        body.put("billing_city", order.getShipCity());
        body.put("billing_pincode", order.getShipPincode());
        body.put("billing_state", order.getShipState());
        body.put("billing_country", "India");
        body.put("billing_email", order.getUser().getEmail());
        body.put("billing_phone", phone(order.getShipPhone()));
        body.put("shipping_is_billing", true);
        body.put("order_items", items(order));
        body.put("payment_method", "Prepaid");
        body.put("shipping_charges", rupees(order.getShippingFee()));
        body.put("total_discount", rupees(order.getDiscountAmount()));
        body.put("sub_total", rupees(order.getTotalAmount() - order.getShippingFee()));
        addParcel(body, parcel);

        return readOrder(post("/v1/external/orders/create/adhoc", body), "Shiprocket did not create the order");
    }

    public SrAwb assignAwb(long shipmentId) {
        Map<String, Object> response = post("/v1/external/courier/assign/awb", Map.of("shipment_id", shipmentId));

        Map<String, Object> data = child(child(response, "response"), "data");
        Object awb = data == null ? null : data.get("awb_code");
        if (awb == null || awb.toString().isBlank()) {
            throw new ShippingGatewayException("Shiprocket could not assign a courier: " + shorten(response));
        }
        Object courier = data.get("courier_name");
        return new SrAwb(awb.toString(), courier == null ? null : courier.toString());
    }

    public void requestPickup(long shipmentId) {
        Map<String, Object> response = post("/v1/external/courier/generate/pickup",
                Map.of("shipment_id", List.of(shipmentId)));
        if (!(response.get("pickup_status") instanceof Number status) || status.intValue() != 1) {
            throw new ShippingGatewayException("Shiprocket could not schedule the pickup: " + shorten(response));
        }
    }

    /**
     * Cancels a shipment that has an AWB. Shiprocket only allows this before the courier is out for pickup.
     */
    public void cancelShipment(String awb) {
        post("/v1/external/orders/cancel/shipment/awbs", Map.of("awbs", List.of(awb)));
    }

    public void cancelOrder(long srOrderId) {
        post("/v1/external/orders/cancel", Map.of("ids", List.of(srOrderId)));
    }

    /**
     * Creates the reverse shipment: the customer is the pickup point and the seller is the destination.
     */
    public SrOrder createReturn(Order order, Parcel parcel) {
        AppProperties.Seller seller = config.getSeller();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order_id", "R-" + order.getOrderNumber());
        body.put("order_date", ORDER_DATE.format(java.time.Instant.now()));
        body.put("pickup_customer_name", order.getShipName());
        body.put("pickup_address", order.getShipLine1());
        if (order.getShipLine2() != null && !order.getShipLine2().isBlank()) {
            body.put("pickup_address_2", order.getShipLine2());
        }
        body.put("pickup_city", order.getShipCity());
        body.put("pickup_state", order.getShipState());
        body.put("pickup_country", "India");
        body.put("pickup_pincode", order.getShipPincode());
        body.put("pickup_email", order.getUser().getEmail());
        body.put("pickup_phone", phone(order.getShipPhone()));
        body.put("shipping_customer_name", seller.getName());
        body.put("shipping_address", seller.getAddress());
        body.put("shipping_city", seller.getCity());
        body.put("shipping_country", "India");
        body.put("shipping_pincode", seller.getPincode());
        body.put("shipping_state", seller.getState());
        body.put("shipping_phone", phone(seller.getPhone()));
        body.put("order_items", items(order));
        body.put("payment_method", "Prepaid");
        body.put("sub_total", rupees(order.getTotalAmount() - order.getShippingFee()));
        addParcel(body, parcel);

        return readOrder(post("/v1/external/orders/create/return", body), "Shiprocket did not create the return");
    }

    // ---------------------------------------------------------------- HTTP

    private Map<String, Object> post(String path, Object body) {
        if (!config.isEnabled()) {
            throw new ShippingGatewayException("The Shiprocket integration is turned off");
        }
        for (int attempt = 0; ; attempt++) {
            try {
                Map<String, Object> response = restClient.post().uri(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(MAP_TYPE);
                return response == null ? Map.of() : response;
            } catch (HttpClientErrorException.Unauthorized e) {
                if (attempt > 0) {
                    throw failure(path, e);
                }
                clearToken();   // the token expired early: log in again and retry once
            } catch (RestClientResponseException e) {
                throw failure(path, e);
            } catch (RestClientException e) {
                log.warn("Shiprocket {} unreachable: {}", path, e.getClass().getSimpleName());
                throw new ShippingGatewayException("Could not reach Shiprocket", e);
            }
        }
    }

    private synchronized String token() {
        if (token != null && Instant.now().isBefore(tokenExpiresAt)) {
            return token;
        }

        try {
            Map<String, Object> response = restClient.post().uri("/v1/external/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", String.valueOf(config.getEmail()), "password", String.valueOf(config.getPassword())))
                    .retrieve()
                    .body(MAP_TYPE);
            Object value = response == null ? null : response.get("token");
            if (value == null || value.toString().isBlank()) {
                throw new ShippingGatewayException("Shiprocket login did not return a token");
            }
            token = value.toString();
            tokenExpiresAt = Instant.now().plus(TOKEN_LIFETIME);
            return token;
        } catch (RestClientResponseException e) {
            // The body is not logged on purpose: it can echo the login details
            log.warn("Shiprocket login failed with HTTP {}", e.getStatusCode().value());
            throw new ShippingGatewayException("Shiprocket login failed (HTTP " + e.getStatusCode().value() + ")", e);
        } catch (RestClientException e) {
            throw new ShippingGatewayException("Could not reach Shiprocket", e);
        }
    }

    private synchronized void clearToken() {
        token = null;
        tokenExpiresAt = Instant.EPOCH;
    }

    private ShippingGatewayException failure(String path, RestClientResponseException e) {
        String message = "Shiprocket rejected " + path + " (HTTP " + e.getStatusCode().value() + "): "
                + truncate(e.getResponseBodyAsString());
        log.warn(message);
        return new ShippingGatewayException(message, e);
    }

    // ---------------------------------------------------------------- helpers

    private SrOrder readOrder(Map<String, Object> response, String errorMessage) {
        if (response.get("order_id") instanceof Number orderId && response.get("shipment_id") instanceof Number shipmentId) {
            return new SrOrder(orderId.longValue(), shipmentId.longValue());
        }
        throw new ShippingGatewayException(errorMessage + ": " + shorten(response));
    }

    private List<Map<String, Object>> items(Order order) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("name", item.getProductName());
            line.put("sku", item.getSku());
            line.put("units", item.getQuantity());
            line.put("selling_price", rupees(item.getUnitPrice()));
            items.add(line);
        }
        return items;
    }

    private void addParcel(Map<String, Object> body, Parcel parcel) {
        body.put("length", parcel.getLengthCm());
        body.put("breadth", parcel.getBreadthCm());
        body.put("height", parcel.getHeightCm());
        body.put("weight", parcel.getWeightKg());
    }

    private static long rupees(long paise) {
        return Math.round(paise / 100.0);
    }

    /** Keeps the last 10 digits, because Shiprocket wants a plain 10-digit number. */
    private static String phone(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        return digits.length() > 10 ? digits.substring(digits.length() - 10) : digits;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> child(Map<String, Object> map, String key) {
        return map != null && map.get(key) instanceof Map<?, ?> value ? (Map<String, Object>) value : null;
    }

    private static String shorten(Map<String, Object> response) {
        return truncate(String.valueOf(response));
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }

}
