# Design notes

## Problem statement

**Users:** students, home-lab owners and junior security analysts who run a few hosts they are allowed to scan.

**Goal:** know which network services (open TCP ports) a host exposes, and notice when this changes.

**Problem:** a normal port scan prints a list and forgets it. Nobody remembers what the list looked like last week, so a service that was opened by mistake stays unnoticed.

**Solution:** PurpleSnake saves every scan as a report and compares each new scan with the previous report of the same target. The result is "port X is newly open" or "port Y is no longer open". The XOR tool and the network information viewer are small helper tools in the same console menu.

## Design pattern: Command

**Design problem:** the menu must start different tools (port scan, history, XOR, network info). A big `switch` that knows every tool would have to change each time a tool is added, and each tool could only be tested through the whole menu.

**Solution:** every menu action is an object with the same two methods, `getLabel()` and `execute()`.

| Role | Class (see [UML: commands](class-diagrams.md)) |
|---|---|
| Command interface | `command.Command` |
| Shared base (inheritance) | `command.BaseCommand` |
| Concrete commands | `PortScanCommand`, `HistoryCommand`, `XorCommand`, `NetworkInfoCommand` |
| Invoker | `ui.Menu` (`register(...)`, `run()`) |
| Client | `Main` (creates the commands and registers them) |
| Receivers | `PortScanner`, `ReportRepository`, `ReportComparator`, `XorCipher`, `NetworkInfoProvider` |

**Benefit:** `Menu` only calls `commands.get(i).execute()`, so the same line runs different code for different objects (runtime polymorphism). A new tool is one new class and one `register(...)` line in `Main`; `Menu` stays unchanged. Each command can be tested alone with scripted input.

## Main design decisions

| Decision | Reason |
|---|---|
| **Command pattern for the menu** | Every menu action is an object. The menu does not know the concrete tools, so a new tool needs one new class and one line in `Main`. |
| **`ReportRepository` interface** | The rest of the program does not depend on how reports are stored. Files are used in production, memory in tests. |
| **Immutable domain objects with validating constructors** | `PortRange`, `ScanResult` and `ScanReport` cannot be created in an invalid state, and cannot change afterwards. This removes a whole class of bugs. |
| **`PortRange` throws a checked exception** | Invalid input from a user is an expected situation, so the compiler forces the caller to handle it. `InputClosedException` is unchecked because it can occur at any prompt and is handled once in `Main`. |
| **All console input and output goes through `ConsoleIO`** | The streams are injected, so tests can type scripted input and capture the output. No test needs a real keyboard. |
| **Plain text report files** | Easy to read, debug and test. The format has a version header, so it can evolve. |
| **Safe file names** | The target text comes from the user, so every character outside `A-Z a-z 0-9 . -` is replaced in the file name. This blocks path tricks such as `../../x`. |
| **Serial scanning** | Simple and predictable. A warning is shown for ranges over 100 ports, with an estimate of the worst-case time. |
| **Compare only ports scanned in both reports** | A port that was not scanned before is unknown, not "newly opened". |
| **No GUI** | The assignment requires a console interface. Keeping one interface keeps the code small enough to understand completely. |
| **Overwrite protection** | The XOR tool asks before overwriting a file and refuses to use the same file as input and output (which would empty it). |

## Known limitations

- The scan is serial. A large range on a host that does not answer can take a long time (up to one timeout per port).
- Only TCP connect scanning is implemented (no UDP, no SYN scan, no banner or version detection).
- XOR with a repeating key is obfuscation. It is **not** secure encryption.
- "Active connections" calls the operating system tool (`netstat` or `ss`), so its output depends on the system, and it shows the real connections of the computer.
- One corrupt report file makes the history unreadable until that file is fixed or removed (the error message names the file).
- Two reports for the same target in the same millisecond would collide; the second save fails instead of overwriting.
- The history list is not paged or filtered.
