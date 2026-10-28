package com.banjaraghar.backend.newsletter.repo;

import com.banjaraghar.backend.newsletter.entity.NewsletterSubscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, UUID> {

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    Page<NewsletterSubscriber> findByActive(boolean active, Pageable pageable);
}
