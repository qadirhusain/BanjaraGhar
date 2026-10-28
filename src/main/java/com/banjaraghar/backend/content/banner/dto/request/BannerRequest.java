package com.banjaraghar.backend.content.banner.dto.request;

import com.banjaraghar.backend.content.banner.enums.BannerPlacement;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class BannerRequest {

    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @Size(max = 250, message = "Subtitle must be at most 250 characters")
    private String subtitle;

    @Size(max = 40, message = "Button text must be at most 40 characters")
    private String buttonText;

    @Size(max = 500, message = "Link must be at most 500 characters")
    private String linkUrl;

    // Upload the picture first with POST /api/admin/banners/image, then paste the url it returns here
    @NotBlank(message = "Image is required")
    @Size(max = 500, message = "Image url must be at most 500 characters")
    @Pattern(regexp = "^(/uploads/|https://).+", message = "Image must be an uploaded image or an https link")
    private String imageUrl;

    @Size(max = 500, message = "Mobile image url must be at most 500 characters")
    @Pattern(regexp = "^$|^(/uploads/|https://).+", message = "Mobile image must be an uploaded image or an https link")
    private String mobileImageUrl;

    @NotNull(message = "Placement is required")
    private BannerPlacement placement;

    private UUID categoryId;              // required for CATEGORY banners, must be empty for HOME banners

    @PositiveOrZero(message = "Sort order cannot be negative")
    private Integer sortOrder;            // smaller numbers come first

    private Instant startsAt;             // empty means shown immediately

    private Instant endsAt;               // empty means shown until switched off
}