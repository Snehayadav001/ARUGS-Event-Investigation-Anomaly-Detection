package com.argus.service;

import com.argus.detection.DetectionResult;
import com.argus.detection.DetectionRule;
import com.argus.model.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Runs independent rules concurrently using one Runnable task per rule. */
public final class EventProcessor {
    private final List<DetectionRule> rules;
    public EventProcessor(List<DetectionRule> rules) { this.rules = List.copyOf(rules); }

    public List<DetectionResult> analyze(List<Event> events) {
        List<Event> safeEvents = List.copyOf(events);
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(2, Math.max(1, rules.size())));
        try {
            List<Future<List<DetectionResult>>> futures = new ArrayList<>();
            for (DetectionRule rule : rules) {
                RunnableRuleTask task = new RunnableRuleTask(rule, safeEvents);
                // The submitted callable runs the explicit Runnable task, then returns its result.
                futures.add(executor.submit(() -> { task.run(); return task.results(); }));
            }
            List<DetectionResult> all = new ArrayList<>();
            for (Future<List<DetectionResult>> future : futures) all.addAll(future.get());
            return all;
        } catch (Exception ex) {
            throw new IllegalStateException("Analysis could not complete: " + ex.getMessage(), ex);
        } finally { executor.shutdown(); }
    }
    private static final class RunnableRuleTask implements Runnable {
        private final DetectionRule rule; private final List<Event> events; private List<DetectionResult> results = List.of();
        private RunnableRuleTask(DetectionRule rule, List<Event> events) { this.rule = rule; this.events = events; }
        @Override public void run() { results = rule.evaluate(events); }
        private List<DetectionResult> results() { return results; }
    }
}
