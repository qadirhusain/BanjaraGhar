package com.banjaraghar.backend.newsletter.controller;

import com.banjaraghar.backend.common.request.ActiveRequest;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.newsletter.dto.response.SubscriberResponse;
import com.banjaraghar.backend.newsletter.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/newsletter/subscribers")
@RequiredArgsConstructor
public class AdminNewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping
    public PageResponse<SubscriberResponse> list(@RequestParam(required = false) Boolean active,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return newsletterService.list(active, page, size);
    }

    @PatchMapping("/{id}/active")
    public SubscriberResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return newsletterService.setActive(id, request.getActive());
    }
}