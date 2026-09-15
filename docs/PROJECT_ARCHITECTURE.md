# ARGUS Project Architecture

## Overview

ARGUS uses a layered, package-based architecture. The command-line interface coordinates the application; services manage validation, storage, concurrent processing, and investigations; detection rules are independent and extensible; immutable model records carry data between components.

~~~mermaid
flowchart TD
    User[Analyst / Student] --> CLI[ArgusCli<br/>CLI Layer]

    CLI --> Validator[EventValidator<br/>Validation Service]
    CLI --> Store[EventStore<br/>File Storage Service]
    CLI --> Processor[EventProcessor<br/>Concurrency Service]
    CLI --> Investigation[InvestigationService<br/>Investigation Service]

    Validator --> Event[Event<br/>Immutable Model]
    Store --> Event
    Store <--> CSV[(data/events.csv<br/>Local Event Storage)]

    Store --> Processor
    Processor --> Task1[RunnableRuleTask 1]
    Processor --> Task2[RunnableRuleTask 2]
    Task1 --> FailedRule[FailedLoginRule]
    Task2 --> SeverityRule[HighSeverityRule]
    FailedRule --> Result[DetectionResult]
    SeverityRule --> Result

    Processor --> Investigation
    Investigation --> CLI
    CLI --> Output[Timeline, findings,<br/>risk score, errors]
~~~

## Layers and responsibilities

| Layer | Components | Responsibility |
|---|---|---|
| Presentation | ArgusCli | Accepts commands, shows results, and handles user-facing errors. |
| Validation and persistence | EventValidator, EventStore | Checks input and saves/loads pipe-delimited event records. |
| Analysis | EventProcessor, DetectionRule implementations | Runs anomaly rules concurrently and returns explainable findings. |
| Investigation | InvestigationService | Filters a user's events into a timeline and totals risk points. |
| Model | Event, DetectionResult | Immutable records passed between layers. |

## Processing sequence

~~~mermaid
sequenceDiagram
    participant U as User
    participant C as ArgusCli
    participant S as EventStore
    participant P as EventProcessor
    participant R1 as FailedLoginRule
    participant R2 as HighSeverityRule
    participant I as InvestigationService

    U->>C: analyze or investigate user
    C->>S: loadAll()
    S-->>C: List of events
    C->>P: analyze(events)
    par Concurrent rule tasks
        P->>R1: evaluate(events)
        R1-->>P: failed-login findings
    and
        P->>R2: evaluate(events)
        R2-->>P: severity findings
    end
    P-->>C: List of findings
    C->>I: timeline and riskScore
    I-->>C: ordered events and score
    C-->>U: readable findings or investigation view
~~~

## Why this design fits the project

- Each class has one focused role, so the code remains easy to explain.
- DetectionRule is an interface: future rules can be added without rewriting the processor.
- EventProcessor uses one Runnable task per rule in a bounded two-thread pool.
- File storage keeps the MVP portable and transparent, while the service boundary would allow a database later.
- Immutable event and result records reduce accidental state changes during concurrent analysis.
