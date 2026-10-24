package com.banjaraghar.backend.catalog.category.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private int sortOrder;
    private boolean active;
}
