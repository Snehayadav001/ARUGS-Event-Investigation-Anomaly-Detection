package com.argus.detection;

import com.argus.model.Event;
import java.util.List;

/** Contract for independently executable anomaly rules. */
public interface DetectionRule {
    String name();
    List<DetectionResult> evaluate(List<Event> events);
}
