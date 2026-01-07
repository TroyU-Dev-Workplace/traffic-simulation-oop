# Phân Tích Cấu Trúc Source Code - Traffic Simulation

## 1. Tổng Quan Dự Án

| Thông tin | Chi tiết |
|-----------|----------|
| **Tên dự án** | Traffic Simulation Group 1 |
| **Công nghệ** | Java 21 + JavaFX 21.0.1 |
| **Build tool** | Maven |
| **Kiến trúc** | Layered Architecture (3 tầng) |
| **Main class** | `com.traffic.sim.app.App` |

---

## 2. Cấu Trúc Thư Mục

```
src/main/java/
├── module-info.java                    # Java module declaration
└── com/traffic/sim/
    ├── app/
    │   └── App.java                    # Entry point - JavaFX Application
    ├── controller/
    │   ├── MainController.java         # Điều phối Simulation <-> Renderer
    │   └── UIController.java           # Xử lý sự kiện UI (FXML)
    ├── rendering/
    │   ├── Renderer.java               # Vẽ simulation lên JavaFX Pane
    │   └── SpriteLoader.java           # Load và cache hình ảnh
    └── simulation/
        ├── SimulationManager.java      # Điều phối logic simulation
        ├── entities/
        │   ├── Vehicle.java            # Entity xe cộ
        │   ├── Pedestrian.java         # Entity người đi bộ
        │   └── TrafficLight.java       # Entity đèn giao thông
        ├── managers/
        │   ├── VehicleManager.java     # Quản lý danh sách Vehicle
        │   ├── PedestrianManager.java  # Quản lý danh sách Pedestrian
        │   ├── TrafficLightSystem.java # Hệ thống đèn giao thông
        │   └── MapSystem.java          # Quản lý bản đồ
        ├── map/
        │   └── Map.java                # Data structure của bản đồ
        └── spawn/
            ├── SpawnBase.java          # Abstract class cho spawn logic
            ├── SpawnVehicle.java       # Logic spawn xe
            └── SpawnPedestrian.java    # Logic spawn người đi bộ
```

---

## 3. Kiến Trúc 3 Tầng

### 3.1. Tầng Simulation (Backend - Pure Java)
**Package:** `com.traffic.sim.simulation`

- **Không phụ thuộc JavaFX** - chỉ dùng Java thuần
- Chứa toàn bộ logic mô phỏng giao thông
- Có thể test độc lập mà không cần UI

### 3.2. Tầng Controller (Coordinator)
**Package:** `com.traffic.sim.controller`

- `MainController`: Cầu nối giữa Backend và Frontend
- `UIController`: Xử lý tương tác người dùng từ FXML
- Quản lý game loop (`AnimationTimer`)

### 3.3. Tầng UI/Rendering (Frontend - JavaFX)
**Package:** `com.traffic.sim.rendering` & `com.traffic.sim.controller`

- Vẽ trạng thái simulation lên màn hình
- Load và quản lý sprites/assets

---

## 4. Luồng Hoạt Động (Flow)

```
┌─────────────────────────────────────────────────────────────────────┐
│                           USER INTERACTION                          │
│                    (Click Start/Stop/Reset/Spawn)                   │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         UIController.java                           │
│              (Nhận sự kiện từ FXML, gọi MainController)             │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        MainController.java                          │
│        ┌─────────────────────────────────────────────────────┐      │
│        │              AnimationTimer (Game Loop)             │      │
│        │  ┌───────────────────────────────────────────────┐  │      │
│        │  │  1. simulationManager.update()                │  │      │
│        │  │  2. renderer.render(simulationManager)        │  │      │
│        │  └───────────────────────────────────────────────┘  │      │
│        └─────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────┘
                          │                    │
                          ▼                    ▼
┌────────────────────────────────┐  ┌────────────────────────────────┐
│     SimulationManager.java     │  │        Renderer.java           │
│  ┌──────────────────────────┐  │  │  ┌──────────────────────────┐  │
│  │   vehicleManager.update  │  │  │  │      drawMap()           │  │
│  │   pedestrianManager.update│  │  │  │      drawVehicle()       │  │
│  │   trafficLightSystem.update│ │  │  │      drawPedestrian()    │  │
│  └──────────────────────────┘  │  │  │      drawTrafficLight()   │  │
└────────────────────────────────┘  │  └──────────────────────────┐  │
                                    └────────────────────────────────┘
```

---

## 5. Chi Tiết Các Class

### 5.1. Entry Point

#### `App.java`
```java
// Điểm khởi đầu ứng dụng JavaFX
- Kế thừa: Application
- Load FXML: /ui/main.fxml
- Load CSS: /styles/main.css
- Kích thước cửa sổ: 1280 x 720 (không resize được)
```

### 5.2. Controllers

#### `UIController.java`
```java
// Xử lý sự kiện từ giao diện FXML
Thuộc tính:
- Pane simulationCanvas     // Canvas để vẽ simulation

Phương thức FXML:
- initialize()              // Khởi tạo Renderer và MainController
- onStartClicked()          // Bắt đầu simulation
- onStopClicked()           // Dừng simulation
- onResetClicked()          // Reset simulation
- onSpawnVehicleClicked()   // Spawn xe mới
- onSpawnPedestrianClicked()// Spawn người đi bộ mới
```

#### `MainController.java`
```java
// Điều phối giữa Simulation và Renderer
Thuộc tính:
- SimulationManager simulationManager
- Renderer renderer
- AnimationTimer gameLoop
- boolean isRunning

Phương thức:
- initializeGameLoop()      // Tạo game loop với AnimationTimer
- start()                   // Bắt đầu loop
- stop()                    // Dừng loop
- reset()                   // Reset toàn bộ
- spawnVehicle()            // Gọi spawn xe
- spawnPedestrian()         // Gọi spawn người
```

### 5.3. Rendering

#### `Renderer.java`
```java
// Vẽ simulation state lên JavaFX Pane
Hằng số:
- TILE_SIZE = 20            // 20 pixels mỗi ô grid

Phương thức:
- render(SimulationManager) // Vẽ toàn bộ frame
- drawMap(Map)              // Vẽ bản đồ (load từ /assets/map/map.png)
- drawVehicle(Vehicle)      // Vẽ xe với sprite tương ứng
- drawPedestrian(Pedestrian)// Vẽ người đi bộ
- drawTrafficLight(TrafficLight) // Vẽ đèn giao thông
- drawTrafficLightBox()     // Vẽ hộp đèn với 3 đèn (R/Y/G) hoặc 2 đèn (R/G)
- drawLight()               // Vẽ 1 bóng đèn tròn
- clear()                   // Xóa canvas
```

#### `SpriteLoader.java`
```java
// Load và cache hình ảnh
Thuộc tính:
- Map<String, Image> cache  // HashMap cache sprites

Phương thức:
- getSprite(String path)    // Load sprite từ resources, cache nếu chưa có
```

### 5.4. Simulation Layer

#### `SimulationManager.java`
```java
// Điều phối chính của logic simulation
Thuộc tính:
- VehicleManager vehicleManager
- PedestrianManager pedestrianManager
- TrafficLightSystem trafficLightSystem
- MapSystem mapSystem
- SpawnVehicle spawnVehicleLogic
- SpawnPedestrian spawnPedestrianLogic

Phương thức:
- update()                  // Cập nhật tất cả managers
- spawnVehicle()            // Gọi spawn logic
- spawnPedestrian()         // Gọi spawn logic
- reset()                   // Xóa tất cả entities
- getVehicles()             // Getter cho Renderer
- getPedestrians()          // Getter cho Renderer
- getTrafficLights()        // Getter cho Renderer
- getMapSystem()            // Getter cho Renderer
```

### 5.5. Entities

#### `Vehicle.java`
```java
// Đại diện cho phương tiện giao thông
Thuộc tính:
- String id                 // UUID
- double x, y               // Vị trí (grid coordinates)
- double speed              // Tốc độ di chuyển
- double directionX, directionY // Vector hướng di chuyển
- String vehicleType        // "Car" | "Truck" | "Motor"
- String spritePath         // Đường dẫn sprite
- double width, height      // Kích thước render

Loại xe và kích thước:
┌──────────┬─────────────────────────────────┬───────────────────┐
│   Loại   │           Sprite                │   Kích thước      │
├──────────┼─────────────────────────────────┼───────────────────┤
│   Car    │ /assets/vehicles/car1-18.png    │   80 x 40 px      │
│  Truck   │ /assets/vehicles/car19-20.png   │  150 x 60 px      │
│  Motor   │ /assets/vehicles/moto1-4.png    │   54 x 24 px      │
└──────────┴─────────────────────────────────┴───────────────────┘

Phương thức:
- initializeSprite()        // Random chọn loại xe và sprite
- update()                  // Di chuyển theo direction * speed
- setDirection(dx, dy)      // Đặt hướng di chuyển
```

#### `Pedestrian.java`
```java
// Đại diện cho người đi bộ
Thuộc tính:
- String id                 // UUID
- double x, y               // Vị trí
- double speed              // Tốc độ
- String spritePath         // /assets/pedestrians/Person1-11.png
- double width, height      // 36 x 36 px

Phương thức:
- initializeSprite()        // Random chọn sprite Person1-11
- update()                  // Di chuyển ngẫu nhiên (random walk)
```

#### `TrafficLight.java`
```java
// Đại diện cho đèn giao thông
Enums:
- State: RED, YELLOW, GREEN
- TrafficLightType: VEHICLE, PEDESTRIAN
- Direction: NS (North-South), EW (East-West)

Thuộc tính:
- String id                 // VD: "TL_V_NS_1", "TL_P_EW_2"
- int x, y                  // Vị trí grid
- State currentState        // Trạng thái hiện tại
- TrafficLightType type     // Loại đèn
- Direction direction       // Hướng điều khiển

Phương thức:
- setState(State)           // Đặt trạng thái (được gọi từ TrafficLightSystem)
```

### 5.6. Managers

#### `VehicleManager.java`
```java
// Quản lý tập hợp Vehicle
Cấu trúc dữ liệu: HashMap<String, Vehicle> (key = id)

Phương thức:
- addVehicle(Vehicle)       // Thêm xe
- update()                  // Gọi update() cho tất cả xe
- getVehicles()             // Trả về List<Vehicle>
- getVehicle(String id)     // Lấy xe theo ID
- removeVehicle(String id)  // Xóa xe
- clear()                   // Xóa tất cả
```

#### `PedestrianManager.java`
```java
// Quản lý tập hợp Pedestrian
Cấu trúc dữ liệu: HashMap<String, Pedestrian> (key = id)

Phương thức: (tương tự VehicleManager)
```

#### `TrafficLightSystem.java`
```java
// Hệ thống điều khiển đèn giao thông
Hằng số thời gian (đơn vị frame):
- GREEN_DURATION = 120
- YELLOW_DURATION = 30
- RED_BUFFER = 20

Chu kỳ đèn (Phase):
NS_GREEN → NS_YELLOW → ALL_RED_1 → EW_GREEN → EW_YELLOW → ALL_RED_2 → (lặp lại)

Danh sách đèn được khởi tạo:
┌─────────────┬────────────┬───────────┬─────────────────────────┐
│     ID      │   Loại     │  Hướng    │        Mô tả            │
├─────────────┼────────────┼───────────┼─────────────────────────┤
│ TL_V_NS_1   │  VEHICLE   │    NS     │ Đèn xe hướng Bắc-Nam 1  │
│ TL_V_NS_2   │  VEHICLE   │    NS     │ Đèn xe hướng Bắc-Nam 2  │
│ TL_V_EW_1   │  VEHICLE   │    EW     │ Đèn xe hướng Đông-Tây 1 │
│ TL_V_EW_2   │  VEHICLE   │    EW     │ Đèn xe hướng Đông-Tây 2 │
│ TL_P_NS_1   │ PEDESTRIAN │    NS     │ Đèn người đi bộ NS 1    │
│ TL_P_NS_2   │ PEDESTRIAN │    NS     │ Đèn người đi bộ NS 2    │
│ TL_P_EW_1   │ PEDESTRIAN │    EW     │ Đèn người đi bộ EW 1    │
│ TL_P_EW_2   │ PEDESTRIAN │    EW     │ Đèn người đi bộ EW 2    │
└─────────────┴────────────┴───────────┴─────────────────────────┘

Logic đèn người đi bộ:
- Đèn pedestrian chỉ GREEN khi đèn xe VUÔNG GÓC là RED
- VD: Khi NS_GREEN, đèn pedestrian EW sẽ GREEN
```

#### `MapSystem.java`
```java
// Wrapper class cho Map
- Khởi tạo Map với kích thước 50x36 (tương ứng 1000x720 px với TILE_SIZE=20)
```

### 5.7. Map

#### `Map.java`
```java
// Grid 2D đại diện cho bản đồ
Tile types:
- 0 = Road (đường - xe chạy)
- 1 = Blocked/Obstacle (chướng ngại vật)
- 2 = Sidewalk (vỉa hè - người đi bộ)

Cấu trúc mặc định:
┌─────────────────────────────────────────────┐
│ 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 │  ← Blocked (border)
│ 1 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 1 │  ← Sidewalk
│ 1 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 1 │  ← Road + Sidewalk
│ 1 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 1 │
│ │ ... (Road 0 ở giữa, Sidewalk 2 ở rìa) ... │
│ 1 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 1 │  ← Sidewalk
│ 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 │  ← Blocked (border)
└─────────────────────────────────────────────┘

Phương thức:
- getTileType(x, y)         // Lấy loại tile
- isValid(x, y)             // Kiểm tra tọa độ hợp lệ
```

### 5.8. Spawn Logic

#### `SpawnBase.java` (Abstract)
```java
// Base class cho spawn logic
- Chứa reference đến MapSystem
- Abstract method: spawn()
```

#### `SpawnVehicle.java`
```java
// Spawn xe tại vị trí ngẫu nhiên trên đường (tile type = 0)
- Thử tối đa 10 lần tìm vị trí hợp lệ
- Tạo Vehicle với UUID unique
- Tốc độ mặc định: 0.05
```

#### `SpawnPedestrian.java`
```java
// Spawn người đi bộ tại vị trí ngẫu nhiên trên vỉa hè (tile type = 2)
- Thử tối đa 10 lần tìm vị trí hợp lệ
- Tạo Pedestrian với UUID unique
- Tốc độ mặc định: 0.02
```

---

## 6. Module System

```java
module com.traffic.sim {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.traffic.sim.controller to javafx.fxml;  // Cho phép FXML reflection
    exports com.traffic.sim.app;                       // Export App class
}
```

---

## 7. Design Patterns Sử Dụng

| Pattern | Vị trí áp dụng | Mô tả |
|---------|----------------|-------|
| **MVC** | Toàn bộ project | Model (entities), View (Renderer), Controller |
| **Manager Pattern** | VehicleManager, PedestrianManager | Quản lý collection của entities |
| **Template Method** | SpawnBase (abstract) | Base class định nghĩa interface, subclass implement |
| **Singleton-like** | SpriteLoader cache | Cache sprites để tránh load lại |
| **Game Loop** | MainController (AnimationTimer) | Update → Render cycle |

---

## 8. Các Tính Năng Hiện Tại

- [x] Hiển thị bản đồ từ hình ảnh
- [x] Spawn xe ngẫu nhiên (Car/Truck/Motor với sprite riêng)
- [x] Spawn người đi bộ ngẫu nhiên
- [x] Hệ thống đèn giao thông tự động chuyển phase
- [x] Đèn cho người đi bộ liên kết với đèn xe
- [x] Start/Stop/Reset simulation
- [x] Render sprites với cache

---

## 9. Các Tính Năng Cần Phát Triển

- [ ] Di chuyển xe theo làn đường (path following)
- [ ] Xe dừng khi gặp đèn đỏ
- [ ] Người đi bộ băng qua đường khi đèn xanh
- [ ] Phát hiện va chạm (collision detection)
- [ ] Xe tránh nhau
- [ ] Hiệu ứng xoay xe theo hướng di chuyển
- [ ] UI hiển thị thống kê (số xe, người đi bộ)
- [ ] Điều chỉnh tốc độ simulation

---

## 10. Cách Chạy Project

```bash
# Yêu cầu: JDK 21+, Maven 3.8+

# Clone và chạy
cd traffic-simulation-oop
mvn clean javafx:run
```

---

## 11. Quy Ước Code (Conventions)

| Loại | Quy ước | Ví dụ |
|------|---------|-------|
| Biến | camelCase | `vehicleManager`, `currentState` |
| Class | PascalCase | `VehicleManager`, `TrafficLight` |
| Hằng số | UPPER_SNAKE_CASE | `TILE_SIZE`, `GREEN_DURATION` |
| Package | lowercase | `com.traffic.sim.simulation` |

---

## 12. Git Workflow

```
main (production)
  │
  └── develop (active development)
        │
        ├── feature/<name>    # Tính năng mới
        ├── fix/<name>        # Sửa bug
        ├── hotfix/<name>     # Sửa bug critical
        └── refactor/<name>   # Tái cấu trúc code

Commit message format:
- feat: <mô tả>
- fix: <mô tả>
- refactor: <mô tả>
- chore: <mô tả>
- docs: <mô tả>
```

---

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

*Tài liệu được tạo tự động bởi phân tích source code.*
