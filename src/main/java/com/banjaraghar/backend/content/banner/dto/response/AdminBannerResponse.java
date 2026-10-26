package com.banjaraghar.backend.content.banner.dto.response;

import com.banjaraghar.backend.content.banner.enums.BannerPlacement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminBannerResponse {

    private UUID id;
    private String title;
    private String subtitle;
    private String buttonText;
    private String linkUrl;
    private String imageUrl;
    private String mobileImageUrl;
    private BannerPlacement placement;
    private UUID categoryId;
    private String categoryName;
    private int sortOrder;
    private boolean active;
    private Instant startsAt;
    private Instant endsAt;
    private Instant createdAt;
}