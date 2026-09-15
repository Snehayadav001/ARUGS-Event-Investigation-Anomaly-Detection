package com.argus.detection;

import com.argus.model.Event;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Detects a probable brute-force attempt: 3 failures in a rolling 15-minute window. */
public final class FailedLoginRule implements DetectionRule {
    private static final Duration WINDOW = Duration.ofMinutes(15);
    @Override public String name() { return "Repeated failed login"; }

    @Override public List<DetectionResult> evaluate(List<Event> events) {
        Map<String, List<Event>> byUser = new HashMap<>();
        for (Event event : events) {
            if (event.type().equals("LOGIN_FAILURE")) {
                byUser.computeIfAbsent(event.user(), ignored -> new ArrayList<>()).add(event);
            }
        }
        List<DetectionResult> results = new ArrayList<>();
        for (Map.Entry<String, List<Event>> entry : byUser.entrySet()) {
            List<Event> failures = entry.getValue();
            failures.sort(Comparator.comparing(Event::timestamp));
            for (int start = 0; start + 2 < failures.size(); start++) {
                Instant end = failures.get(start + 2).timestamp();
                if (!Duration.between(failures.get(start).timestamp(), end).minus(WINDOW).isPositive()) {
                    List<String> ids = failures.subList(start, start + 3).stream().map(Event::id).toList();
                    results.add(new DetectionResult(name(), entry.getKey(), 50,
                            "3 login failures within 15 minutes", ids));
                    break; // one concise finding per user
                }
            }
        }
        return results;
    }
}
