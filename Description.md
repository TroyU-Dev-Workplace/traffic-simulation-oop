# Traffic Simulation System - Academic Documentation

**Course:** CS3360 - Object-Oriented Programming  
**Project:** Traffic Simulation Group 1  
**Technology Stack:** Java 21, JavaFX 21.0.1, Maven

---

## 1. Project Overview

### Project Name

**Traffic Simulation System**

### Brief Description

This project is a real-time traffic simulation system that models the behavior of vehicles, pedestrians, and traffic lights within an urban intersection environment. The system visualizes traffic flow, entity spawning, and traffic light coordination in a 2D grid-based representation.

### Main Objectives

- **Demonstrate OOP Principles**: Apply core object-oriented concepts including encapsulation, abstraction, inheritance, and polymorphism
- **Model Real-World Traffic Systems**: Simulate realistic traffic behaviors including vehicle movement, pedestrian crossing, and synchronized traffic light control
- **Achieve Clean Architecture**: Maintain strict separation between simulation logic (backend) and visualization (frontend)
- **Enable Extensibility**: Design a modular system that can be easily extended with new entity types, behaviors, and features

### Target Users

1. **System Administrators**: Control simulation parameters via UI controls (Start, Stop, Reset, Spawn entities)
2. **Academic Reviewers**: Evaluate the OOP design and implementation quality
3. **Developers**: Extend the system with additional features and entity types

---

## 2. Project Scope & Current Status

### Current Development Stage

**Prototype / MVP (Minimum Viable Product)**

The project has established core architectural foundations and basic simulation capabilities. The system is functional but represents an early-stage implementation with room for enhancement.

### Features Implemented ✓

#### Core Architecture

- ✓ **Layered Architecture**: Complete separation between simulation logic (pure Java) and rendering (JavaFX)
- ✓ **Game Loop System**: `AnimationTimer`-based update-render cycle controlled by `MainController`
- ✓ **Resource Management**: Sprite loading with caching system via `SpriteLoader`

#### Entity System

- ✓ **Vehicle Entities**: Three vehicle types (Car, Truck, Motor) with randomized sprite selection (20 total sprites)
- ✓ **Pedestrian Entities**: Eleven pedestrian sprite variations with randomized selection
- ✓ **Traffic Light Entities**: Separate vehicle and pedestrian lights with directional orientation (NS/EW)

#### Simulation Logic

- ✓ **Entity Managers**: Dedicated managers for vehicles, pedestrians, and traffic lights
- ✓ **Spawn System**: Abstract base class with concrete implementations for vehicle and pedestrian spawning
- ✓ **Traffic Light Coordination**: State machine controlling traffic light phases with proper timing
- ✓ **Map System**: Grid-based map with tile types (road, sidewalk, obstacle)

#### User Interface

- ✓ **Control Panel**: Start, Stop, Reset, Spawn Vehicle, Spawn Pedestrian buttons
- ✓ **Visual Rendering**: Real-time display of vehicles, pedestrians, traffic lights, and map grid
- ✓ **FXML-based UI**: Separation of UI structure from logic

### Features Partially Implemented or Not Yet Implemented ✗

#### Movement & Pathfinding

- ✗ **Vehicle Pathfinding**: Vehicles currently move in a straight line based on direction vector without path planning
- ✗ **Collision Detection**: No collision avoidance between vehicles or between vehicles and pedestrians
- ✗ **Lane Management**: Vehicles do not follow defined lanes or road rules
- ✗ **Traffic Light Obedience**: Vehicles do not stop at red lights

#### Pedestrian Behavior

- ✗ **Pedestrian Pathfinding**: Pedestrians move randomly without destination-based movement
- ✗ **Crosswalk System**: No defined crosswalk zones or pedestrian crossing behavior
- ✗ **Pedestrian-Traffic Light Integration**: Pedestrians do not respond to pedestrian crossing signals

#### Advanced Features

- ✗ **Statistics Dashboard**: No metrics tracking (vehicle count, average speed, wait times)
- ✗ **Configurable Simulation Parameters**: Speed, spawn rate, and intersection layout are hardcoded
- ✗ **Multiple Intersections**: Currently limited to a single intersection
- ✗ **Save/Load Functionality**: No ability to save or load simulation states

---

## 3. System Architecture Overview

### High-Level Architecture

The system follows a **strict layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│                  (com.traffic.sim.app)                       │
│                        App.java                              │
│                    JavaFX Entry Point                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                     UI Layer                                 │
│              (com.traffic.sim.controller)                    │
│                   (com.traffic.sim.rendering)                │
├──────────────────────────────────────────────────────────────┤
│  • UIController: FXML event handlers                         │
│  • Renderer: Visualization of simulation state               │
│  • SpriteLoader: Image asset management                      │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                  Controller Layer                            │
│              (com.traffic.sim.controller)                    │
├──────────────────────────────────────────────────────────────┤
│  • MainController: Coordinates simulation + rendering        │
│  • Game Loop: AnimationTimer managing update-render cycle    │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                 Simulation Layer                             │
│               (com.traffic.sim.simulation)                   │
│                    Pure Java - No JavaFX                     │
├──────────────────────────────────────────────────────────────┤
│  • SimulationManager: Main simulation coordinator            │
│  • Managers: VehicleManager, PedestrianManager,              │
│              TrafficLightSystem, MapSystem                   │
│  • Entities: Vehicle, Pedestrian, TrafficLight               │
│  • Spawn System: SpawnBase, SpawnVehicle, SpawnPedestrian    │
│  • Map: Grid-based map representation                        │
└──────────────────────────────────────────────────────────────┘
```

### Package Structure and Responsibilities

#### **1. Application Layer** (`com.traffic.sim.app`)

- **Responsibility**: JavaFX application initialization and lifecycle management
- **Key Class**: `App`
  - Extends `javafx.application.Application`
  - Loads FXML UI definition
  - Sets up primary stage with window properties (1280x720, non-resizable)

#### **2. UI Layer** (`com.traffic.sim.controller`, `com.traffic.sim.rendering`)

- **Responsibility**: User interaction handling and visual presentation
- **Key Classes**:
  - `UIController`: Bridges FXML UI elements to `MainController` logic
  - `Renderer`: Translates simulation state into JavaFX graphics
  - `SpriteLoader`: Manages image loading with caching to optimize performance

#### **3. Controller Layer** (`com.traffic.sim.controller`)

- **Responsibility**: Orchestrates interaction between simulation and rendering
- **Key Class**: `MainController`
  - Creates and manages `AnimationTimer` game loop
  - Calls `SimulationManager.update()` every frame
  - Triggers `Renderer.render()` with current simulation state
  - Provides control methods: `start()`, `stop()`, `reset()`, `spawnVehicle()`, `spawnPedestrian()`

#### **4. Simulation Layer** (`com.traffic.sim.simulation`)

- **Responsibility**: Core simulation logic (completely independent of JavaFX)
- **Sub-packages**:
  - **`managers`**: Domain-specific managers
    - `VehicleManager`: CRUD operations for vehicles
    - `PedestrianManager`: CRUD operations for pedestrians
    - `TrafficLightSystem`: State machine controlling traffic light phases
    - `MapSystem`: Wrapper for map data access
  - **`entities`**: Domain entities representing simulation objects
    - `Vehicle`: Position, speed, direction, sprite metadata
    - `Pedestrian`: Position, speed, sprite metadata
    - `TrafficLight`: Position, state (RED/YELLOW/GREEN), type (VEHICLE/PEDESTRIAN), direction (NS/EW)
  - **`spawn`**: Spawning logic using Template Method pattern
    - `SpawnBase` (abstract): Common spawning logic structure
    - `SpawnVehicle`: Vehicle-specific spawn logic (spawns on road tiles)
    - `SpawnPedestrian`: Pedestrian-specific spawn logic (spawns on sidewalk tiles)
  - **`map`**: Map and grid system
    - `Map`: 2D grid with tile types (0=Road, 1=Obstacle, 2=Sidewalk)

### Logical Architecture Explanation

The architecture follows the **Model-View-Controller (MVC)** pattern with additional layers:

1. **View** (`Renderer`, `UIController`, FXML files): Presents information to the user
2. **Controller** (`MainController`, `UIController`): Processes user input and coordinates model-view interaction
3. **Model** (`SimulationManager`, Entity classes, Managers): Encapsulates business logic and data

**Key Design Principle**: **Separation of Concerns**

- The simulation layer is entirely framework-agnostic (pure Java)
- Rendering logic is isolated in the `Renderer` class
- Controllers act as mediators, preventing tight coupling between layers

**Data Flow**:

1. User interaction → `UIController` → `MainController`
2. `MainController` → `SimulationManager.update()` (update simulation state)
3. `SimulationManager` → Individual managers update their entities
4. `MainController` → `Renderer.render(simulationManager)` (read-only access to state)
5. `Renderer` reads entity lists and draws to JavaFX `Pane`

---

## 4. Object-Oriented Design Analysis

### 4.1 Key Classes

#### **Entities Package** (`com.traffic.sim.simulation.entities`)

| Class          | Responsibility              | Key Attributes                                                                                      | Key Methods                                        |
| -------------- | --------------------------- | --------------------------------------------------------------------------------------------------- | -------------------------------------------------- |
| `Vehicle`      | Represents a moving vehicle | `id`, `x`, `y`, `speed`, `directionX`, `directionY`, `vehicleType`, `spritePath`, `width`, `height` | `update()`, `setDirection()`, `initializeSprite()` |
| `Pedestrian`   | Represents a pedestrian     | `id`, `x`, `y`, `speed`, `spritePath`, `width`, `height`                                            | `update()`, `initializeSprite()`                   |
| `TrafficLight` | Represents a traffic signal | `id`, `x`, `y`, `currentState`, `type`, `direction`                                                 | `setState()`, `update()`                           |

**Notes**:

- All entities are **data-centric** classes with clear state management
- Each entity is responsible for its own behavior (`update()` method)
- Sprite initialization is encapsulated within each entity

#### **Managers Package** (`com.traffic.sim.simulation.managers`)

| Class                | Responsibility                                    | Key Data Structure                          |
| -------------------- | ------------------------------------------------- | ------------------------------------------- |
| `VehicleManager`     | Manages vehicle lifecycle                         | `HashMap<String, Vehicle>`                  |
| `PedestrianManager`  | Manages pedestrian lifecycle                      | `HashMap<String, Pedestrian>`               |
| `TrafficLightSystem` | Controls traffic light phases using state machine | `List<TrafficLight>`, `Phase` enum, `timer` |
| `MapSystem`          | Provides access to map grid                       | `Map` object                                |

**Design Pattern**: **Manager Pattern** (also known as **Registry Pattern**)

- Centralizes entity storage and retrieval
- Provides unified interface for entity operations (add, remove, update, clear)
- Decouples entity management from simulation logic

#### **Coordinator Classes**

| Class               | Responsibility                       | Coordination Role                                                               |
| ------------------- | ------------------------------------ | ------------------------------------------------------------------------------- |
| `SimulationManager` | Orchestrates all simulation logic    | Aggregates all managers, delegates `update()` calls, provides read-only getters |
| `MainController`    | Coordinates simulation and rendering | Manages game loop, calls simulation update, triggers rendering                  |

#### **Spawn System** (`com.traffic.sim.simulation.spawn`)

| Class             | Type     | Responsibility                                     |
| ----------------- | -------- | -------------------------------------------------- |
| `SpawnBase`       | Abstract | Defines template for spawning logic                |
| `SpawnVehicle`    | Concrete | Implements vehicle spawn (finds road tiles)        |
| `SpawnPedestrian` | Concrete | Implements pedestrian spawn (finds sidewalk tiles) |

**Design Pattern**: **Template Method Pattern**

- `SpawnBase` defines the interface (`abstract void spawn()`)
- Concrete classes implement spawn logic specific to entity type

---

### 4.2 Application of OOP Principles

This project demonstrates all four fundamental OOP principles:

#### **1. Encapsulation**

**Definition**: Hiding internal state and requiring all interaction through methods.

**Examples in Project**:

- **Private Fields with Public Getters**: All entity classes (`Vehicle`, `Pedestrian`, `TrafficLight`) keep their state private and expose read-only access via getters:

  ```java
  // In Vehicle.java
  private double x, y;
  private double speed;

  public double getX() { return x; }
  public double getY() { return y; }
  ```

- **Manager Classes Hide Implementation Details**:

  ```java
  // VehicleManager.java
  private Map<String, Vehicle> vehicles; // Internal storage hidden

  public List<Vehicle> getVehicles() {
      return new ArrayList<>(vehicles.values()); // Returns defensive copy
  }
  ```

  The internal use of `HashMap` is hidden from clients, which only see a `List`.

- **SpriteLoader Cache**: The caching mechanism is completely hidden:

  ```java
  private Map<String, Image> cache; // Clients never access this directly

  public Image getSprite(String path) {
      // Caching logic is encapsulated
  }
  ```

**Benefits**:

- Internal implementation can change without affecting callers
- Prevents unauthorized modification of state
- Reduces coupling between classes

#### **2. Abstraction**

**Definition**: Hiding complex implementation details and exposing only essential features.

**Examples in Project**:

- **Abstract Base Class `SpawnBase`**:

  ```java
  public abstract class SpawnBase {
      protected MapSystem mapSystem;

      public abstract void spawn(); // Forces subclasses to implement
  }
  ```

  Defines the "spawn" concept without specifying how entities are spawned.

- **Interface-Like Usage of SimulationManager**:

  ```java
  // Renderer only needs to read state
  public List<Vehicle> getVehicles() { ... }
  public List<Pedestrian> getPedestrians() { ... }
  public List<TrafficLight> getTrafficLights() { ... }
  ```

  The renderer doesn't know or care about the internal simulation logic—it only needs access to the current state.

- **Map Abstraction**:
  ```java
  public int getTileType(int x, int y) { ... }
  ```
  Clients don't know how the map is stored (2D array) or initialized; they only interact via `getTileType()`.

**Benefits**:

- Simplifies complex systems by providing high-level interfaces
- Allows implementation changes without affecting clients
- Promotes code reuse through polymorphic behavior

#### **3. Inheritance**

**Definition**: Creating new classes based on existing ones to reuse and extend functionality.

**Examples in Project**:

- **JavaFX Framework Inheritance**:

  ```java
  public class App extends Application {
      // Inherits JavaFX lifecycle methods
      @Override
      public void start(Stage stage) { ... }
  }
  ```

- **Spawn System Inheritance Hierarchy**:

  ```
  SpawnBase (abstract)
      ├── SpawnVehicle
      └── SpawnPedestrian
  ```

  Both `SpawnVehicle` and `SpawnPedestrian` inherit the `mapSystem` field and must implement the `spawn()` method.

  ```java
  public class SpawnVehicle extends SpawnBase {
      @Override
      public void spawn() {
          // Vehicle-specific spawning on road tiles (tile type 0)
      }
  }

  public class SpawnPedestrian extends SpawnBase {
      @Override
      public void spawn() {
          // Pedestrian-specific spawning on sidewalk tiles (tile type 2)
      }
  }
  ```

**Benefits**:

- Reduces code duplication through shared base class fields/methods
- Establishes "IS-A" relationships (SpawnVehicle IS-A SpawnBase)
- Enables polymorphic behavior (different spawn implementations)

**Note**: This project uses **inheritance selectively**. Most relationships use **composition** instead (see next section).

#### **4. Polymorphism**

**Definition**: The ability to treat objects of different types through a common interface.

**Examples in Project**:

- **Polymorphic Spawn Calls**:

  ```java
  // In SimulationManager
  private SpawnVehicle spawnVehicleLogic;
  private SpawnPedestrian spawnPedestrianLogic;

  public void spawnVehicle() {
      spawnVehicleLogic.spawn(); // Calls SpawnVehicle's version
  }

  public void spawnPedestrian() {
      spawnPedestrianLogic.spawn(); // Calls SpawnPedestrian's version
  }
  ```

  Both reference `SpawnBase` type conceptually, but invoke different implementations.

- **Enum-Based Polymorphic Behavior**:

  ```java
  // In TrafficLight
  public enum State { RED, YELLOW, GREEN }
  public enum TrafficLightType { VEHICLE, PEDESTRIAN }

  // In Renderer, different rendering based on type:
  if (tl.getType() == TrafficLightType.VEHICLE) {
      // Draw vehicle traffic light
  } else {
      // Draw pedestrian crossing signal
  }
  ```

- **Collection Polymorphism**:
  ```java
  // VehicleManager returns List, hiding HashMap implementation
  public List<Vehicle> getVehicles() {
      return new ArrayList<>(vehicles.values());
  }
  ```

**Benefits**:

- Allows flexible, extensible code (easy to add new spawn types)
- Enables behavior variation without changing client code
- Supports open-closed principle (open for extension, closed for modification)

---

### 4.3 Relationships Between Classes

This section maps the **key relationships** between classes using UML relationship terminology.

#### **Inheritance Relationships** (IS-A)

```
Application (JavaFX)
    ↑
    └── App

SpawnBase (abstract)
    ↑
    ├── SpawnVehicle
    └── SpawnPedestrian

AnimationTimer (JavaFX)
    ↑
    └── (anonymous class in MainController.initializeGameLoop())
```

#### **Composition Relationships** (HAS-A, strong ownership)

Composition indicates that the contained object's lifecycle is managed by the container.

```
SimulationManager
    ├──● VehicleManager
    ├──● PedestrianManager
    ├──● TrafficLightSystem
    ├──● MapSystem
    ├──● SpawnVehicle
    └──● SpawnPedestrian

MainController
    ├──● SimulationManager
    ├──● Renderer
    └──● AnimationTimer (game loop)

UIController
    └──● MainController

MapSystem
    └──● Map

Renderer
    └──● SpriteLoader
```

**Design Justification**: Composition provides tight control over object lifecycles. For example:

- `SimulationManager` creates and owns all managers
- When `SimulationManager` is destroyed or reset, all managers are implicitly managed

#### **Aggregation Relationships** (HAS-A, weak ownership)

Aggregation indicates that the container references objects but doesn't own them.

```
VehicleManager
    ◇── Vehicle (0..*)

PedestrianManager
    ◇── Pedestrian (0..*)

TrafficLightSystem
    ◇── TrafficLight (0..*)
```

**Design Justification**: Managers store entities, but entities can exist independently (in theory). The relationship is managed via `HashMap` and `List` structures.

#### **Dependency Relationships** (USES)

Dependency indicates that one class uses another temporarily (method parameters, local variables).

```
MainController ──→ SimulationManager (receives in constructor)
MainController ──→ Renderer (calls render())

Renderer ──→ SimulationManager (receives in render() method)
Renderer ──→ Vehicle, Pedestrian, TrafficLight (iterates and draws)

SpawnVehicle ──→ VehicleManager (calls addVehicle())
SpawnPedestrian ──→ PedestrianManager (calls addPedestrian())

UIController ──→ Renderer (passes to MainController)
```

#### **Association Summary Table**

| Class A              | Relationship | Class B              | Cardinality | Description              |
| -------------------- | ------------ | -------------------- | ----------- | ------------------------ |
| `SimulationManager`  | Composition  | `VehicleManager`     | 1:1         | Owns and manages         |
| `SimulationManager`  | Composition  | `TrafficLightSystem` | 1:1         | Owns and manages         |
| `VehicleManager`     | Aggregation  | `Vehicle`            | 1:N         | Stores multiple vehicles |
| `TrafficLightSystem` | Aggregation  | `TrafficLight`       | 1:N         | Stores multiple lights   |
| `MainController`     | Dependency   | `SimulationManager`  | 1:1         | Calls `update()`         |
| `Renderer`           | Dependency   | `SimulationManager`  | 1:1         | Reads state via getters  |
| `SpawnVehicle`       | Inheritance  | `SpawnBase`          | 1:1         | IS-A relationship        |

---

### How These Relationships Support System Design

1. **Separation of Concerns**:

   - **Composition** ensures clear ownership boundaries (e.g., `SimulationManager` owns all simulation logic components)
   - **Dependency** keeps layers loosely coupled (e.g., `Renderer` depends on abstractions, not concrete implementations)

2. **Maintainability**:

   - Inheritance is used sparingly, reducing fragile base class problems
   - Composition over inheritance allows easier refactoring

3. **Testability**:

   - Dependencies are injected (e.g., `MainController` receives `Renderer`)
   - Pure Java simulation layer can be tested without JavaFX

4. **Extensibility**:
   - New entity types can be added by extending `SpawnBase`
   - New managers can be added to `SimulationManager` without affecting existing code

---

## 5. Core Features

### Feature 1: Real-Time Simulation Loop

**Description**: The system runs a continuous game loop that updates simulation state and renders visuals at approximately 60 FPS.

**Execution Flow**:

1. User clicks "Start" button → `UIController.onStartClicked()`
2. `MainController.start()` sets `isRunning = true` and starts `AnimationTimer`
3. Every frame (approximately 16ms), `AnimationTimer.handle()` is called:
   - Calls `SimulationManager.update()`
   - Calls `Renderer.render(simulationManager)`
4. Loop continues until user clicks "Stop" or closes application

**Classes Involved**: `MainController`, `UIController`, `SimulationManager`, `Renderer`

---

### Feature 2: Vehicle Spawning and Movement

**Description**: Vehicles are dynamically spawned on road tiles with randomized types and sprites. They move continuously based on direction vectors.

**Execution Flow**:

1. User clicks "Spawn Vehicle" button → `UIController.onSpawnVehicleClicked()`
2. `MainController.spawnVehicle()` → `SimulationManager.spawnVehicle()`
3. `SpawnVehicle.spawn()`:
   - Randomly selects coordinates and checks if tile type is road (0)
   - Creates new `Vehicle` with unique UUID
   - `Vehicle` constructor calls `initializeSprite()`:
     - Randomly selects vehicle type (Car 33%, Truck 33%, Motor 33%)
     - Assigns sprite path based on type and random index
     - Sets dimensions based on vehicle type
   - `VehicleManager.addVehicle(vehicle)` stores vehicle in `HashMap`
4. Every frame, `VehicleManager.update()` calls `vehicle.update()` for all vehicles:
   - Updates position: `x += directionX * speed`, `y += directionY * speed`
5. `Renderer` reads vehicle list and draws each vehicle sprite at its position

**Classes Involved**: `SpawnVehicle`, `Vehicle`, `VehicleManager`, `SimulationManager`, `Renderer`

**Sprite Assets**:

- Cars: `car1.png` to `car18.png` (18 variations)
- Trucks: `car19.png` to `car20.png` (2 variations)
- Motors: `moto1.png` to `moto4.png` (4 variations)

---

### Feature 3: Pedestrian Spawning and Movement

**Description**: Pedestrians spawn on sidewalk tiles and move randomly within the simulation grid.

**Execution Flow**:

1. User clicks "Spawn Pedestrian" button → `UIController.onSpawnPedestrianClicked()`
2. `MainController.spawnPedestrian()` → `SimulationManager.spawnPedestrian()`
3. `SpawnPedestrian.spawn()`:
   - Randomly selects coordinates and checks if tile type is sidewalk (2)
   - Creates new `Pedestrian` with unique UUID
   - `Pedestrian` constructor calls `initializeSprite()`:
     - Randomly selects sprite from 11 variations (`Person1.png` to `Person11.png`)
     - Sets fixed size (36x36 pixels)
   - `PedestrianManager.addPedestrian(pedestrian)` stores pedestrian
4. Every frame, `PedestrianManager.update()` calls `pedestrian.update()`:
   - Random movement: `x += (Math.random() - 0.5) * speed`
5. `Renderer` draws each pedestrian sprite

**Classes Involved**: `SpawnPedestrian`, `Pedestrian`, `PedestrianManager`, `SimulationManager`, `Renderer`

---

### Feature 4: Traffic Light Coordination System

**Description**: Traffic lights cycle through phases (North-South Green, Yellow, All Red, East-West Green, Yellow, All Red) with synchronized timing.

**Execution Flow**:

1. `TrafficLightSystem` constructor:
   - Initializes 8 traffic lights (4 vehicle lights, 4 pedestrian lights) at intersection coordinates
   - Sets initial phase to `NS_GREEN` and timer to 0
2. Every frame, `TrafficLightSystem.update()`:
   - Increments timer
   - **State Machine Logic** (using `switch` on `currentPhase`):
     - **NS_GREEN**: Sets NS vehicle lights to GREEN, EW vehicle lights to RED, EW pedestrian lights to GREEN (walk). Duration: 120 frames (~2 seconds)
     - **NS_YELLOW**: NS vehicle lights to YELLOW. Duration: 30 frames (~0.5 seconds)
     - **ALL_RED_1**: All lights RED for safety buffer. Duration: 20 frames
     - **EW_GREEN**: EW vehicle lights GREEN, NS vehicle lights RED, NS pedestrian lights GREEN
     - **EW_YELLOW**: EW vehicle lights YELLOW
     - **ALL_RED_2**: Safety buffer before cycling back to NS_GREEN
   - Each phase transition resets the timer
3. `Renderer` draws traffic lights with appropriate colors based on `currentState`

**Classes Involved**: `TrafficLightSystem`, `TrafficLight`, `SimulationManager`, `Renderer`

**Pedestrian Light Logic**:

- Pedestrians can walk (GREEN) only when the **perpendicular** vehicle axis is GREEN
- Example: If NS vehicles are GREEN, then EW pedestrians can walk (because EW vehicles are RED)

---

### Feature 5: Map Rendering and Grid System

**Description**: The simulation grid consists of tiles representing roads, sidewalks, and obstacles. The renderer visualizes this grid.

**Execution Flow**:

1. `MapSystem` creates a `Map(50, 36)` grid (50 tiles wide, 36 tiles tall)
2. `Map.initializeDefaultMap()`:
   - Outermost layer: Obstacles (tile type 1)
   - Second layer: Sidewalks (tile type 2)
   - Interior: Roads (tile type 0)
3. `Renderer.render()` calls `drawMap(map)`:
   - Iterates through all tiles
   - Draws rectangles with color based on tile type:
     - Road (0): Dark gray
     - Obstacle (1): Black
     - Sidewalk (2): Light gray
4. Map is drawn first (background), then entities are drawn on top

**Classes Involved**: `Map`, `MapSystem`, `Renderer`

---

### Feature 6: Simulation Control (Start, Stop, Reset)

**Description**: Users can control the simulation state via UI buttons.

**Execution Flow**:

**Start**:

1. `UIController.onStartClicked()` → `MainController.start()`
2. Sets `isRunning = true`
3. Starts `AnimationTimer` game loop

**Stop**:

1. `UIController.onStopClicked()` → `MainController.stop()`
2. Sets `isRunning = false`
3. Stops `AnimationTimer` (but retains simulation state)

**Reset**:

1. `UIController.onResetClicked()` → `MainController.reset()`
2. Calls `stop()` to halt simulation
3. `SimulationManager.reset()`:
   - `VehicleManager.clear()` removes all vehicles
   - `PedestrianManager.clear()` removes all pedestrians
4. `Renderer.clear()` clears the canvas
5. Renders one frame of empty state to show cleared canvas

**Classes Involved**: `UIController`, `MainController`, `SimulationManager`, `VehicleManager`, `PedestrianManager`, `Renderer`

---

## 6. Current Limitations & Improvement Directions

### 6.1 Architectural Limitations

#### **1. Lack of Event-Driven Architecture**

**Current State**: The game loop polls entity states every frame.

**Limitation**: Inefficient for sparse events (e.g., traffic light state changes happen only every ~120 frames but are checked every frame).

**Improvement Direction**: Implement an **Observer Pattern** or **Event Bus**:

- Traffic lights emit state change events
- Vehicles subscribe to nearby traffic light events
- Reduces unnecessary checks and improves performance

#### **2. Tight Coupling Between Renderer and Entity Structure**

**Current State**: `Renderer` directly accesses entity fields like `getX()`, `getY()`, `getSpritePath()`.

**Limitation**: If entity structure changes, `Renderer` must be updated.

**Improvement Direction**:

- Introduce a **DTO (Data Transfer Object)** pattern:
  ```java
  class RenderableEntity {
      double x, y;
      String spritePath;
      double width, height;
  }
  ```
- Entities convert themselves to `RenderableEntity` objects
- `Renderer` only depends on `RenderableEntity`, not concrete entity classes

#### **3. No Dependency Injection Framework**

**Current State**: Dependencies are manually instantiated in constructors:

```java
this.vehicleManager = new VehicleManager();
```

**Limitation**: Hard to test and swap implementations.

**Improvement Direction**: Use **constructor injection** or a lightweight DI framework to allow mock objects during testing.

---

### 6.2 Technical Constraints

#### **1. No Collision Detection**

**Current State**: Entities can overlap and pass through each other.

**Improvement Direction**:

- Implement **bounding box collision detection**
- Use spatial partitioning (e.g., **quadtree**) to optimize collision checks
- Add collision response (vehicles slow down or stop when approaching others)

#### **2. Hardcoded Simulation Parameters**

**Current State**: Speed, spawn logic, traffic light durations are hardcoded.

**Improvement Direction**:

- Externalize configuration to a **properties file** or **JSON configuration**:
  ```json
  {
    "trafficLight": {
      "greenDuration": 120,
      "yellowDuration": 30,
      "redBuffer": 20
    },
    "vehicle": {
      "baseSpeed": 0.05
    }
  }
  ```
- Create a `ConfigurationManager` to load and provide settings

#### **3. Simple Movement Logic**

**Current State**: Vehicles move in straight lines; pedestrians move randomly.

**Improvement Direction**:

- Implement **pathfinding algorithms** (e.g., A\* or Dijkstra) for vehicles to navigate the road network
- Add **waypoint systems** for pedestrians to cross at designated crosswalks
- Implement **steering behaviors** (seek, avoid, follow path)

#### **4. No Persistent State**

**Current State**: Simulation state is lost on exit.

**Improvement Direction**:

- Implement **serialization** using JSON or Java Serialization to save/load simulation states
- Add UI controls for save/load functionality

---

### 6.3 OOP Design Weaknesses

#### **1. Anemic Domain Model**

**Current State**: Entities have little behavior beyond `update()`. Most logic resides in manager classes.

```java
// Current: Vehicle has minimal logic
public void update() {
    x += directionX * speed;
    y += directionY * speed;
}
```

**Improvement Direction**: Move domain logic into entity classes:

```java
// Improved: Vehicle has richer behavior
public void update(Map map, List<Vehicle> nearbyVehicles) {
    if (shouldStopAtTrafficLight()) {
        speed = 0;
    } else if (isCollidingWith(nearbyVehicles)) {
        slowDown();
    } else {
        accelerate();
    }
    move();
}
```

#### **2. Missing Interfaces for Polymorphism**

**Current State**: Managers are concrete classes with no interfaces.

**Improvement Direction**: Define interfaces for extensibility:

```java
interface EntityManager<T> {
    void add(T entity);
    void remove(String id);
    T get(String id);
    List<T> getAll();
    void update();
    void clear();
}

class VehicleManager implements EntityManager<Vehicle> { ... }
```

#### **3. Limited Use of Design Patterns**

**Current State**: Only Template Method and Manager patterns are used.

**Improvement Direction**: Apply additional patterns:

- **Strategy Pattern** for vehicle movement behaviors (different driving styles)
- **State Pattern** for vehicle states (Idle, Moving, Stopped, Turning)
- **Factory Pattern** for entity creation (instead of `new Vehicle(...)`)

---

### 6.4 Refactoring Suggestions

Based strictly on the current codebase:

#### **Refactor 1: Extract TrafficLightSystem Phase Logic**

**Current**: All phase logic is in a large `switch` statement in `update()`.

**Suggested Approach**:

```java
interface TrafficPhase {
    void execute(TrafficLightSystem system);
    TrafficPhase nextPhase(int timer, int duration);
}

class NorthSouthGreenPhase implements TrafficPhase { ... }
class EastWestGreenPhase implements TrafficPhase { ... }
```

**Benefit**: Easier to add new phases and test phase logic independently.

#### **Refactor 2: Introduce a RenderContext**

**Current**: `Renderer` accesses individual entity fields directly.

**Suggested Approach**:

```java
class RenderContext {
    List<RenderableEntity> entities;
    Map map;
}

// Renderer only depends on RenderContext
public void render(RenderContext context) { ... }
```

**Benefit**: Decouples rendering from simulation entity structure.

#### **Refactor 3: Create a Constants/Configuration Class**

**Current**: Magic numbers scattered throughout code (`120`, `30`, `0.05`, etc.)

**Suggested Approach**:

```java
public class SimulationConstants {
    public static final int GREEN_DURATION = 120;
    public static final int YELLOW_DURATION = 30;
    public static final double VEHICLE_BASE_SPEED = 0.05;
}
```

**Benefit**: Centralized configuration, easier to tune parameters.

---

## 7. Conclusion

### Academic Value Summary

This **Traffic Simulation System** project successfully demonstrates the practical application of Object-Oriented Programming principles in a real-world simulation context. The project provides significant academic value in the following areas:

1. **Architectural Design**: Clear demonstration of layered architecture and separation of concerns
2. **OOP Fundamentals**: Concrete examples of all four OOP pillars (Encapsulation, Abstraction, Inheritance, Polymorphism)
3. **Software Engineering Practices**: Use of design patterns, clean code structure, and maintainable architecture
4. **Problem-Solving Skills**: Modeling complex real-world systems (traffic flow) using object-oriented abstractions

---

### Key OOP Concepts Demonstrated

| OOP Concept                | Demonstration in Project                                                                            |
| -------------------------- | --------------------------------------------------------------------------------------------------- |
| **Encapsulation**          | Private fields with public getters in all entity classes; hidden implementation details in managers |
| **Abstraction**            | Abstract `SpawnBase` class; `SimulationManager` provides high-level simulation interface            |
| **Inheritance**            | `SpawnVehicle` and `SpawnPedestrian` extend `SpawnBase`; `App` extends JavaFX `Application`         |
| **Polymorphism**           | Polymorphic `spawn()` calls; enum-based behavioral variation in traffic lights                      |
| **Composition**            | `SimulationManager` composes multiple managers; `MainController` composes simulation and rendering  |
| **Separation of Concerns** | Pure Java simulation layer independent of JavaFX rendering layer                                    |
| **Design Patterns**        | Template Method (spawn system), Manager Pattern (entity management), State Machine (traffic lights) |

---

### Overall Evaluation

#### **Strengths** ✓

- ✓ **Clean Architecture**: Strict separation between simulation logic and UI creates a maintainable, testable codebase
- ✓ **OOP Principles**: All four pillars are demonstrated with practical, real-world examples
- ✓ **Extensibility**: Abstract spawn system and manager pattern make it easy to add new entity types
- ✓ **Performance Optimization**: Sprite caching in `SpriteLoader` demonstrates awareness of performance concerns
- ✓ **Code Organization**: Well-structured package hierarchy with clear responsibilities

#### **Areas for Improvement** ⚠

- ⚠ **Limited Entity Intelligence**: Movement logic is basic; no pathfinding or collision avoidance
- ⚠ **Anemic Domain Model**: Entities have minimal behavior; most logic is in manager classes
- ⚠ **Hardcoded Configuration**: Simulation parameters are not externalized
- ⚠ **Missing Advanced Patterns**: Could benefit from Strategy, Factory, and Observer patterns
- ⚠ **No Testing Framework**: Absence of unit tests for simulation logic

#### **Current Implementation Rating**

From an academic OOP course perspective:

- **Architecture Design**: ★★★★☆ (4/5) - Strong separation of concerns, minor room for improvement in decoupling
- **OOP Principles Application**: ★★★★★ (5/5) - All principles clearly demonstrated with real examples
- **Code Quality**: ★★★★☆ (4/5) - Clean, readable code with minor refactoring opportunities
- **Completeness**: ★★★☆☆ (3/5) - Core features implemented, but several features incomplete (pathfinding, collision detection)
- **Educational Value**: ★★★★★ (5/5) - Excellent learning resource for OOP concepts and design patterns

**Overall**: This is a **solid foundation** for a traffic simulation system that effectively demonstrates OOP principles in action. While the simulation logic can be enhanced with more sophisticated algorithms, the existing architecture provides an excellent base for iterative improvement and serves as a strong example of clean, maintainable object-oriented design.

---

## 8. Running the Application

### System Requirements

- **JDK**: Version 21 or higher
- **Maven**: Version 3.8 or higher
- **Operating System**: macOS, Windows, or Linux with JavaFX support

### Installation and Execution

#### Using Maven (Recommended)

```bash
# Navigate to project root
cd Traffic-simulation-Group1

# Clean and run
mvn clean javafx:run
```

#### Troubleshooting

If you encounter "JavaFX runtime components are missing":

1. Verify JDK 21+ is installed: `java -version`
2. Verify Maven can access JavaFX dependencies: `mvn dependency:tree`
3. Ensure `pom.xml` includes JavaFX Maven plugin configuration

### User Interface Controls

- **Start**: Begin the simulation loop
- **Stop**: Pause the simulation (retains state)
- **Reset**: Clear all entities and reset simulation
- **Spawn Vehicle**: Add a random vehicle to a road tile
- **Spawn Pedestrian**: Add a random pedestrian to a sidewalk tile

---

**End of Documentation**

---

### Appendix: Package Diagram

```
com.traffic.sim
├── app
│   └── App
├── controller
│   ├── MainController
│   └── UIController
├── rendering
│   ├── Renderer
│   └── SpriteLoader
└── simulation
    ├── SimulationManager
    ├── entities
    │   ├── Vehicle
    │   ├── Pedestrian
    │   └── TrafficLight
    ├── managers
    │   ├── VehicleManager
    │   ├── PedestrianManager
    │   ├── TrafficLightSystem
    │   └── MapSystem
    ├── map
    │   └── Map
    └── spawn
        ├── SpawnBase (abstract)
        ├── SpawnVehicle
        └── SpawnPedestrian
```

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-01  
**Author**: Automated Documentation System  
**Purpose**: Academic project documentation for CS3360 - Object-Oriented Programming
