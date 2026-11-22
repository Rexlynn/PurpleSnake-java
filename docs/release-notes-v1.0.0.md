# PurpleSnake v1.0.0

First complete release.

## Features
- TCP port scanner with service names, input validation and a warning for large ranges
- Automatic saving of every scan as a report file
- Comparison with the previous scan of the same target (newly open / no longer open)
- Scan history (newest first) with details
- XOR file tool with overwrite protection
- Network interfaces and active connections viewer
- Robust console menu (invalid input, closed input)

## Technical
- Java 17, Maven build, 93 JUnit 5 tests
- Command pattern, repository interface, immutable domain objects, custom exceptions
- UML class diagrams and user stories in `docs/`

## Known limitations
- Serial scanning (slow for large ranges on hosts that do not answer)
- TCP connect scan only
- XOR is obfuscation, not secure encryption
- Active connections depend on `netstat` / `ss` of the operating system
