package com.banjaraghar.backend.shipping.controller;

import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.shipping.dto.request.ApproveReturnRequest;
import com.banjaraghar.backend.shipping.dto.request.RejectReturnRequest;
import com.banjaraghar.backend.shipping.dto.response.AdminReturnResponse;
import com.banjaraghar.backend.shipping.enums.ReturnStatus;
import com.banjaraghar.backend.shipping.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController {

    private final ReturnService returnService;

    @GetMapping
    public PageResponse<AdminReturnResponse> list(@RequestParam(required = false) ReturnStatus status,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return returnService.list(status, page, size);
    }

    @GetMapping("/{id}")
    public AdminReturnResponse get(@PathVariable UUID id) {
        return returnService.get(id);
    }

    @PostMapping("/{id}/approve")
    public AdminReturnResponse approve(@PathVariable UUID id, @Valid @RequestBody ApproveReturnRequest request) {
        return returnService.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public AdminReturnResponse reject(@PathVariable UUID id, @Valid @RequestBody RejectReturnRequest request) {
        return returnService.reject(id, request);
    }
}