package com.banjaraghar.backend.newsletter.service;

import com.banjaraghar.backend.common.response.MessageResponse;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.newsletter.dto.request.SubscribeRequest;
import com.banjaraghar.backend.newsletter.dto.response.SubscriberResponse;
import com.banjaraghar.backend.newsletter.entity.NewsletterSubscriber;
import com.banjaraghar.backend.newsletter.repo.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NewsletterSubscriberRepository subscriberRepository;

    // Not @Transactional on purpose: a duplicate from a double click must not poison a transaction
    public MessageResponse subscribe(SubscribeRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        NewsletterSubscriber existing = subscriberRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existing == null) {
            NewsletterSubscriber subscriber = new NewsletterSubscriber();
            subscriber.setEmail(email);
            try {
                subscriberRepository.save(subscriber);
            } catch (DataIntegrityViolationException e) {
                // Subscribed a moment ago from another request; nothing to do
            }
        } else if (!existing.isActive()) {
            existing.setActive(true);
            subscriberRepository.save(existing);
        }

        // Same answer whether the address was already on the list
        return new MessageResponse("Thanks for subscribing to Banjara Ghar");
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriberResponse> list(Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "subscribedAt"));
        Page<NewsletterSubscriber> subscribers = active == null
                ? subscriberRepository.findAll(pageable)
                : subscriberRepository.findByActive(active, pageable);
        return PageResponse.of(subscribers, subscribers.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public SubscriberResponse setActive(UUID id, boolean active) {
        NewsletterSubscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found"));
        subscriber.setActive(active);
        return toResponse(subscriberRepository.save(subscriber));
    }

    private SubscriberResponse toResponse(NewsletterSubscriber subscriber) {
        return new SubscriberResponse(subscriber.getId(), subscriber.getEmail(), subscriber.isActive(),
                subscriber.getSubscribedAt());
    }
}
