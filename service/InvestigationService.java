package com.argus.service;

import com.argus.detection.DetectionResult;
import com.argus.model.Event;
import java.util.Comparator;
import java.util.List;

/** Builds a small, ordered investigation view for one user. */
public final class InvestigationService {
    public List<Event> timeline(String user, List<Event> events) {
        return events.stream().filter(e -> e.user().equalsIgnoreCase(user))
                .sorted(Comparator.comparing(Event::timestamp)).toList();
    }
    public int riskScore(String user, List<DetectionResult> results) {
        return results.stream().filter(r -> r.user().equalsIgnoreCase(user)).mapToInt(DetectionResult::riskPoints).sum();
    }
}
