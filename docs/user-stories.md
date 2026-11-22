# User stories

Roles: **lab owner** (a person who runs hosts they may scan) and **developer** (a person who builds and tests the project).

| ID | User story | Acceptance criteria |
|---|---|---|
| US-1 | As a lab owner, I want to scan a host for a range of ports, so that I can see which services are open. | Open ports are listed with service names. A summary shows the number of open, closed and filtered ports. |
| US-2 | As a lab owner, I want invalid input to be rejected with a clear message, so that I do not scan by mistake. | Empty target, unknown host, ports outside 1-65535 and start > end are rejected. Large ranges ask for confirmation first. |
| US-3 | As a lab owner, I want every scan to be saved automatically, so that I keep a history without extra work. | After a scan a report file exists in `data/reports`. The data is still there after restarting the program. |
| US-4 | As a lab owner, I want to see what changed since the previous scan of the same host, so that I notice unexpected open ports. | New open ports and ports that are no longer open are listed. "No changes" is shown if nothing changed. |
| US-5 | As a lab owner, I want to browse my scan history, so that I can look at old results. | Reports are listed newest first with date, target and counts. I can open one to see its open ports. |
| US-6 | As a lab owner, I want to obfuscate and restore a file with a key, so that I can hide a file from casual viewing. | Running the tool twice with the same key restores the original. The tool never overwrites a file without asking and never uses the same file as input and output. |
| US-7 | As a lab owner, I want to see my network interfaces and active connections, so that I know my own network state. | Interfaces with IP addresses and the system connection list are shown. A warning says that real connections are visible. |
| US-8 | As a lab owner, I want the menu to survive mistakes, so that one wrong key does not end my session. | Invalid menu input asks again. Closing the input (Ctrl+D / Ctrl+Z) ends the program cleanly, without an error trace. |
| US-9 | As a developer, I want the project to build and test from a clean checkout, so that anybody can verify it. | `mvn clean verify` succeeds and runs all tests. |
