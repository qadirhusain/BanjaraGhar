package com.banjaraghar.backend.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectReturnRequest {

    @NotBlank(message = "Please give the customer a reason")
    @Size(max = 500, message = "Note is too long")
    private String note;
}