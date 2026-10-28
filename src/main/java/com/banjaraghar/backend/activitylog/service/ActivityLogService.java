package com.banjaraghar.backend.activitylog.service;

import com.banjaraghar.backend.activitylog.dto.response.ActivityLogResponse;
import com.banjaraghar.backend.activitylog.entity.ActivityLog;
import com.banjaraghar.backend.activitylog.repo.ActivityLogRepository;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String ADMIN_PREFIX = "/api/admin/";
    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /** Called by the interceptor after a successful activitylog change. */
    @Transactional
    public void record(UUID adminId, String method, String uri) {
        Matcher matcher = UUID_PATTERN.matcher(uri);
        UUID entityId = matcher.find() ? UUID.fromString(matcher.group()) : null;

        // Ids are replaced so every call to the same endpoint reads the same: POST /api/admin/returns/{id}/approve
        String path = UUID_PATTERN.matcher(uri).replaceAll("{id}");

        String rest = path.startsWith(ADMIN_PREFIX) ? path.substring(ADMIN_PREFIX.length()) : path;
        int slash = rest.indexOf('/');
        String entityType = slash < 0 ? rest : rest.substring(0, slash);

        ActivityLog entry = new ActivityLog();
        entry.setAdmin(userRepository.getReferenceById(adminId));
        entry.setAction(truncate(method + " " + path, 200));
        entry.setEntityType(truncate(entityType, 50));
        entry.setEntityId(entityId);
        activityLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> list(String entityType, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> entries = entityType == null || entityType.isBlank()
                ? activityLogRepository.findAll(pageable)
                : activityLogRepository.findByEntityType(entityType.trim(), pageable);
        return PageResponse.of(entries, entries.getContent().stream().map(this::toResponse).toList());
    }

    private ActivityLogResponse toResponse(ActivityLog entry) {
        User admin = entry.getAdmin();
        return new ActivityLogResponse(entry.getId(), admin.getId(), admin.getFullName(), admin.getEmail(),
                entry.getAction(), entry.getEntityType(), entry.getEntityId(), entry.getCreatedAt());
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
