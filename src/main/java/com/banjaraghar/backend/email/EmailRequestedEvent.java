package com.banjaraghar.backend.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Published inside a transaction; the mail is only sent once that transaction has committed. */
@Getter
@AllArgsConstructor
public class EmailRequestedEvent {

    private final String to;
    private final String subject;
    private final String body;
}