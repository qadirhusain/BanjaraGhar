package com.banjaraghar.backend.shipping.dto.response;

import com.banjaraghar.backend.shipping.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReturnResponse {

    private UUID id;
    private ReturnStatus status;
    private String reason;
    private String adminNote;
    private Long refundAmount;          // paise, once approved
    private List<String> photoUrls;
    private Instant createdAt;
    private Instant decidedAt;
}