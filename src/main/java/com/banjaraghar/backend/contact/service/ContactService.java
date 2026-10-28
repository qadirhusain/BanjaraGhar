package com.banjaraghar.backend.contact.service;

import com.banjaraghar.backend.common.response.MessageResponse;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.contact.dto.request.ContactRequest;
import com.banjaraghar.backend.contact.dto.response.ContactMessageResponse;
import com.banjaraghar.backend.contact.entity.ContactMessage;
import com.banjaraghar.backend.contact.enums.MessageStatus;
import com.banjaraghar.backend.contact.repo.ContactMessageRepository;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ContactMessageRepository contactMessageRepository;

    @Transactional
    public MessageResponse submit(ContactRequest request) {
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setName(request.getName().trim());
        contactMessage.setEmail(request.getEmail().trim());
        contactMessage.setSubject(request.getSubject().trim());
        contactMessage.setMessage(request.getMessage().trim());
        contactMessageRepository.save(contactMessage);
        return new MessageResponse("Thanks for reaching out. We will get back to you soon");
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactMessageResponse> list(MessageStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ContactMessage> messages = status == null
                ? contactMessageRepository.findAll(pageable)
                : contactMessageRepository.findByStatus(status, pageable);
        return PageResponse.of(messages, messages.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public ContactMessageResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public ContactMessageResponse setStatus(UUID id, MessageStatus status) {
        ContactMessage contactMessage = find(id);
        contactMessage.setStatus(status);
        return toResponse(contactMessageRepository.save(contactMessage));
    }

    private ContactMessage find(UUID id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    private ContactMessageResponse toResponse(ContactMessage message) {
        return new ContactMessageResponse(message.getId(), message.getName(), message.getEmail(),
                message.getSubject(), message.getMessage(), message.getStatus(), message.getCreatedAt());
    }
}
