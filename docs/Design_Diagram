# ARGUS Design Diagrams

This document contains the design artefacts required for the ARGUS v1.0 submission. GitHub renders the Mermaid diagrams directly.

## Use-case diagram

~~~mermaid
flowchart LR
    A[Analyst / Administrator]
    A --> I[Ingest event]
    A --> L[List stored events]
    A --> AN[Analyze anomalies]
    A --> IV[Investigate user]
    A --> SU[View summary]
    I --> V[Validate event]
    V --> S[(CSV event storage)]
    AN --> R[Run detection rules concurrently]
    R --> F[Display findings]
    IV --> T[Show timeline and risk score]
~~~

## Workflow diagram

~~~mermaid
flowchart TD
    Start([Start ARGUS]) --> Command{CLI command}
    Command -->|ingest| Input[Read event fields]
    Input --> Valid{Valid event?}
    Valid -->|No| Error[Show validation message]
    Error --> Command
    Valid -->|Yes| Save[Append event to CSV]
    Save --> Command
    Command -->|analyze| Load[Load all events]
    Command -->|investigate user| Load
    Load --> Parallel[Submit each rule as a Runnable task]
    Parallel --> Login[Failed-login rule]
    Parallel --> Severity[High-severity rule]
    Login --> Findings[Combine findings]
    Severity --> Findings
    Findings --> Display[Display findings or timeline/risk score]
    Display --> Command
    Command -->|exit| End([Exit])
~~~

## Component / class diagram

~~~mermaid
classDiagram
    class ArgusCli {
      +main(String[] args)
      -run(String[] args)
      -execute(String input, Scanner scanner)
    }
    class Event {
      +String id
      +Instant timestamp
      +String user
      +String type
      +String severity
      +String source
      +String message
    }
    class EventValidator {
      +validate(Event event)
    }
    class EventStore {
      +save(Event event)
      +loadAll() List~Event~
    }
    class EventProcessor {
      +analyze(List~Event~) List~DetectionResult~
    }
    class DetectionRule {
      <<interface>>
      +name() String
      +evaluate(List~Event~) List~DetectionResult~
    }
    class FailedLoginRule
    class HighSeverityRule
    class DetectionResult
    class InvestigationService {
      +timeline(String, List~Event~) List~Event~
      +riskScore(String, List~DetectionResult~) int
    }
    ArgusCli --> EventValidator
    ArgusCli --> EventStore
    ArgusCli --> EventProcessor
    ArgusCli --> InvestigationService
    EventStore --> Event
    EventProcessor --> DetectionRule
    DetectionRule <|.. FailedLoginRule
    DetectionRule <|.. HighSeverityRule
    EventProcessor --> DetectionResult
    InvestigationService --> Event
    InvestigationService --> DetectionResult
~~~

## Sequence diagram

~~~mermaid
sequenceDiagram
    actor Analyst
    participant CLI as ArgusCli
    participant Store as EventStore
    participant Processor as EventProcessor
    participant Login as FailedLoginRule
    participant Severity as HighSeverityRule
    participant Investigation as InvestigationService

    Analyst->>CLI: investigate alice
    CLI->>Store: loadAll()
    Store-->>CLI: List of events
    CLI->>Processor: analyze(events)
    par Runnable task 1
      Processor->>Login: evaluate(events)
      Login-->>Processor: login findings
    and Runnable task 2
      Processor->>Severity: evaluate(events)
      Severity-->>Processor: severity findings
    end
    Processor-->>CLI: List of findings
    CLI->>Investigation: timeline and riskScore
    Investigation-->>CLI: timeline and score
    CLI-->>Analyst: investigation result
~~~

## Storage design

ARGUS uses a local, pipe-delimited CSV file rather than a database. It is intentionally transparent and appropriate for the small MVP dataset.

~~~mermaid
erDiagram
    EVENT {
      string id PK
      string timestamp
      string user
      string type
      string severity
      string source
      string message
    }
~~~

File: data/events.csv  
Schema: id|timestamp|user|type|severity|source|message

