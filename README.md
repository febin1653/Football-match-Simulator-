# Event-Based Football Match Simulator Engine

A lightweight, probabilistic Java simulation engine that models football matches through dynamic event allocation rather than fixed time-step loops. 

Instead of generating arbitrary scores, the engine calculates realistic match outcomes by evaluating team power scores, adaptive defensive barriers, playmaker build-up quality, and individual finisher statistics.

---

## Project Overview

The core simulation shifts away from minute-by-minute time logic to an incident-bucket system. Matches are resolved through discrete match events divided between two competing teams based on their relative strength.

### Key Logic & Features

1. **Power Score & Event Allocation**: Match event capacity is determined by comparing both teams' combined strength—derived from Overall Rating (OVR), Domestic Prestige, Match Importance, and potential Underdog Upset factors (FOG).
2. **Creator-Finisher Dynamic**: When a goal chance triggers, a playmaker's passing stat calculates an Assist Quality score. High assist quality directly lowers the defending team's barrier, giving the finisher a higher probability of scoring.
3. **Discipline System**: Card events are triggered independently (25% base chance). Players with higher Aggressiveness stats are prioritized for bookings, and sending-off events (Red Cards) adjust active team availability in real time.
4. **Database Integration**: Teams and rosters are stored in a local SQLite database (`football_sim.db`), allowing users to select match participants dynamically via an interactive menu.

---

## Technical & Java Memory Optimizations

To ensure the engine executes with minimal system overhead and prevents native memory allocation failures during runtime initialization, several Java-level optimizations were implemented:

- **Primitive Byte Scaling**: Individual player attributes (ratings 0–99) are stored using primitive `byte` fields instead of 32-bit `int` types, significantly reducing the memory footprint per object instance.
- **ArrayList Over Vector**: Synchronized `Vector` usage was replaced with `ArrayList` to eliminate thread-locking overhead during match event resolution.
- **Zero In-Loop Heap Allocation**: The attacking pool bucket (`Player[] attBucket`) is instantiated once per match and repopulated in-place. This prevents unnecessary object instantiation during match loops and reduces Garbage Collection (GC) load.

---

## Teams and Roster Data Model

The application uses an external SQLite database to manage clubs and their starting lineups. On initial launch, the system automatically creates the database schema and populates sample teams.

### Included Default Squads

- **Real Madrid**: Includes squad profiles (e.g., Courtois, Carvajal, Modric, Bellingham, Mbappe, Vinicius Jr) mapped to specific tactical position identifiers.
- **FC Barcelona**: Includes  squad profiles (e.g., Ter Stegen, Araujo, Pedri, Lewandowski, Raphinha, Lamine Yamal).

### Squad Positional Schema

Players are mapped using positional numbers (`1` to `11`) to accurately construct positional pools during match events:

- `1`: Goalkeeper (GK)
- `2 - 5`: Defenders (CB, RB, LB)
- `6 - 8`: Midfielders (LM, CAM, RM)
- `9 - 11`: Attackers (ST, LW, RW)

---

## Compilation and Running

### Prerequisites

- Java Development Kit (JDK 17 or higher)
- SQLite JDBC Driver JAR (`sqlite-jdbc-3.45.1.0.jar`)

### Build and Run Instructions

1. Download or clone the repository.
2. Ensure the SQLite JDBC driver file is placed in the project root directory.
3. Compile the Java source files:javac -cp .;sqlite-jdbc-3.45.1.0.jar sim.java
   
   ```cmd
### OUTPUT
- - -
   
   <img width="799" height="470" alt="Screenshot 2026-10-16 105222" src="https://github.com/user-attachments/assets/37638c81-4202-4fb8-9acf-955f4ea84a5e" />
<img width="799" height="470" alt="Screenshot 2026-10-16 105222" src="https://github.com/user-attachments/assets/4b7bc0a1-b9b3-4b22-ac24-cd5f7b321b56" />
