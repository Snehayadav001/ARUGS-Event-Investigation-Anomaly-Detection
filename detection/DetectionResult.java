package com.argus.detection;

import java.util.List;

/** A human-readable anomaly finding produced by a detection rule. */
public record DetectionResult(String ruleName, String user, int riskPoints,
                              String description, List<String> eventIds) { }
