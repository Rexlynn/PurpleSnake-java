# PurpleSnake - Multi-Platform Security Toolkit (Java)

A modular cybersecurity utility written in Java for educational demonstration and network security learning, designed to runs on Windows, Linux and WSL

## Overview
- PurpleSnake is a lightweight security toolkit demonstrating core networking and basic file obfuscation concepts in Java.
- Organized into separate methods and classes for port scanning, basic file encryption/decryption, and network monitoring.
- Developed strictly for educational and authorized demonstration purposes.

## Features

1. **TCP Port Scanner**
   - Scans target hosts/IPs across a specified port range.
   - Maps common ports to service names and handles connection timeouts.

2. **XOR Encrypt / Decrypt Tool**
   - Stream-based file encryption/decryption using buffered streams and a repeating key.
   - Demonstrates symmetric XOR logic along with basic output/overwrite safety checks.

3. **Network Monitor**
   - Lists local network interfaces via Java's `NetworkInterface` API.
   - Runs OS-native commands (`ss`/`netstat` on Linux/WSL, `netstat` on Windows) via `ProcessBuilder`.

## Build & Run

- **Compile:**
  ```bash
  javac PurpleSnake.java
  ```
- **Run:**
  ```bash
  java PurpleSnake
  ```
## Disclaimer

- Do not scan targets or networks without explicit permission.
- The author takes no responsibility for misuse.

## Author 

Berke Bolsoy  
GitHub: https://github.com/Rexlynn  
Computer Science Engineering – 2nd Year University of Pécs (PTE)  

