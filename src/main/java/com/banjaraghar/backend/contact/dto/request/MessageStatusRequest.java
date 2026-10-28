package com.banjaraghar.backend.contact.dto.request;

import com.banjaraghar.backend.contact.enums.MessageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
public class MessageStatusRequest {

    @NotNull(message = "Status is required")
    private MessageStatus status;
}