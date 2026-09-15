package com.argus;

import com.argus.detection.*;
import com.argus.model.Event;
import com.argus.service.EventValidator;
import com.argus.service.InvestigationService;
import java.time.Instant;
import java.util.List;

/** Lightweight dependency-free checks for the ARGUS MVP. */
public final class ArgusMvpTest {
    public static void main(String[] args) {
        List<Event> events = List.of(
                e("1", "2026-09-15T08:00:00Z", "alice", "LOGIN_FAILURE", "MEDIUM"), e("2", "2026-09-15T08:05:00Z", "alice", "LOGIN_FAILURE", "MEDIUM"),
                e("3", "2026-09-15T08:10:00Z", "alice", "LOGIN_FAILURE", "MEDIUM"), e("4", "2026-09-15T09:00:00Z", "bob", "SYSTEM_ALERT", "CRITICAL"));
        List<DetectionResult> results = new com.argus.service.EventProcessor(List.of(new FailedLoginRule(), new HighSeverityRule())).analyze(events);
        check(results.size() == 2, "expected failed-login and critical findings");
        check(new InvestigationService().riskScore("alice", results) == 50, "alice risk should be 50");
        try { new EventValidator().validate(e("bad", "2026-09-15T00:00:00Z", "eve", "WRONG", "LOW")); throw new AssertionError("invalid type accepted"); }
        catch (IllegalArgumentException expected) { /* expected */ }
        System.out.println("ARGUS MVP tests passed.");
    }
    private static Event e(String id, String time, String user, String type, String severity) { return new Event(id, Instant.parse(time), user, type, severity, "test", "test event"); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
