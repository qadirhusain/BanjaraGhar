package com.banjaraghar.backend.activitylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ActivityLogResponse {

    private UUID id;
    private UUID adminId;
    private String adminName;
    private String adminEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private Instant createdAt;
}