package com.banjaraghar.backend.newsletter.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SubscriberResponse {

    private UUID id;
    private String email;
    private boolean active;
    private Instant subscribedAt;
}