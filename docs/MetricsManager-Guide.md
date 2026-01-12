# MetricsManager - Developer Guide

## MetricsManager là gì?

`MetricsManager` là một class chuyên thu thập và tính toán các chỉ số hiệu suất của giao thông trong simulation.

**Vai trò:** Observer (quan sát) - chỉ đọc dữ liệu, không can thiệp vào logic simulation.

## Tại sao cần tách riêng?

- **Separation of Concerns**: Logic simulation (xe chạy, đèn đổi màu) tách biệt với việc đo lường hiệu suất
- **Read-Only**: MetricsManager không bao giờ thay đổi trạng thái của Vehicle, Map, hay TrafficLight
- **Reusability**: Dễ dàng thêm/bớt metrics mà không ảnh hưởng đến simulation core

## MetricsManager lấy dữ liệu từ đâu?

### 1. Map

- Tính diện tích đường có thể chạy (`totalDriveableTiles`)
- Dùng để tính **Occupancy** (mật độ xe)

### 2. Vehicle

Mỗi frame, đọc từ `List<Vehicle>`:

- `getArea()` - diện tích xe
- `isMoving()` - xe đang chạy hay dừng
- `getAccumulatedWaitingTime()` - tổng thời gian chờ
- `getTotalCO2()` - lượng CO₂ thải ra
- `getStopCount()` - số lần dừng

### 3. Simulation Loop

- `SimulationManager.update()` gọi `metricsManager.update(activeVehicles)` mỗi frame
- Khi xe thoát map, gọi `metricsManager.registerVehicleExit(vehicle)`

---

## Các nhóm chỉ số chính

### 1. Efficiency Metrics

#### **Throughput** (Thông lượng)

- **Đo:** Số xe thoát khỏi giao lộ trong 60 frames gần nhất
- **Tính khi nào:** Real-time (mỗi frame)
- **Công thức:** Đếm số xe exit trong sliding window 60 frames
- **Output:** `vehicles/min` - càng cao càng tốt (giao thông thông thoáng)

#### **Occupancy** (Mật độ)

- **Đo:** % diện tích đường bị xe chiếm
- **Tính khi nào:** Real-time (mỗi frame)
- **Công thức:**
  ```
  rawOccupancy = (tổng diện tích xe / diện tích đường) × 100
  occupancy = min(100, rawOccupancy × 3.0)  // Scaled để phản ánh "lane occupancy"
  ```
- **Output:** `%` - >60% = tắc nghẽn

---

### 2. Deadlock / Blockage Metrics

#### **Deadlock Rate** (Tỷ lệ kẹt xe)

- **Đo:** % xe đang dừng (không di chuyển)
- **Tính khi nào:** Real-time (mỗi frame)
- **Công thức:**
  ```
  deadlockRate = (số xe dừng / tổng số xe) × 100
  ```
- **Output:** `%` - >50% = nguy cơ deadlock cao

#### **Blockage Count** (Số lần tắc nghẽn)

- **Đo:** Số lần deadlock kéo dài
- **Tính khi nào:** Real-time
- **Công thức:**
  ```
  Nếu deadlockRate > 50% liên tục trong 300 frames (5 giây):
    blockageCount++
  ```
- **Output:** Số nguyên - đếm số lần tắc nghẽn nghiêm trọng

---

### 3. Waiting / Travel Time Metrics

#### **Average Waiting Time** (Thời gian chờ trung bình)

- **Đo:** Thời gian xe dừng (speed = 0)
- **Tính khi nào:** Summary (khi simulation kết thúc)
- **Công thức:**
  ```
  avgWait = (tổng waiting time của xe đã exit + xe đang active) / tổng số xe
  ```
- **Output:** `seconds` - thời gian chờ trung bình mỗi xe

#### **Average Travel Time** (Thời gian di chuyển)

- **Đo:** Thời gian từ lúc spawn đến lúc exit
- **Tính khi nào:** Summary (chỉ tính xe đã exit)
- **Công thức:**
  ```
  avgTravel = tổng travel time / số xe đã exit
  ```
- **Output:** `seconds` - thời gian hoàn thành hành trình

---

### 4. Environmental & Frustration Metrics

#### **Total CO₂**

- **Đo:** Tổng lượng khí thải
- **Tính khi nào:** Summary
- **Công thức:**
  ```
  totalCO2 = CO₂ của xe đã exit + CO₂ của xe đang active
  ```
- **Output:** `CO₂ units` - tác động môi trường

#### **Frustration Rate** (Tỷ lệ khó chịu)

- **Đo:** % xe bị frustrated (chờ lâu hoặc dừng nhiều)
- **Tính khi nào:** Summary
- **Công thức:**

  ```
  Xe bị frustrated nếu:
    - waitingTime > 60 giây, HOẶC
    - stopCount > 5 lần

  frustrationRate = (số xe frustrated / tổng số xe) × 100
  ```

- **Output:** `%` - chất lượng trải nghiệm người lái

---

## Các Method Chính

### `update(List<Vehicle> activeVehicles)`

Gọi mỗi frame để cập nhật real-time metrics (throughput, occupancy, deadlock).

### `registerVehicleExit(Vehicle v)`

Gọi khi xe thoát map - lưu travel time, waiting time, CO₂, frustration vào summary.

### `reset()`

Xóa toàn bộ metrics, dùng khi reset simulation.

### Getters (Real-time)

- `getThroughput()` - thông lượng hiện tại
- `getOccupancy()` - mật độ hiện tại
- `isCongested()` - có tắc nghẽn không
- `getDeadlockRate()` - tỷ lệ kẹt xe
- `getBlockageCount()` - số lần tắc nghẽn

### Getters (Summary)

- `getFinalAvgWaitingTime()` - thời gian chờ TB (bao gồm cả xe active)
- `getFinalAvgTravelTime()` - thời gian di chuyển TB (chỉ xe đã exit)
- `getFinalTotalCO2()` - tổng CO₂
- `getFinalFrustrationRate()` - tỷ lệ frustrated

---

## Lưu ý quan trọng

✅ **MetricsManager chỉ đọc dữ liệu** - không có setter nào thay đổi Vehicle/Map

✅ **Không ảnh hưởng simulation** - xe vẫn chạy bình thường dù có hay không có MetricsManager

✅ **Summary metrics bao gồm active vehicles** - để tránh mất dữ liệu khi simulation kết thúc sớm (deadlock)

❌ **Không điều khiển simulation** - MetricsManager không quyết định xe dừng/chạy, đèn đổi màu
