package com.banjaraghar.backend.contact.dto.response;

import com.banjaraghar.backend.contact.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ContactMessageResponse {

    private UUID id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private MessageStatus status;
    private Instant createdAt;
}