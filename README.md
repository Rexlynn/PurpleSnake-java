# PurpleSnake

A small console security toolkit written in Java. Its main feature is a **TCP port scanner with scan history**: every scan is saved, and the next scan of the same host shows which ports were **newly opened** or are **no longer open**. It also contains an XOR file tool and a network information viewer.

> Developed for educational use and for the *Java OOP Projectwork* (Prog 2) assignment.
> **Only scan hosts you own or have explicit permission to scan.**

## Problem statement

Someone who runs a home lab or a few test servers needs to know which network services are exposed, and, more important, **when that changes**. A single port scan gives only a snapshot. PurpleSnake keeps every scan and compares it with the previous one, so an unexpected new open port is noticed immediately. See [docs/design.md](docs/design.md) for the full problem statement, design decisions and known limitations.

## Features

| Menu item | What it does |
|---|---|
| **Port Scanner** | Scans a host or IP for a port range, prints the open ports with service names, saves a report and shows the changes since the previous scan of the same target. |
| **Scan history** | Lists all saved reports (newest first) and shows the open ports of a chosen report. |
| **XOR Encrypt / Decrypt** | XOR-obfuscates or restores a file with a text key (obfuscation, **not** secure encryption). |
| **Network Info** | Shows the network interfaces and the active connections (uses `netstat` on Windows, `ss`/`netstat` on Linux/WSL). |

## Prerequisites

- JDK 17 or newer (`java -version`)
- Apache Maven 3.8 or newer (`mvn -v`)

## Build, test and run

```bash
# Build, run all tests, create the jar
mvn clean verify

# Start the program (run it from the project folder: reports are saved in ./data/reports)
java -jar target/purplesnake-1.0.0.jar
```

To run only the tests: `mvn test`.

## Sample usage

Two separate runs of the program. Lines after a `:` prompt marked with `<` are typed by the user.

```text
=== PurpleSnake Security Toolkit ===
1) Port Scanner (saves a report and shows changes)
2) Scan history
3) XOR Encrypt / Decrypt
4) Network Info
0) Exit
Your choice: 1                      <
Target IP or hostname: 127.0.0.1    <
Start port: 8079                    <
End port: 8083                      <

Scanning 127.0.0.1, ports 8079-8083 ...

Port  8081 OPEN (Unknown service)

Scan complete. Open: 1, Closed: 4, Filtered: 0
Report saved.
```

Later, after the service on port 8081 was stopped, the same scan in a new run of the program:

```text
Scan complete. Open: 0, Closed: 5, Filtered: 0
Report saved.

Changes since the scan of 2026-11-22 16:25:18:
  NO LONGER OPEN: port 8081
```

Scan history (menu item 2):

```text
=== Scan History ===
1) 2026-11-22 16:25:19  127.0.0.1  open=0 closed=5 filtered=0
2) 2026-11-22 16:25:18  127.0.0.1  open=1 closed=4 filtered=0
Report number for details (0 = back):
```

## Implemented requirements

| Requirement | Where in the code |
|---|---|
| **Encapsulation** | `PortRange`, `ScanResult`, `ScanReport`: private final fields, constructors that only create valid objects, defensive copy and read-only list in `ScanReport`. |
| **Abstraction** | Interfaces `Command` and `ReportRepository`, abstract class `BaseCommand`. |
| **Inheritance and runtime polymorphism** | `PortScanCommand`, `HistoryCommand`, `XorCommand`, `NetworkInfoCommand` extend `BaseCommand`; `Menu` runs them only through the `Command` type. `ReportRepository` has two implementations (`FileReportRepository`, `InMemoryReportRepository`). |
| **Relationships** | Composition `ScanReport` 1 — 0..* `ScanResult`; aggregation `Menu` 0..* `Command`; associations between commands and services (see UML). |
| **Collections and generics** | `List<ScanResult>`, `Map<Integer, String>`, `Optional<ScanReport>`. Sorting: `ScanResult` implements `Comparable`, plus `Comparator` lambdas (`ScanReport.getSortedBy`, `HistoryCommand`, `FileReportRepository`). |
| **Exceptions** | Custom `InvalidPortRangeException` (checked, domain rule), `ReportStorageException` (checked), `InputClosedException` (unchecked). Input is validated everywhere and failures are handled and reported. |
| **Persistence** | `FileReportRepository` writes and reads scan reports as text files in `data/reports/` using try-with-resources. |
| **Testing** | 93 JUnit 5 tests in `src/test/java` (domain rules, scanner on localhost, persistence in a temp folder, menu and commands with scripted input). |
| **Build** | Maven project, builds and tests from a clean checkout with `mvn clean verify`. |

## Chosen design pattern: Command

Every menu action is an object that implements `Command` (`getLabel()`, `execute()`).

| Role in the pattern | Class |
|---|---|
| Command (interface) | `Command` |
| Shared base | `BaseCommand` (abstract) |
| Concrete commands | `PortScanCommand`, `HistoryCommand`, `XorCommand`, `NetworkInfoCommand` |
| Invoker | `Menu` |
| Client (builds and registers the commands) | `Main` |
| Receivers (do the real work) | `PortScanner`, `ReportRepository`, `ReportComparator`, `XorCipher`, `NetworkInfoProvider` |

**Benefit:** the menu contains no `switch` over tool types. A new tool is one new class plus one line in `Main`, and `Menu` does not change. Each action can also be tested alone with scripted input (see `MenuTest`, `PortScanCommandTest`).

## Project structure

```
src/main/java/io/github/rexlynn/purplesnake/
├── Main.java
├── model/         ScanReport, ScanResult, PortRange, PortState, ScanComparison, ServiceCatalog
├── service/       PortScanner, ReportComparator, XorCipher, NetworkInfoProvider
├── persistence/   ReportRepository, FileReportRepository, InMemoryReportRepository
├── command/       Command, BaseCommand, PortScanCommand, HistoryCommand, XorCommand, NetworkInfoCommand
├── ui/            Menu, ConsoleIO
└── exception/     InvalidPortRangeException, ReportStorageException, InputClosedException
src/test/java/...  JUnit 5 tests (same package structure)
docs/              UML class diagrams, user stories, design notes, release notes
```

## Documentation

- [Problem statement, design decisions, known limitations](docs/design.md)
- [UML class diagrams](docs/class-diagrams.md) (editable Mermaid sources: `docs/*.mmd`)
- [User stories](docs/user-stories.md)
- [Release notes v1.0.0](docs/release-notes-v1.0.0.md)

## Contributors

- Berke Bolsoy ([@Rexlynn](https://github.com/Rexlynn)), University of Pécs

AI assistance: the design and code were developed together with Claude (Anthropic), as permitted by the course instructor.

## License

MIT, see [LICENSE](LICENSE).
