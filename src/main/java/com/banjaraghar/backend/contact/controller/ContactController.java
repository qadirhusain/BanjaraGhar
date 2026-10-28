package com.banjaraghar.backend.contact.controller;

import com.banjaraghar.backend.common.ratelimit.RateLimitService;
import com.banjaraghar.backend.common.response.MessageResponse;
import com.banjaraghar.backend.contact.dto.request.ContactRequest;
import com.banjaraghar.backend.contact.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final RateLimitService rateLimitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse submit(@Valid @RequestBody ContactRequest request, HttpServletRequest http) {
        rateLimitService.check("contact:" + http.getRemoteAddr(), 5, Duration.ofHours(1));
        return contactService.submit(request);
    }
}