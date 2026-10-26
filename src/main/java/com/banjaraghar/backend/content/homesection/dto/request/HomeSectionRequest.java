package com.banjaraghar.backend.content.homesection.dto.request;

import com.banjaraghar.backend.content.homesection.enums.SectionLayout;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeSectionRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 250, message = "Subtitle must be at most 250 characters")
    private String subtitle;

    @NotNull(message = "Layout is required")
    private SectionLayout layout;

    @PositiveOrZero(message = "Sort order cannot be negative")
    private Integer sortOrder;        // smaller numbers come first
}