package com.argus.service;

import com.argus.model.Event;
import java.util.Set;

/** Central validation keeps malformed input out of the data file. */
public final class EventValidator {
    private static final Set<String> TYPES = Set.of("LOGIN_SUCCESS", "LOGIN_FAILURE", "FILE_ACCESS", "CONFIG_CHANGE", "SYSTEM_ALERT");
    private static final Set<String> SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    public void validate(Event event) {
        required(event.id(), "id"); required(event.user(), "user");
        required(event.source(), "source"); required(event.message(), "message");
        if (!TYPES.contains(event.type())) throw new IllegalArgumentException("Unsupported type: " + event.type());
        if (!SEVERITIES.contains(event.severity())) throw new IllegalArgumentException("Unsupported severity: " + event.severity());
        if (hasDelimiter(event)) throw new IllegalArgumentException("Fields cannot contain the | character.");
    }
    private void required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required.");
    }
    private boolean hasDelimiter(Event event) {
        return event.id().contains("|") || event.user().contains("|") || event.source().contains("|") || event.message().contains("|");
    }
}
