package com.banjaraghar.backend.content.homesection.dto.response;

import com.banjaraghar.backend.content.homesection.enums.SectionLayout;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminHomeSectionResponse {

    private UUID id;
    private String title;
    private String subtitle;
    private SectionLayout layout;
    private int sortOrder;
    private boolean active;
    private List<UUID> productIds;      // in display order
    private Instant createdAt;
}