# Particle Simulator

## Overview
This project is a **2D particle simulator** that models moving charged particles with:
- **Coulomb forces** (repulsion/attraction depending on charge),
- **Elastic collisions** between particles,
- **Boundary collisions** with restitution (energy loss on bounce).

The simulator supports three execution modes:
1. **Sequential (single-core)** – a straightforward implementation.
2. **Parallel (multi-core, shared memory)** – forces computed in parallel using Java threads.
3. **Distributed (MPI, multi-process)** – particles simulated across multiple processes using MPJ Express.

The system can run **headless (no graphics)** for performance benchmarking or with a **real-time Swing GUI** for visualization. The GUI version can also add new particles during the simulation.

---

## Features
- Realistic 2D physics:
  - Particle-to-particle Coulomb interaction with softening to avoid singularities.
  - Elastic circle-to-circle collision resolution with restitution.
  - Wall collision and bouncing.
- Visualization at **60 FPS** using Swing.
- Three execution modes (sequential, parallel, distributed).
- Scalable: configurable number of particles and simulation cycles.
- Deterministic initialization via fixed random seed for reproducibility.
- CLI parameters to control:
  - Number of particles
  - Number of cycles
  - Whether to show GUI or run headless

---

## Implementation Details

### 1. Sequential Version 
- Implemented with a simple **nested double loop**:
  - Each pair `(i, j)` of particles computes Coulomb forces and applies equal and opposite updates.
  - Collisions are resolved after force calculation.
- Runs on a single CPU core.
- **Swing Timer (60 Hz)** drives updates and repainting when GUI is enabled.
- Provides a **control panel** to dynamically add new particles.

---

### 2. Parallel Version 
- Forces are computed **in parallel** using a Java **ExecutorService** with a fixed thread pool sized to `Runtime.getRuntime().availableProcessors()`.
- Work is divided into **chunks of particles** per thread.
- To avoid race conditions:
  - Each thread writes forces into a **per-thread buffer (force matrix)**.
  - After all tasks complete, forces are **reduced (summed)** into the main array.
- Particle-to-particle collisions are resolved with **ordered locking** to prevent deadlocks.
- Ensures deterministic results across runs with the same random seed.

---

### 3. Distributed Version 
- Uses **MPJ Express** (MPI for Java) for message passing across processes.
- Particle state is stored in a **flat double array** of length `n * FIELDS` (where FIELDS = x, y, dx, dy, mass, charge).
- Simulation steps:
  1. **Broadcast** initial state from rank 0 to all processes.
  2. Each process computes updates for its chunk of particles.
  3. **Allgatherv** collects updated local states into the global array.
  4. On rank 0:
     - Handle collisions between particles.
     - Broadcast corrected global state back to all processes.
- Only **rank 0** manages the GUI (other ranks compute only).
- GUI uses a **Swing Timer (60 FPS)** for smooth rendering of the latest synchronized state.

---

