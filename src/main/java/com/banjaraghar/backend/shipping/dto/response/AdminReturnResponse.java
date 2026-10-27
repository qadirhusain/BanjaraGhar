package com.banjaraghar.backend.shipping.dto.response;

import com.banjaraghar.backend.shipping.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminReturnResponse {

    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private long orderTotal;            // paise
    private String customerName;
    private String customerEmail;
    private ReturnStatus status;
    private String reason;
    private String adminNote;
    private Long refundAmount;          // paise
    private String returnAwb;           // null if the return pickup was not booked automatically
    private List<String> photoUrls;
    private Instant createdAt;
    private Instant decidedAt;
}