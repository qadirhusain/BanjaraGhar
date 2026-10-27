package com.banjaraghar.backend.shipping.controller;

import com.banjaraghar.backend.shipping.dto.response.ReturnResponse;
import com.banjaraghar.backend.shipping.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/return")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    // Multipart form: field "reason" (text) and field "photos" (1 to 5 image files)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse request(@AuthenticationPrincipal UUID userId,
                                  @PathVariable UUID orderId,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  @RequestParam(value = "photos", required = false) List<MultipartFile> photos) {
        return returnService.request(userId, orderId, reason, photos);
    }

    @GetMapping
    public ReturnResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID orderId) {
        return returnService.getForCustomer(userId, orderId);
    }
}