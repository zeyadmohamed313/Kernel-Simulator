<img width="2816" height="1536" alt="Gemini_Generated_Image_axryhiaxryhiaxry" src="https://github.com/user-attachments/assets/f5da0494-a65f-40e5-a3c8-1b2e39ce2551" />


## 📖 Overview

**MyOS** is not a bootable operating system, but a sophisticated **Kernel Simulator** running on the JVM. It exposes low-level OS functionalities—such as Process Scheduling, Memory Management, and Inter-Process Communication (IPC)—via high-level RESTful APIs.

The goal of this project was to bridge the gap between theoretical OS concepts (like Semaphores, Paging, and Deadlocks) and practical software engineering, simulating the chaotic "orchestration" that happens inside a CPU.

---

## 🏗️ System Architecture

The system follows a clean **Layered Architecture**:

1.  **Interface Layer:** REST Controllers (`OsController`, `FsController`) acting as System Calls.
2.  **Kernel Layer:** The central Orchestrator (`KernelService`) managing the Scheduler Thread and Process Table (PCB).
3.  **Subsystem Layer:** Specialized managers for Memory, Resources, IPC, and File System.
4.  **Infrastructure:** In-Memory Data Structures (Queues, Trees, Graphs, Semaphores).

![System Architecture Diagram](https://github.com/YourUsername/MyOS-Simulator/blob/main/docs/architecture-diagram.png)
*(Place your architecture image in a docs folder and link it here)*

---

## 🚀 Key Features

### 1. CPU Scheduling (Round Robin)
* **Mechanism:** Implements a custom **Preemptive Round Robin Scheduler** running asynchronously.
* **Context Switching:** Simulates saving and loading Process Control Blocks (PCBs) based on a configurable **Time Quantum**.
* **State Management:** Manages accurate process states (`READY`, `RUNNING`, `WAITING`, `TERMINATED`).

### 2. Memory Management (Paging)
* **Simulation:** Replaces standard JVM memory handling with a simulated **Paging System**.
* **Logic:** RAM is divided into fixed-size **Frames** and Processes into **Pages**.
* **Allocation:** The Memory Manager handles the Logical-to-Physical address translation and rejects processes if frames are exhausted.

### 3. Inter-Process Communication (IPC) & Concurrency
* **Problem Solved:** The classic **Producer-Consumer Problem**.
* **Synchronization:** Utilizes **Counting Semaphores** for buffer capacity management and **Mutexes** for thread safety.
* **Blocking Behavior:**
    * If a Producer tries to write to a full buffer, the thread is **Blocked** (Request hangs).
    * It automatically resumes only when a Consumer reads data, proving true thread synchronization.

### 4. Deadlock Detection & Recovery
* **Algorithm:** Uses **Depth-First Search (DFS)** on a Resource Allocation Graph.
* **Detection:** Identifies circular wait conditions (Cycles) between processes competing for resources (e.g., Mutexes).
* **Resolution:** Automatically selects a **"Victim Process"**, terminates it, and releases its held resources to restore system stability.

### 5. Virtual File System (VFS)
* **Structure:** An In-Memory **Composite Tree** representing directories and files.
* **Concurrency:** Implements the **Readers-Writers Lock Pattern**.
    * Allows multiple processes to `read/ls` simultaneously (High Throughput).
    * Enforces exclusive access for `write` operations to ensure **Data Consistency**.

---

## 🛠️ Design Patterns Used

* **Singleton:** For the `KernelService` to ensure a single point of orchestration.
* **Factory Method:** For creating Process Control Blocks (PCBs) with initial configurations.
* **Composite:** For the File System (treating Files and Directories as `FSNode`).
* **Producer-Consumer:** For the IPC Subsystem structure.
* **Readers-Writers:** For File System concurrency control.

---

## 🧪 How to Run

### Prerequisites
* Java 17 or 21+
* Maven

### Installation
1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/YourUsername/MyOS-Simulator.git](https://github.com/YourUsername/MyOS-Simulator.git)
    ```
2.  **Build the project:**
    ```bash
    mvn clean install
    ```
3.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```

---

## 🎮 API Usage Scenarios

You can test the simulator using **Postman** or **cURL**.

### Scenario 1: Start System & Submit Processes
Initialize the OS with a Producer and a Consumer process.
```bash
curl -X POST http://localhost:8080/api/os/submit-batch \
  -H "Content-Type: application/json" \
  -d '[
    { "name": "P1-Producer", "totalInstructions": 100, "memoryRequired": 10 },
    { "name": "P2-Consumer", "totalInstructions": 100, "memoryRequired": 10 }
  ]'


### Scenario 2: Test IPC Blocking (The "Magic" Moment)
1. **Create a Buffer of Size 1:**
   curl -X POST "http://localhost:8080/api/os/ipc/create?capacity=1"

2. Fill the Buffer (Producer):
curl -X POST "http://localhost:8080/api/os/ipc/write?pid=1&bufferId=1&data=Apple"

Try to Overfill (Blocking Occurs):
# This request will HANG (Loading...) until the consumer reads.
curl -X POST "http://localhost:8080/api/os/ipc/write?pid=1&bufferId=1&data=Banana"

Scenario 3: File System Operations
Create a directory and write to a file safely.


# Create directory
curl -X POST "http://localhost:8080/api/fs/mkdir?parent=/&name=home"

# Write to file (Thread Safe)
curl -X POST http://localhost:8080/api/fs/write \
   -H "Content-Type: application/json" \
   -d '{ "parentPath": "/home", "fileName": "log.txt", "content": "Hello Sys!" }'

# Create an Empty File (touch):
curl -X POST "http://localhost:8080/api/fs/touch?parent=/home&name=syslog.txt"

# List Directory Contents (ls):
# Returns: ["syslog.txt"]
curl -X GET "http://localhost:8080/api/fs/ls?path=/home"
Write to File (write - Exclusive Lock):


# This operation simulates a delay. During this time, no one else can read or write.
curl -X POST http://localhost:8080/api/fs/write \
  -H "Content-Type: application/json" \
  -d '{ "path": "/home/syslog.txt", "content": "Kernel started successfully at 00:00:01" }'

#Read File Content (cat - Shared Lock):
curl -X GET "http://localhost:8080/api/fs/cat?path=/home/syslog.txt"


🧠 Technologies
Language: Java

Framework: Spring Boot 3 (Web)

Concurrency: java.util.concurrent (Semaphores, ReentrantReadWriteLock, Executors).

Tools: Maven, Docker, Postman.
