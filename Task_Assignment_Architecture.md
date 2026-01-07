# UPDATED ARCHITECTURE & TASK ASSIGNMENT (THEO ROLE MỚI)

**Date:** 2026-01-03
**Status:** Revised based on USER'S TEAM STRUCTURE

---

## 0. UI CONCEPT: RIGHT PANEL DASHBOARD

Để giải quyết bài toán "Hiển thị Data Analysis" cho Người 1 (UI), chúng ta sẽ chia màn hình Right Panel (khoảng 300px chiều rộng) thành 3 phần:

```
┌──────────────────────────────┐
│ [ CONFIGURATION ]            │
│ Green Time (NS): [ 120   ]   │
│ Green Time (EW): [ 120   ]   │
│ [ APPLY & RESET SCENARIO ]   │
├──────────────────────────────┤
│ [ REAL-TIME ANALYSIS ]       │
│ 🏭 Total CO2: 1245.5 g       │
│ ⏱ Avg Wait Time: 12.3 s      │
│ 🏎 Throughput: 45 vehicles    │
│                              │
│ LANE OCCUPANCY (Độ lấp đầy)  │
│ North: █▒▒▒▒▒▒▒▒▒ 12%        │
│ South: █████▒▒▒▒▒ 55%        │
│ East:  ██▒▒▒▒▒▒▒▒ 20%        │
│ West:  ▒▒▒▒▒▒▒▒▒▒ 0%         │
└──────────────────────────────┘
```

**Luồng hoạt động A/B Testing:**

1. User nhập số "60" vào Green Time -> Bấm Apply.
2. Hệ thống Reset toàn bộ xe.
3. Đèn giao thông chạy logic mới (60s xanh).
4. Các chỉ số CO2, Occupancy nhảy số khác -> User so sánh hiệu quả.

---

## 1. PHÂN CÔNG CHI TIẾT (5 MEMBERS)

### 👤 NGƯỜI 1: UI/UX & CONTROL LOGIC (The Observer)

**Nhiệm vụ:** Hiển thị Dashboard (như hình trên) và gửi lệnh điều khiển xuống logic.

- **Files cần sửa/tạo:**

  - `src/.../ui/main.fxml`: Thêm `VBox` (Right Panel) chứa TextField, Label, ProgressBar.
  - `src/.../controller/UIController.java`: Bind biến từ FXML.
  - `src/.../controller/DashboardController.java` (New): Class riêng để update các số liệu thống kê đỡ rối `MainController`.

- **Logic Code:**
  - `onApplyClicked()`: Lấy giá trị từ TextField -> Gọi `MainController.resetSimulation(newConfig)`.
  - `updateDashboard(StatsSnapshot stats)`: Hàm này được gọi mỗi frame (hoặc mỗi giây). Lấy object `stats` (chứa CO2, Occupancy...) để `setText` cho các Label và `setProgress` cho các thanh Bar.

---

### 👤 NGƯỜI 2: ENTITY DEFINITION (The Modeler)

**Nhiệm vụ:** Định nghĩa "Dữ liệu tĩnh" và "Tính chất vật lý cơ bản" của xe/người. Cung cấp dữ liệu để Người 5 tính toán.

- **Files cần sửa/tạo:**

  - `src/.../entities/VehicleType.java` (Enum): `CAR(1x2, emission=0.5)`, `TRUCK(2x3, emission=1.2)`.
  - `src/.../entities/Vehicle.java`:
    - Thêm field `totalCO2`, `idlingSeconds`.
    - Viết hàm `calculateEmissions()`: Nếu `speed == 0` thì cộng `idlingEmission`, ngược lại cộng `movingEmission`.

- **Logic Code:**
  - Bạn không cần viết logic di chuyển phức tạp (Người 5 làm), nhưng bạn phải **cung cấp hàm `isColliding(Rect other)`** hoặc `getBounds()` chuẩn xác để Người 5 dùng.

---

### 👤 NGƯỜI 3: TRAFFIC LIGHT LOGIC (The Regulator)

**Nhiệm vụ:** Làm cho đèn giao thông "thông minh" hơn, nhận tham số từ Người 1.

- **Files cần sửa/tạo:**

  - `src/.../managers/TrafficLightSystem.java`

- **Logic Code:**
  - Sửa Constructor và method `update()` để không dùng `final static` cho thời gian đèn nữa.
  - Viết hàm `setConfig(int greenDuration, int yellowDuration)`: Hàm này sẽ được gọi khi User bấm "Apply".
  - Đảm bảo trạng thái đèn (RED/GREEN) được public hoặc có getter chuẩn để **Người 5** check "Có được đi qua ngã tư không?".

---

### 👤 NGƯỜI 4: MAP & GRID SYSTEM (The Architect)

**Nhiệm vụ:** Xây dựng "Bàn cờ". Quan trọng nhất là định nghĩa các **Zones (Vùng)** để tính toán Occupancy.

- **Files cần sửa/tạo:**

  - `src/.../managers/MapSystem.java`
  - `src/.../map/GridMap.java` (Class mới quản lý mảng 2 chiều `int[][]`).

- **Logic Code:**
  - **Grid Initialization**: Tạo mảng `int[height][width]`.
  - **Define Zones**: Viết hàm `List<Zone> getLaneZones()`.
    - Bạn phải tính tay xem: Làn Bắc là hình chữ nhật từ toạ độ (20,0) đến (24,15)...
    - Tạo ra 4 object `Zone` (x, y, w, h) đại diện cho 4 luồng xe đi vào.
    - Đây là dữ liệu cốt lõi để tính toán "Độ lấp đầy".
  - Cung cấp API: `isWalkable(x, y)` (cho người đi bộ), `isDrivable(x, y)`.

---

### 👤 NGƯỜI 5: SPAWN, PHYSICS, COLLISION & ANALYTICS (The Engine & Brain)

**Nhiệm vụ:** Đây là phần nặng nhất. Bạn điều khiển xe chạy, va chạm, và **tính toán thống kê**.

- **Files cần sửa/tạo:**

  - `src/.../spawn/SpawnVehicle.java`: Random hướng đi (Target Node).
  - `src/.../simulation/managers/VehicleManager.java`: Loop update.
  - `src/.../simulation/analysis/StatisticsManager.java` (New).

- **Logic Code:**
  1.  **Movement & Collision (Physics)**:
      - Lấy Grid từ **Người 4**. Trước khi xe nhích 1 ô, check trên Grid xem có vật cản ko.
      - Check đèn giao thông từ **Người 3**: Nếu đến StopLine & Đèn đỏ -> Dừng.
      - Khi xe di chuyển: Update lại Grid (xoá vết cũ, ghi vết mới).
  2.  **Logic Rẽ (Pathing)**:
      - Khi Spawn: Random luôn `Destination` (Ví dụ: Từ Bắc -> Rẽ trái sang Đông).
      - Khi đến giữa ngã tư: Thay đổi vector hướng đi (`dx, dy`) theo Destination.
  3.  **Statistics Calculation (Analytics)**:
      - Mỗi game tick (hoặc mỗi giây), bạn gọi `statisticsManager.calculate(allVehicles, mapZones)`.
      - Logic tính Occupancy: Loop qua các xe active, xem nó đang nằm đè lên `Zone` nào của Người 4 -> Cộng diện tích vào Zone đó.
      - Kết quả tính toán `StatsSnapshot` sẽ được gửi ra ngoài cho **Người 1** hiển thị.

---

## 2. SƠ ĐỒ PHỐI HỢP (WORKFLOW)

1.  **Start Request**:
    - User bấm Start (Người 1) -> MainController gọi `Loop`.
2.  **Trong Loop (Mỗi Frame)**:
    - **Người 5** update vị trí xe. Nếu vướng xe khác (theo Grid của **Người 4**) thì dừng. Nếu gặp đèn đỏ (**Người 3**) thì dừng.
    - **Người 5** (hoặc module Stats) tính toán lại CO2, Occupancy.
    - **Người 1** (UI Controller) đọc số liệu Stats mới nhất -> Vẽ lên màn hình.
3.  **Config Request**:
    - User nhập Time -> Bấm Apply (Người 1).
    - System gọi `TrafficLightSystem.setConfig` (Người 3).
    - System gọi `Reset` (Clear Map của Người 4, Clear Xe của Người 5).

---

**Lời khuyên:** Người 4 và Người 5 phải ngồi cạnh nhau vì Logic Grid và Logic Di chuyển dính chặt. Người 1 và Người 3 phải thống nhất API cấu hình đèn.
