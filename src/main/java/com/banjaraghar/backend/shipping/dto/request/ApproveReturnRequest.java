package com.banjaraghar.backend.shipping.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveReturnRequest {

    @NotNull(message = "Refund amount is required")
    @Min(value = 100, message = "Refund must be at least 100 paise (Rs 1)")
    private Long refundAmount;          // paise

    @Size(max = 500, message = "Note is too long")
    private String note;                // optional
}