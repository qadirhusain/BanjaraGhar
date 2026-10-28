package com.banjaraghar.backend.activitylog.interceptor;

import com.banjaraghar.backend.activitylog.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

/**
 * Writes one activity log entry for every successful activitylog change, so no activitylog service has to remember to do it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminActivityInterceptor implements HandlerInterceptor {

    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final ActivityLogService activityLogService;

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, @Nullable Exception ex) {
        if (READ_ONLY_METHODS.contains(request.getMethod()) || ex != null || response.getStatus() >= 400) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID adminId)) {
            return;
        }

        try {
            activityLogService.record(adminId, request.getMethod(), request.getRequestURI());
        } catch (Exception e) {
            // A logging problem must never break the activitylog's request
            log.warn("Could not write activity log for {} {}", request.getMethod(), request.getRequestURI(), e);
        }
    }
}