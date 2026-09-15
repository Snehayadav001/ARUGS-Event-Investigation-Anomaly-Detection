package com.argus.model;

import java.time.Instant;
import java.util.Objects;

/** Immutable security event stored by ARGUS. */
public record Event(String id, Instant timestamp, String user, String type,
                    String severity, String source, String message) {
    public Event {
        Objects.requireNonNull(id);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(user);
        Objects.requireNonNull(type);
        Objects.requireNonNull(severity);
        Objects.requireNonNull(source);
        Objects.requireNonNull(message);
    }
}
