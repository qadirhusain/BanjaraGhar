package com.banjaraghar.backend.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AfterCommitEmailListener {

    private final EmailService emailService;

    // fallbackExecution: an event published outside any transaction is sent straight away
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEmailRequested(EmailRequestedEvent event) {
        emailService.send(event.getTo(), event.getSubject(), event.getBody());   // @Async, so it never slows the request
    }
}