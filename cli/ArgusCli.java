package com.argus.cli;

import com.argus.detection.*;
import com.argus.model.Event;
import com.argus.service.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/** CLI entry point for ARGUS. */
public final class ArgusCli {
    private final EventValidator validator = new EventValidator();
    private final EventStore store = new EventStore(Path.of("data", "events.csv"));
    private final EventProcessor processor = new EventProcessor(List.of(new FailedLoginRule(), new HighSeverityRule()));
    private final InvestigationService investigations = new InvestigationService();

    public static void main(String[] args) { new ArgusCli().run(args); }
    private void run(String[] args) {
        if (args.length > 0) { execute(String.join(" ", args), new Scanner(System.in)); return; }
        System.out.println("ARGUS v1.0 — type help for commands.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) { System.out.print("argus> "); if (!scanner.hasNextLine()) break; if (!execute(scanner.nextLine(), scanner)) break; }
        }
    }
    private boolean execute(String input, Scanner scanner) {
        String[] parts = input.trim().split("\\s+", 2); String command = parts[0].toLowerCase();
        try {
            switch (command) {
                case "help" -> help(); case "seed" -> seed(); case "list" -> list(); case "analyze" -> analyze(); case "summary" -> summary();
                case "investigate" -> investigate(parts.length == 2 ? parts[1] : ""); case "ingest" -> ingest(scanner);
                case "exit", "quit" -> { return false; }
                case "" -> { } default -> System.out.println("Unknown command. Type help.");
            }
        } catch (IOException | IllegalArgumentException ex) { System.out.println("Error: " + ex.getMessage()); }
        return true;
    }
    private void seed() throws IOException {
        if (!store.loadAll().isEmpty()) { System.out.println("Events already exist; sample data was not added."); return; }
        List<Event> samples = List.of(
                event("e-001", "2026-09-15T08:00:00Z", "alice", "LOGIN_FAILURE", "MEDIUM", "vpn", "Invalid password"),
                event("e-002", "2026-09-15T08:05:00Z", "alice", "LOGIN_FAILURE", "MEDIUM", "vpn", "Invalid password"),
                event("e-003", "2026-09-15T08:10:00Z", "alice", "LOGIN_FAILURE", "MEDIUM", "vpn", "Invalid password"),
                event("e-004", "2026-09-15T09:00:00Z", "bob", "CONFIG_CHANGE", "CRITICAL", "admin-console", "Firewall policy changed"),
                event("e-005", "2026-09-15T09:15:00Z", "alice", "LOGIN_SUCCESS", "LOW", "vpn", "Successful login"));
        for (Event event : samples) store.save(event);
        System.out.println("Created 5 sample events.");
    }
    private Event event(String id, String time, String user, String type, String severity, String source, String message) { return new Event(id, Instant.parse(time), user, type, severity, source, message); }
    private List<Event> events() throws IOException { return store.loadAll(); }
    private List<DetectionResult> findings() throws IOException { return processor.analyze(events()); }
    private void list() throws IOException { List<Event> es = events(); if (es.isEmpty()) System.out.println("No events stored."); else es.forEach(e -> System.out.printf("%s | %s | %s | %s | %s%n", e.timestamp(), e.user(), e.type(), e.severity(), e.message())); }
    private void analyze() throws IOException { List<DetectionResult> rs = findings(); if (rs.isEmpty()) System.out.println("No anomalies found."); else rs.forEach(r -> System.out.printf("[%s] user=%s risk=+%d: %s (%s)%n", r.ruleName(), r.user(), r.riskPoints(), r.description(), String.join(", ", r.eventIds()))); }
    private void summary() throws IOException { System.out.printf("Events: %d | Findings: %d%n", events().size(), findings().size()); }
    private void investigate(String user) throws IOException {
        if (user.isBlank()) throw new IllegalArgumentException("Usage: investigate <user>");
        List<Event> timeline = investigations.timeline(user, events());
        if (timeline.isEmpty()) { System.out.println("No events for " + user + "."); return; }
        System.out.println("Timeline for " + user + ":"); timeline.forEach(e -> System.out.printf("  %s | %s | %s | %s%n", e.timestamp(), e.type(), e.severity(), e.message()));
        System.out.println("Risk score: " + investigations.riskScore(user, findings()));
    }
    private void ingest(Scanner scanner) throws IOException {
        System.out.print("User: "); String user = scanner.nextLine().trim(); System.out.print("Type: "); String type = scanner.nextLine().trim().toUpperCase();
        System.out.print("Severity: "); String severity = scanner.nextLine().trim().toUpperCase(); System.out.print("Source: "); String source = scanner.nextLine().trim(); System.out.print("Message: "); String message = scanner.nextLine().trim();
        Event event = new Event(UUID.randomUUID().toString(), Instant.now(), user, type, severity, source, message); validator.validate(event); store.save(event); System.out.println("Event saved.");
    }
    private void help() { System.out.println("Commands: seed, list, analyze, summary, investigate <user>, ingest, help, exit"); }
}
