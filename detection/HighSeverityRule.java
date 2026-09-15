package com.argus.detection;

import com.argus.model.Event;
import java.util.ArrayList;
import java.util.List;

/** Flags each high-impact event so investigators do not overlook it. */
public final class HighSeverityRule implements DetectionRule {
    @Override public String name() { return "High severity event"; }

    @Override public List<DetectionResult> evaluate(List<Event> events) {
        List<DetectionResult> results = new ArrayList<>();
        for (Event event : events) {
            if (event.severity().equals("HIGH") || event.severity().equals("CRITICAL")) {
                int points = event.severity().equals("CRITICAL") ? 50 : 30;
                results.add(new DetectionResult(name(), event.user(), points,
                        event.severity() + " event: " + event.message(), List.of(event.id())));
            }
        }
        return results;
    }
}
