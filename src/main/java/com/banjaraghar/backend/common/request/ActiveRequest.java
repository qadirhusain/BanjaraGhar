package com.banjaraghar.backend.common.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
public class ActiveRequest {

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
