package com.argus.service;

import com.argus.model.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Pipe-delimited local storage, chosen to keep the MVP inspectable without a database. */
public final class EventStore {
    private final Path path;
    public EventStore(Path path) { this.path = path; }

    public void save(Event event) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, toLine(event) + System.lineSeparator(),
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }
    public List<Event> loadAll() throws IOException {
        if (!Files.exists(path)) return List.of();
        List<Event> events = new ArrayList<>(); int lineNumber = 0;
        for (String line : Files.readAllLines(path)) {
            lineNumber++;
            if (line.isBlank()) continue;
            try { events.add(fromLine(line)); }
            catch (RuntimeException ex) { throw new IOException("Invalid event data at line " + lineNumber + ": " + ex.getMessage(), ex); }
        }
        return events;
    }
    private String toLine(Event e) { return String.join("|", e.id(), e.timestamp().toString(), e.user(), e.type(), e.severity(), e.source(), e.message()); }
    private Event fromLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 7) throw new IllegalArgumentException("expected 7 fields");
        return new Event(p[0], Instant.parse(p[1]), p[2], p[3], p[4], p[5], p[6]);
    }
}
