package com.banjaraghar.backend.activitylog.controller;

import com.banjaraghar.backend.activitylog.service.ActivityLogService;
import com.banjaraghar.backend.activitylog.dto.response.ActivityLogResponse;
import com.banjaraghar.backend.common.response.PageResponse;
import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activity-log")
@RequiredArgsConstructor
public class AdminActivityLogController {

    private final ActivityLogService activityLogService;

    // entityType is the first part of the activitylog path: products, orders, coupons, returns, banners ...
    @GetMapping
    public PageResponse<ActivityLogResponse> list(@RequestParam(required = false) String entityType,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "50") int size) {
        return activityLogService.list(entityType, page, size);
    }
}
