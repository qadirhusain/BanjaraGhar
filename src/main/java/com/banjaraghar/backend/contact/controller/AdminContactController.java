package com.banjaraghar.backend.contact.controller;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.contact.dto.request.MessageStatusRequest;
import com.banjaraghar.backend.contact.dto.response.ContactMessageResponse;
import com.banjaraghar.backend.contact.enums.MessageStatus;
import com.banjaraghar.backend.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/messages")
@RequiredArgsConstructor
public class AdminContactController {

    private final ContactService contactService;

    @GetMapping
    public PageResponse<ContactMessageResponse> list(@RequestParam(required = false) MessageStatus status,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return contactService.list(status, page, size);
    }

    @GetMapping("/{id}")
    public ContactMessageResponse get(@PathVariable UUID id) {
        return contactService.get(id);
    }

    @PatchMapping("/{id}/status")
    public ContactMessageResponse setStatus(@PathVariable UUID id, @Valid @RequestBody MessageStatusRequest request) {
        return contactService.setStatus(id, request.getStatus());
    }
}