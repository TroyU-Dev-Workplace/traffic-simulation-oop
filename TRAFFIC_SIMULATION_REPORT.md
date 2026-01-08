# Traffic Simulation Project Report

## Abstraction

This project implements a comprehensive traffic simulation system using object-oriented programming principles in Java with JavaFX for visualization. The system simulates realistic traffic behavior including vehicles, pedestrians, traffic lights, and intersection management with proper collision detection, turning logic, and environmental impact tracking through CO2 emissions.

## Introduction

The traffic simulation project demonstrates the application of OOP concepts to model complex real-world systems. It provides a platform for analyzing traffic flow patterns, environmental impact, and optimization strategies for urban transportation systems.

### Reason to Choose This Topic as Final Project

1. **Real-world Relevance**: Traffic management is a critical urban planning challenge affecting millions daily
2. **Complex System Design**: Requires integration of multiple interacting components (vehicles, lights, pedestrians)
3. **OOP Demonstration**: Perfect showcase of inheritance, polymorphism, encapsulation, and composition
4. **Performance Analysis**: Enables measurement of system efficiency and environmental impact
5. **Visual Feedback**: Provides immediate visual confirmation of algorithmic decisions
6. **Scalability**: Architecture supports extension with new vehicle types, traffic patterns, and analysis tools

## Overall UML Diagram and User Flow

### Detailed System Architecture UML

## 13. Sơ Đồ Quan Hệ Class (Class Diagram)

```

                                    ┌───────────────┐
                                    │     App       │
                                    │ (JavaFX App)  │
                                    └───────┬───────┘
                                            │ loads FXML
                                            ▼
                                    ┌───────────────┐
                                    │ UIController  │
                                    │   (FXML)      │
                                    └───────┬───────┘
                                            │ creates
                                            ▼
┌───────────────┐               ┌───────────────────────┐
│   Renderer    │◄──────────────│    MainController     │
└───────┬───────┘    renders    │   (AnimationTimer)    │
        │                       └───────────┬───────────┘
        │                                   │ coordinates
        │                                   ▼
        │                       ┌───────────────────────┐
        │                       │  SimulationManager    │
        │                       └───────────┬───────────┘
        │                                   │
        │           ┌───────────────────────┼───────────────────────┐
        │           │                       │                       │
        │           ▼                       ▼                       ▼
        │   ┌───────────────┐   ┌───────────────────┐   ┌───────────────┐
        │   │VehicleManager │   │PedestrianManager  │   │TrafficLight   │
        │   │               │   │                   │   │System         │
        │   └───────┬───────┘   └─────────┬─────────┘   └───────┬───────┘
        │           │                     │                     │
        │           ▼                     ▼                     ▼
        │   ┌───────────────┐   ┌───────────────────┐   ┌───────────────┐
        └──►│   Vehicle     │   │   Pedestrian      │   │ TrafficLight  │
            │   (Entity)    │   │   (Entity)        │   │   (Entity)    │
            └───────────────┘   └───────────────────┘   └───────────────┘
                    ▲                     ▲
                    │                     │
            ┌───────┴───────┐   ┌─────────┴─────────┐
            │ SpawnVehicle  │   │ SpawnPedestrian   │
            └───────────────┘   └───────────────────┘
                    │                     │
                    └──────────┬──────────┘
                               │ extends
                               ▼
                       ┌───────────────┐
                       │  SpawnBase    │
                       │  (abstract)   │
                       └───────────────┘
                               │
                               ▼
                       ┌───────────────┐
                       │   MapSystem   │──────►┌───────┐
                       └───────────────┘       │  Map  │
                                               └───────┘
```

---

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐            │
│ │   App.java      │───▶│ MainController   │───▶│   UIController  │            │
│ │ + main()        │    │ + initialize()   │    │ + handleEvents()│            │
│ │ + start()       │    │ + setupScene()   │    │ + updateUI()    │            │
│ └─────────────────┘    └──────────────────┘    └─────────────────┘            │
└─────────────────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            BUSINESS LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│                    ┌──────────────────────┐                                    │
│                    │   SimulationManager  │                                    │
│                    │ + update()           │                                    │
│                    │ + spawnVehicle()     │                                    │
│                    │ + spawnPedestrian()  │                                    │
│                    │ + reset()            │                                    │
│                    │ + getVehicles()      │                                    │
│                    └─────────┬────────────┘                                    │
│                              │                                                 │
│        ┌─────────────────────┼─────────────────────┐                          │
│        │                     │                     │                          │
│        ▼                     ▼                     ▼                          │
│ ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────┐            │
│ │  VehicleManager  │ │PedestrianManager │ │ TrafficLightSystem  │            │
│ │ + addVehicle()   │ │ + addPedestrian()│ │ + update()          │            │
│ │ + updateWithMap()│ │ + updateWithMap()│ │ + getTrafficLights()│            │
│ │ + getVehicles()  │ │ + getPedestrians()│ │ + setPhase()        │            │
│ │ + removeVehicle()│ │ + clear()        │ │ + addTrafficLight() │            │
│ └─────────┬────────┘ └─────────┬────────┘ └─────────┬───────────┘            │
│           │                    │                    │                        │
│           ▼                    ▼                    ▼                        │
│ ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────┐            │
│ │   SpawnVehicle   │ │ SpawnPedestrian  │ │    TrafficLight     │            │
│ │ + spawn()        │ │ + spawn()        │ │ + update()          │            │
│ │ + pickSpawnPoint()│ │ + findValidTile()│ │ + getCurrentState() │            │
│ │ + setRotation()  │ │ + setDirection() │ │ + setState()        │            │
│ └─────────┬────────┘ └─────────┬────────┘ └─────────────────────┘            │
│           │                    │                                             │
│           ▼                    ▼                                             │
│ ┌──────────────────┐ ┌──────────────────┐                                   │
│ │     Vehicle      │ │    Pedestrian    │                                   │
│ │ + update()       │ │ + update()       │                                   │
│ │ + setDirection() │ │ + move()         │                                   │
│ │ + getRotation()  │ │ + getPosition()  │                                   │
│ │ + executeTurn()  │ │ + crossStreet()  │                                   │
│ │ + getTotalCO2()  │ │ + followPath()   │                                   │
│ └──────────────────┘ └──────────────────┘                                   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                             DATA LAYER                                          │
├─────────────────────────────────────────────────────────────────────────────────┤
│                    ┌──────────────────────┐                                    │
│                    │      MapSystem       │                                    │
│                    │ + getMap()           │                                    │
│                    │ + getRoadTiles()     │                                    │
│                    │ + canVehicleMoveTo() │                                    │
│                    │ + pixelToTile()      │                                    │
│                    └─────────┬────────────┘                                    │
│                              │                                                 │
│                              ▼                                                 │
│                    ┌──────────────────────┐                                    │
│                    │        Map           │                                    │
│                    │ + getTileType()      │                                    │
│                    │ + isIntersection()   │                                    │
│                    │ + isDriveable()      │                                    │
│                    │ + getStopLinePos()   │                                    │
│                    │ + isWalkable()       │                                    │
│                    └──────────────────────┘                                    │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           SUPPORT ENTITIES                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────────┐ │
│ │ Direction   │ │VehicleType  │ │   Region    │ │      TurnDirection          │ │
│ │ UP          │ │ CAR         │ │ NORTH       │ │ STRAIGHT                    │ │
│ │ DOWN        │ │ TRUCK       │ │ SOUTH       │ │ LEFT                        │ │
│ │ LEFT        │ │ MOTORCYCLE  │ │ EAST        │ │ RIGHT                       │ │
│ │ RIGHT       │ │             │ │ WEST        │ │                             │ │
│ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### User Flow Sequence

```
User          UI Controller    SimulationManager    VehicleManager    Vehicle
│                   │                 │                  │             │
│ Start Simulation  │                 │                  │             │
├──────────────────▶│                 │                  │             │
│                   │ initialize()    │                  │             │
│                   ├────────────────▶│                  │             │
│                   │                 │ spawnVehicle()   │             │
│                   │                 ├─────────────────▶│             │
│                   │                 │                  │ new Vehicle │
│                   │                 │                  ├────────────▶│
│                   │                 │                  │             │
│                   │ update()        │                  │             │
│                   ├────────────────▶│                  │             │
│                   │                 │ updateWithMap()  │             │
│                   │                 ├─────────────────▶│             │
│                   │                 │                  │ update()    │
│                   │                 │                  ├────────────▶│
│                   │                 │                  │ ◄───────────│
│                   │                 │ ◄────────────────│             │
│                   │ ◄───────────────│                  │             │
│                   │ render()        │                  │             │
│◄──────────────────│                 │                  │             │
```

## Deep Analysis of Components

### 1. Map System (`Map.java`, `MapSystem.java`)

#### UML Class Diagram - Map System

```
┌─────────────────────────────────────────────────────────────────────┐
│                           MapSystem                                 │
├─────────────────────────────────────────────────────────────────────┤
│ - map: Map                                                          │
│ - random: Random                                                    │
│ - TILE_SIZE: int = 20                                               │
│ - roadTiles: List<int[]>                                            │
│ - sidewalkTiles: List<int[]>                                        │
│ - crosswalkTiles: List<int[]>                                       │
├─────────────────────────────────────────────────────────────────────┤
│ + MapSystem(width: int, height: int)                                │
│ + getMap(): Map                                                     │
│ + getRoadTiles(): List<int[]>                                       │
│ + getSidewalkTiles(): List<int[]>                                   │
│ + getCrosswalkTiles(): List<int[]>                                  │
│ + getRandomRoadTile(): int[]                                        │
│ + canVehicleMoveTo(x: int, y: int): boolean                         │
│ + canPedestrianMoveTo(x: int, y: int): boolean                      │
│ + pixelToTile(pixelX: double, pixelY: double): int[]                │
│ - cacheWalkableTiles(): void                                        │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ 1
                               │ contains
                               │ 1
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                             Map                                     │
├─────────────────────────────────────────────────────────────────────┤
│ + ROAD: int = 0                                                     │
│ + BLOCKED: int = 1                                                  │
│ + SIDEWALK: int = 2                                                 │
│ + CROSSWALK: int = 3                                                │
│ + STOP_LINE: int = 4                                                │
│ + LANE_DIVIDER: int = 5                                             │
│ - grid: int[][]                                                     │
│ - width: int                                                        │
│ - height: int                                                       │
│ - intersectionCenterX: Double                                       │
│ - intersectionCenterY: Double                                       │
├─────────────────────────────────────────────────────────────────────┤
│ + Map(width: int, height: int)                                      │
│ + getTileType(x: int, y: int): int                                  │
│ + isValidCoordinate(x: int, y: int): boolean                        │
│ + isRoad(x: int, y: int): boolean                                   │
│ + isSidewalk(x: int, y: int): boolean                               │
│ + isCrosswalk(x: int, y: int): boolean                              │
│ + isDriveable(x: int, y: int): boolean                              │
│ + isWalkable(x: int, y: int): boolean                               │
│ + isIntersection(x: int, y: int): boolean                           │
│ + getIntersectionCenterX(): double                                  │
│ + getIntersectionCenterY(): double                                  │
│ + getStopLinePosition(region: Region): double[]                     │
│ - initializeDefaultMap(): void                                      │
│ - fillArea(x1: int, y1: int, x2: int, y2: int, tileType: int): void│
│ - ensureIntersectionCenter(): void                                  │
└─────────────────────────────────────────────────────────────────────┘
```

**Purpose**: Defines the simulation environment with different tile types and navigation logic.

**Key Features**:
- **Tile Types**: `ROAD(0)`, `BLOCKED(1)`, `SIDEWALK(2)`, `CROSSWALK(3)`, `STOP_LINE(4)`, `LANE_DIVIDER(5)`
- **Map Layout**: 50×36 grid representing real intersection with proper lane markings
- **Intersection Detection**: Automatic identification of intersection boundaries for traffic light placement
- **Navigation Logic**: Tile-based pathfinding and movement validation

**Code Mapping**:
- Lines 15-22: Tile type constants
- Lines 40-105: Map initialization with realistic intersection layout  
- Lines 208-217: Dynamic intersection detection algorithm
- Lines 345-362: Stop line positioning for traffic light coordination

### 2. Vehicle System (`Vehicle.java`, `VehicleManager.java`, `SpawnVehicle.java`)

#### UML Class Diagram - Vehicle System

```
┌─────────────────────────────────────────────────────────────────────┐
│                        VehicleManager                              │
├─────────────────────────────────────────────────────────────────────┤
│ - vehicles: Map<String, Vehicle>                                    │
│ - trafficLightSystem: TrafficLightSystem                           │
├─────────────────────────────────────────────────────────────────────┤
│ + VehicleManager()                                                  │
│ + VehicleManager(trafficLightSystem: TrafficLightSystem)           │
│ + addVehicle(vehicle: Vehicle): void                                │
│ + updateWithMap(map: Map): void                                     │
│ + getVehicles(): List<Vehicle>                                      │
│ + getVehicle(id: String): Vehicle                                   │
│ + removeVehicle(id: String): void                                   │
│ + clear(): void                                                     │
│ - checkTrafficLightsForVehicleWithMap(v: Vehicle, map: Map): void  │
│ - calculateDistanceToStopLine(v: Vehicle, region: Region): double   │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ 1
                               │ manages
                               │ 0..*
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           Vehicle                                   │
├─────────────────────────────────────────────────────────────────────┤
│ <<enumeration>>                                                     │
│ TurnDirection { STRAIGHT, LEFT, RIGHT }                             │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                        │
│ - x: double                                                         │
│ - y: double                                                         │
│ - speed: double                                                     │
│ - direction: Direction                                               │
│ - type: VehicleType                                                 │
│ - rotation: double                                                  │
│ - plannedTurn: TurnDirection                                        │
│ - hasReachedIntersection: boolean                                   │
│ - isTurning: boolean                                                │
│ - turnProgress: double                                              │
│ - totalCO2: double                                                  │
│ - waitingFrames: int                                                │
│ - originalSpeed: double                                             │
│ - isStoppedAtLight: boolean                                         │
│ - spawnRegion: Region                                               │
├─────────────────────────────────────────────────────────────────────┤
│ + Vehicle(id: String, x: double, y: double, speed: double,          │
│           type: VehicleType, direction: Direction)                  │
│ + update(): void                                                    │
│ + updateWithMap(map: Map): void                                     │
│ + setDirection(dx: double, dy: double): void                        │
│ + setDirection(direction: Direction): void                          │
│ + getRotation(): double                                             │
│ + setRotation(rotation: double): void                               │
│ + setPlannedTurn(turn: TurnDirection): void                         │
│ + getPlannedTurn(): TurnDirection                                   │
│ + respondToTrafficLight(lightState: TrafficLight.State): void       │
│ + getTotalCO2(): double                                             │
│ + hasVehicleAhead(vehicles: Iterable<Vehicle>, distance: double,    │
│                   threshold: double): boolean                       │
│ - moveForward(map: Map): void                                       │
│ - executeTurn(): void                                               │
│ - executeRegularTurn(): void                                        │
│ - completeTurn(): void                                              │
│ - applyTurnRotation(): void                                         │
│ - updateRotationDuringTurn(): void                                  │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │
                               │ uses
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         SpawnVehicle                               │
├─────────────────────────────────────────────────────────────────────┤
│ - vehicleManager: VehicleManager                                    │
│ - regionCounter: static int                                         │
│ - hasStaticCounter: static boolean                                  │
├─────────────────────────────────────────────────────────────────────┤
│ + SpawnVehicle(mapSystem: MapSystem, vehicleManager: VehicleManager)│
│ + spawn(): void                                                     │
│ - getDirectionFromRegion(region: SpawnRegion): Direction            │
│ - assignRandomTurnDirection(vehicle: Vehicle, regionName: String)   │
│ - setVehicleRotation(vehicle: Vehicle, regionName: String): void    │
│ - buildRegionSpawns(intersection: RegionBounds): SpawnRegion[]      │
│ - pickSpawnPoint(region: SpawnRegion): int[]                        │
│ - findEdgeSpawnPoint(minX: int, maxX: int, minY: int, maxY: int,    │
│                      regionName: String): int[]                     │
│ - isCorrectLaneForRegion(x: int, y: int, regionName: String): boolean│
└─────────────────────────────────────────────────────────────────────┘
```

**Purpose**: Manages vehicle lifecycle, movement, and traffic interaction.

**Vehicle Entity Features**:
- **Movement System**: Direction-based movement with speed control
- **Turning Logic**: Support for LEFT, RIGHT, STRAIGHT turns (U-turn removed for safety)
- **Collision Detection**: Forward-looking collision prevention with configurable thresholds
- **Traffic Light Response**: Automatic stopping at red lights with distance-based activation
- **Rotation System**: Proper sprite rotation for visual accuracy
- **Environmental Tracking**: CO2 emission calculation based on movement and idle states

**Code Mapping**:
- Lines 6-8: TurnDirection enum (STRAIGHT, LEFT, RIGHT)
- Lines 442-497: Turn completion logic with 90° rotations only
- Lines 666-742: Collision detection with directional awareness
- Lines 845-858: Rotation application based on final direction

**Spawning Logic**:
- **Regional Spawning**: Vehicles spawn from 4 map edges (North, South, East, West)
- **Initial Orientation**: Vehicles face their movement direction immediately upon spawn
- **Turn Assignment**: Random distribution of turn directions (33.3% each for STRAIGHT, LEFT, RIGHT)

### 3. Traffic Light System (`TrafficLight.java`, `TrafficLightSystem.java`)

#### UML Class Diagram - Traffic Light System

```
┌─────────────────────────────────────────────────────────────────────┐
│                      TrafficLightSystem                            │
├─────────────────────────────────────────────────────────────────────┤
│ - trafficLights: List<TrafficLight>                                 │
│ - phase: int                                                        │
│ - phaseTimer: int                                                   │
│ - PHASE_DURATION: int = 180                                         │
├─────────────────────────────────────────────────────────────────────┤
│ + TrafficLightSystem()                                              │
│ + update(): void                                                    │
│ + getTrafficLights(): List<TrafficLight>                            │
│ + addTrafficLight(light: TrafficLight): void                        │
│ + setPhase(phase: int): void                                        │
│ - initializeTrafficLights(): void                                   │
│ - updatePhase(): void                                               │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ 1
                               │ manages
                               │ 0..*
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        TrafficLight                                │
├─────────────────────────────────────────────────────────────────────┤
│ <<enumeration>>                                                     │
│ State { RED, YELLOW, GREEN }                                        │
│ TrafficLightType { VEHICLE, PEDESTRIAN }                           │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                        │
│ - x: double                                                         │
│ - y: double                                                         │
│ - currentState: State                                               │
│ - type: TrafficLightType                                            │
│ - region: Region                                                    │
│ - stateTimer: int                                                   │
│ - redDuration: int                                                  │
│ - yellowDuration: int                                               │
│ - greenDuration: int                                                │
├─────────────────────────────────────────────────────────────────────┤
│ + TrafficLight(id: String, x: double, y: double,                   │
│                type: TrafficLightType, region: Region)             │
│ + update(): void                                                    │
│ + getCurrentState(): State                                          │
│ + setState(state: State): void                                      │
│ + getType(): TrafficLightType                                       │
│ + getRegion(): Region                                               │
│ + getId(): String                                                   │
│ + getX(): double                                                    │
│ + getY(): double                                                    │
│ + setTimings(red: int, yellow: int, green: int): void              │
│ - transitionState(): void                                           │
└─────────────────────────────────────────────────────────────────────┘
```

**Purpose**: Manages intersection traffic control with realistic timing and coordination.

**Features**:
- **Multi-State Lights**: RED, YELLOW, GREEN states with configurable timing
- **Regional Assignment**: Each light controls specific map regions
- **Vehicle/Pedestrian Types**: Separate control logic for different entity types
- **Distance-Based Activation**: Vehicles respond to lights within 3.0 unit threshold
- **Coordinated Phases**: Prevents conflicting green signals at intersections

### 4. Pedestrian System (`Pedestrian.java`, `PedestrianManager.java`)

#### UML Class Diagram - Pedestrian System

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PedestrianManager                             │
├─────────────────────────────────────────────────────────────────────┤
│ - pedestrians: Map<String, Pedestrian>                             │
├─────────────────────────────────────────────────────────────────────┤
│ + PedestrianManager()                                               │
│ + addPedestrian(pedestrian: Pedestrian): void                      │
│ + updateWithMap(map: Map): void                                     │
│ + getPedestrians(): List<Pedestrian>                               │
│ + getPedestrian(id: String): Pedestrian                            │
│ + removePedestrian(id: String): void                               │
│ + clear(): void                                                     │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ 1
                               │ manages
                               │ 0..*
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Pedestrian                                │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                        │
│ - x: double                                                         │
│ - y: double                                                         │
│ - speed: double                                                     │
│ - direction: Direction                                               │
│ - targetX: double                                                   │
│ - targetY: double                                                   │
│ - spritePath: String                                                │
│ - isWaiting: boolean                                                │
│ - waitTimer: int                                                    │
├─────────────────────────────────────────────────────────────────────┤
│ + Pedestrian(id: String, x: double, y: double, speed: double)      │
│ + update(): void                                                    │
│ + updateWithMap(map: Map): void                                     │
│ + move(): void                                                      │
│ + setTarget(x: double, y: double): void                             │
│ + getX(): double                                                    │
│ + getY(): double                                                    │
│ + getSpeed(): double                                                │
│ + getDirection(): Direction                                         │
│ + getSpritePath(): String                                           │
│ + getWidth(): double                                                │
│ + getHeight(): double                                               │
│ + crossStreet(): boolean                                            │
│ + followPath(): void                                                │
│ - findNearestCrosswalk(map: Map): int[]                             │
│ - canCrossStreet(map: Map): boolean                                 │
│ - initializeSprite(): void                                          │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │
                               │ spawned by
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       SpawnPedestrian                              │
├─────────────────────────────────────────────────────────────────────┤
│ - pedestrianManager: PedestrianManager                             │
├─────────────────────────────────────────────────────────────────────┤
│ + SpawnPedestrian(mapSystem: MapSystem,                             │
│                   pedestrianManager: PedestrianManager)            │
│ + spawn(): void                                                     │
│ - findValidSpawnTile(): int[]                                       │
│ - setRandomDirection(pedestrian: Pedestrian): void                 │
└─────────────────────────────────────────────────────────────────────┘
```

**Purpose**: Simulates pedestrian movement with crosswalk usage and traffic interaction.

**Features**:
- **Walkable Surface Navigation**: Movement restricted to sidewalks and crosswalks
- **Traffic Light Obedience**: Pedestrians wait for appropriate signals
- **Random Movement Patterns**: Realistic pedestrian behavior with directional changes

### 5. Simulation Management (`SimulationManager.java`)

#### UML Class Diagram - Simulation Management

```
┌─────────────────────────────────────────────────────────────────────┐
│                       SimulationManager                            │
├─────────────────────────────────────────────────────────────────────┤
│ - vehicleManager: VehicleManager                                   │
│ - pedestrianManager: PedestrianManager                             │
│ - trafficLightSystem: TrafficLightSystem                           │
│ - mapSystem: MapSystem                                              │
│ - spawnVehicleLogic: SpawnVehicle                                   │
│ - spawnPedestrianLogic: SpawnPedestrian                             │
├─────────────────────────────────────────────────────────────────────┤
│ + SimulationManager()                                               │
│ + update(): void                                                    │
│ + spawnVehicle(): void                                              │
│ + spawnPedestrian(): void                                           │
│ + reset(): void                                                     │
│ + getVehicles(): List<Vehicle>                                      │
│ + getPedestrians(): List<Pedestrian>                               │
│ + getTrafficLights(): List<TrafficLight>                            │
│ + getMapSystem(): MapSystem                                         │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ coordinates
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
┌─────────────────────┐ ┌──────────────────┐ ┌─────────────────────┐
│   VehicleManager    │ │PedestrianManager │ │ TrafficLightSystem  │
│                     │ │                  │ │                     │
│ Manages vehicle     │ │ Manages          │ │ Controls traffic    │
│ lifecycle,          │ │ pedestrian       │ │ signals and         │
│ movement, and       │ │ movement and     │ │ intersection        │
│ traffic response    │ │ crosswalk usage  │ │ coordination        │
└─────────────────────┘ └──────────────────┘ └─────────────────────┘
                               │
                               │ uses
                               ▼
                    ┌──────────────────────┐
                    │      MapSystem       │
                    │                      │
                    │ Provides map data    │
                    │ and navigation       │
                    │ utilities for all    │
                    │ simulation entities  │
                    └──────────────────────┘
```

**Purpose**: Central coordinator orchestrating all simulation components.

**Responsibilities**:
- **Update Coordination**: Synchronized updates of all managers
- **Entity Lifecycle**: Managing spawn, update, and removal of entities
- **State Management**: Providing read-only access to simulation state for rendering
- **Performance Optimization**: Efficient update loops and memory management

## Test Cases the Code Handles

### 1. Map Definition and Validation

**Blocks and Regions**:
- **Building Blocks**: 4 corner regions marked as `BLOCKED` preventing entity passage
- **Road Networks**: Continuous road tiles enabling vehicle movement
- **Sidewalk Systems**: Separate pedestrian pathways with crosswalk connections
- **Intersection Zones**: Automatic detection and management of traffic convergence points

**Test Scenarios**:
- Boundary validation for entity movement
- Tile type transitions and accessibility rules
- Intersection detection accuracy across different map configurations

### 2. Vehicle Logic Comprehensive Testing

**Rotation and Spawning**:
- **Initial Facing**: Vehicles correctly oriented toward movement direction at spawn
- **Turn Execution**: Proper 90° rotations for LEFT/RIGHT turns, 0° for STRAIGHT
- **Spawn Region Coordination**: Vehicles spawned from appropriate map edges with correct initial directions

**Traffic Light Integration**:
- **Distance-Based Response**: Vehicles stop when within 3.0 units of red lights
- **Signal Transitions**: Smooth speed changes during YELLOW phases (30% speed reduction)
- **Post-Intersection Behavior**: Vehicles ignore traffic lights after passing stop lines

**Collision Detection**:
- **Forward Collision Avoidance**: 1.5-unit look-ahead with 0.5-unit lateral tolerance
- **Multi-Directional Checking**: Collision detection considers planned turn directions
- **Traffic Light Interaction**: Vehicles avoid colliding with traffic-light-stopped vehicles

### 3. Performance Metrics and Analysis

**CO2 Emission Tracking**:
- **Real-time Monitoring**: Continuous CO2 accumulation based on vehicle state
- **Type-Based Emissions**: Different emission rates for CAR, TRUCK, MOTORCYCLE
- **Idle vs. Moving**: Separate emission calculations for stopped and moving vehicles

**Traffic Flow Analysis**:
- **Wait Time Tracking**: Measurement of time vehicles spend stopped at traffic lights
- **Intersection Throughput**: Analysis of vehicle processing rate through intersections
- **Congestion Detection**: Monitoring vehicle density to prevent traffic jams

## Scripts for Performance Testing

### CO2 Emission Analysis

The system tracks environmental impact through:
- **Total Emissions**: Cumulative CO2 output across all vehicles
- **Per-Vehicle Analysis**: Individual emission tracking with type-based differentiation
- **Efficiency Metrics**: Ratio of moving vs. idle emission rates
- **Time-Based Reporting**: CO2 per minute calculations for rate analysis

### Traffic Light Performance

**Stop Time Analysis**:
- **Average Wait Duration**: Mean time vehicles spend stopped at red lights
- **Signal Efficiency**: Percentage of time lights optimally serve traffic demand
- **Intersection Utilization**: Analysis of traffic light timing effectiveness

### Road Capacity Management

**Vehicle Density Control**:
- **Spawn Rate Optimization**: Dynamic adjustment to prevent overcrowding
- **Congestion Metrics**: Real-time monitoring of traffic density
- **Flow Rate Analysis**: Vehicles per minute through intersection measurement

## Performance Analysis Conclusions

Based on simulation testing and analysis:

### Environmental Impact

1. **Emission Efficiency**: The system successfully differentiates between moving and idle emissions, providing realistic environmental impact modeling
2. **Vehicle Type Impact**: Trucks produce significantly higher emissions than cars or motorcycles, supporting policy analysis for vehicle restrictions
3. **Traffic Light Optimization**: Proper signal timing reduces idle time, directly correlating with lower CO2 emissions

### Traffic Flow Optimization

1. **Intersection Management**: The 4-way intersection design with proper traffic light coordination prevents gridlock
2. **Turn Distribution**: Equal probability distribution of turns (33.3% each) provides balanced traffic flow
3. **Collision Prevention**: The forward-looking collision detection maintains safe following distances without causing unnecessary delays

### System Scalability

1. **Performance**: The tile-based map system scales efficiently with larger grids
2. **Entity Management**: Object-oriented design supports easy addition of new vehicle types or behaviors
3. **Analysis Tools**: Built-in metrics collection enables continuous performance monitoring

## Limitations

### Current System Constraints

1. **Map Complexity**: Single intersection design limits real-world applicability to complex road networks
2. **AI Behavior**: Vehicles follow simple rule-based logic without adaptive learning or complex decision-making
3. **Pedestrian Integration**: Limited pedestrian-vehicle interaction compared to real-world scenarios
4. **Weather/Time Effects**: No environmental factors affecting visibility, road conditions, or traffic patterns

### Technical Limitations

1. **Scalability**: Performance may degrade with very high entity counts (>100 vehicles simultaneously)
2. **Real-time Constraints**: Fixed update frequency may not accommodate variable simulation speeds
3. **Memory Management**: Long-running simulations may accumulate memory usage without cleanup optimization
4. **Visual Fidelity**: 2D tile-based representation lacks 3D spatial realism

### Future Enhancement Opportunities

1. **Multi-Intersection Networks**: Expand to city-scale simulations with multiple connected intersections
2. **Machine Learning Integration**: Implement adaptive traffic light timing based on traffic patterns
3. **Emergency Scenarios**: Add emergency vehicle priority and route optimization
4. **Data Export**: Enhanced analysis tools with detailed performance reporting and visualization
5. **User Interaction**: Allow real-time parameter adjustment and scenario testing

---

*This report demonstrates the successful implementation of object-oriented principles in creating a comprehensive traffic simulation system with practical applications for urban planning and environmental analysis.*