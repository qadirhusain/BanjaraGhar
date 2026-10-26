package com.banjaraghar.backend.content.banner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/** What the website and the iOS app need to draw one banner. */
@Getter
@AllArgsConstructor
public class BannerResponse {

    private UUID id;
    private String title;
    private String subtitle;
    private String buttonText;
    private String linkUrl;
    private String imageUrl;
    private String mobileImageUrl;     // null when there is no phone version, so use imageUrl
}