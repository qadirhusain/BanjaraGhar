package com.banjaraghar.backend.newsletter.controller;

import com.banjaraghar.backend.common.ratelimit.RateLimitService;
import com.banjaraghar.backend.common.response.MessageResponse;
import com.banjaraghar.backend.newsletter.dto.request.SubscribeRequest;
import com.banjaraghar.backend.newsletter.service.NewsletterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;
    private final RateLimitService rateLimitService;

    @PostMapping("/subscribe")
    public MessageResponse subscribe(@Valid @RequestBody SubscribeRequest request, HttpServletRequest http) {
        rateLimitService.check("newsletter:" + http.getRemoteAddr(), 10, Duration.ofHours(1));
        return newsletterService.subscribe(request);
    }
}