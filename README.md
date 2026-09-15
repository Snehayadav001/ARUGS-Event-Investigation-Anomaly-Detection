# ARGUS — Concurrent Event Investigation & Anomaly Detection Engine

ARGUS is a small Java command-line application for storing security events, detecting suspicious activity concurrently, and investigating a user's event timeline.

## What problem does it solve?

Small teams often receive authentication and system events as raw logs. Reading every line makes it easy to miss a burst of failed logins or a critical event. ARGUS keeps a local event file, applies transparent detection rules, and gives an investigator a focused timeline.

## MVP features

- Event ingestion with validation and file-based CSV storage
- Concurrent rule evaluation using `Runnable` tasks and a fixed thread pool
- Two explainable anomaly rules: high-severity events and repeated failed logins
- Per-user investigation timeline and risk summary
- Interactive CLI plus one-command modes
- Dependency-free tests runnable with the Java compiler

## Requirements

Java 17 or newer. No Maven/Gradle or external libraries are required.

## Run

From the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object FullName)
java -cp out com.argus.cli.ArgusCli seed
java -cp out com.argus.cli.ArgusCli analyze
java -cp out com.argus.cli.ArgusCli investigate alice
java -cp out com.argus.cli.ArgusCli
```

The `seed` command creates `data/events.csv` only when it does not already contain events. In interactive mode, type `help` for commands and `exit` to close it.

## Test

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp out com.argus.ArgusMvpTest
```

## Project layout

```text
src/main/java/com/argus/
  cli/          command-line entry point
  detection/    detection interface, rules, and results
  model/        Event value object
  service/      storage, validation, processing, investigation
docs/           specification and diagrams
data/           runtime CSV data (created when used)
```

See [the v1.0 specification](docs/V1_SPECIFICATION.md) for the rules, commands, and implementation phases. See [the architecture document](docs/PROJECT_ARCHITECTURE.md) for the system diagram and processing sequence, [the design diagrams](docs/DESIGN_DIAGRAMS.md) for the use-case, workflow, class, sequence, and storage diagrams, and [statement.md](statement.md) for the academic project statement.
