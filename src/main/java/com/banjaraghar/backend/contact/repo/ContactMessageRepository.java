package com.banjaraghar.backend.contact.repo;

import com.banjaraghar.backend.contact.entity.ContactMessage;
import com.banjaraghar.backend.contact.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    Page<ContactMessage> findByStatus(MessageStatus status, Pageable pageable);
}
