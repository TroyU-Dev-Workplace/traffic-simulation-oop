# Traffic Simulation Group 1

A clean, modular traffic simulation project using JavaFX for the UI and pure Java for the backend simulation logic.

## Architecture

The project follows a strict layered architecture:

### 1. Simulation Layer (Pure Backend)

**Package:** `com.traffic.sim.simulation`

- Contains all logic for the traffic simulation.
- **No JavaFX dependencies.**
- `SimulationManager`: Main entry point for logic.
- `managers/`: Sub-managers for Vehicles, Pedestrians, Map, Traffic Lights.
- `entities/`: Data objects (Vehicle, Pedestrian, TrafficLight).
- `spawn/`: Logic for spawning entities.

### 2. MainController Layer (Coordinator)

**Package:** `com.traffic.sim.controller`

- `MainController`: Coordinates between the Simulation Layer and the Renderer.
- Manages the game loop (AnimationTimer).

### 3. UI Layer (JavaFX)

**Package:** `com.traffic.sim.rendering` & `com.traffic.sim.controller`

- `UIController`: Handles FXML interactions (Start/Stop/Reset buttons).
- `Renderer`: Draws the current state of the simulation onto the JavaFX Pane.
- `SpriteLoader`: Loads image assets.

## Project Structure

```
src/
 └── main/
      ├── java/
      │     └── com.traffic.sim/
      │           ├── controller/
      │           ├── rendering/
      │           ├── simulation/
      │           └── app/
      └── resources/
            ├── ui/
            ├── assets/
            └── styles/
```

## How to Run

### Using Maven

1.  Open terminal in project root.
2.  Run `mvn clean javafx:run`

### Requirements

- JDK 21+
- Maven 3.8+

## Usage

- **Start**: Begins the simulation loop.
- **Stop**: Pauses the simulation.
- **Reset**: Clears all vehicles and pedestrians.
- **Spawn Vehicle**: Adds a vehicle to a random road tile.
- **Spawn Pedestrian**: Adds a pedestrian to a random sidewalk tile.

## Workflow & Gitflow Rules

### 1. Gitflow Rules

The main branches are: `main` (release) and `develop` (active development).

New work **MUST** be createdzxcvc- `fix/fix-traffic-light`

- `hotfix/fix-traffic-light`
- `refactor/refactor-traffic-light`

After a task is completed, create a Pull Request into `develop`.

**Only the team lead merges PRs.**

### 2. Pull / Fetch Behavior

Always keep the local repository fully synced before starting any task:

1.  Switch to `develop`
2.  Run:
    ```bash
    git fetch --all
    git pull
    ```

Only after syncing, create new branches or continue development.

If Git reports “Already up to date” but remote has new commits, or a branch is missing, automatically trigger:

```bash
git fetch --all
```

### 3. Commit Message Convention

Use:

- `feat: description` //new feature
- `fix: description` //fix bug
- `refactor: description` //refactor code
- `chore: description` //chore
- `docs: description` //documentation

Example:

- `feat: add traffic light`
- `fix: fix traffic light`
- `refactor: refactor traffic light`
- `chore: chore traffic light`
- `docs: documentation traffic light`

Commit messages must be short, imperative, and English-based.

### 4. Conflict Prevention Logic

**Never commit directly to `develop` or `main`.**

Before pushing any branch:

```bash
git fetch --all
git rebase develop
```

If conflicts appear, resolve locally before creating PR.

### 5. Issue & PR Workflow

1.  Every task must start from a GitHub Issue.
2.  Branch name must match the Issue title meaningfully.
3.  PR must include:
    - Summary of changes
    - Linked Issue ID
    - Test results or manual verification steps
4.  Example:

    ```
    Title:
    Feature: Random vehicle spawning (Issue #12)

    Description:
    This PR implements the random spawning system for vehicles and pedestrians, including:
    - Car / Truck / Motor classification
    - HashMap-based entity storage
    - Randomized sprite selection
    - Updated Renderer pipeline to support dynamic types

    Linked Issue:
    Closes #12
    ```

### 6. General Naming Conventions

- **Variables**: `camelCase`
- **Classes**: `PascalCase`
- **Constants**: `UPPER_SNAKE_CASE`

Avoid abbreviations unless widely standardized.
