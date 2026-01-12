# Mô tả Các Chỉ Số Giao Thông (Traffic Metrics)

Tài liệu này mô tả chi tiết cách tính toán các chỉ số trong hệ thống mô phỏng giao thông sau khi đã được cập nhật logic để phản ánh chính xác hơn tình trạng thời gian thực.

## 1. Efficiency (Hiệu quả)

### Throughput (Lưu lượng)

- **Định nghĩa**: Số lượng phương tiện đã rời khỏi bản đồ (hoàn thành hành trình) trong 60 giây gần nhất.
- **Cách tính**: Hệ thống duy trì một danh sách thời gian rời đi (`exitTimestamps`). Mỗi frame, các mốc thời gian cũ hơn 60s sẽ bị loại bỏ. Total count = số lượng mốc còn lại.
- **Đơn vị**: v/m (vehicles per minute).

### Occupancy (Mật độ chiếm dụng)

- **Định nghĩa**: Phần trăm diện tích đường đang bị chiếm bởi các phương tiện.
- **Cách tính**: `(Tổng diện tích các xe đang hoạt động / Tổng diện tích bản đồ) * 100`.
- **Đơn vị**: %.

## 2. Safety & Congestion (An toàn & Tắc nghẽn)

### Congestion (Trạng thái tắc nghẽn)

- **Định nghĩa**: Trạng thái "Congested" được kích hoạt nếu `Occupancy` vượt quá ngưỡng 80% trong hơn 3 giây liên tục.
- **Trạng thái**: "Normal" hoặc "Congested".

## 3. Deadlock & Logic (Kẹt xe & Lỗi logic)

### Deadlock Rate (Tỷ lệ kẹt xe)

- **Cập nhật Logic**: Trước đây chỉ tính khi _toàn bộ_ xe đứng yên. Hiện tại hệ thống coi là có nguy cơ Deadlock nếu **hơn 50% số lượng xe đang hoạt động bị dừng lại**.
- **Cách tính**: `(Số frame bị deadlock / Tổng số frame đã chạy) * 100`.
- **Ý nghĩa**: Phản ánh tỷ lệ thời gian hệ thống rơi vào trạng thái ùn tắc nghiêm trọng (phần lớn xe không di chuyển).

### Blockage Count (Số lần tắc nghẽn kéo dài)

- **Định nghĩa**: Số lần hệ thống phát hiện trạng thái Deadlock (như định nghĩa trên) kéo dài liên tục quá 5 giây (300 frames).

## 4. Experience (Trải nghiệm người dùng)

**Lưu ý quan trọng**: Các chỉ số này hiện tại là sự kết hợp giữa các xe **đã rời đi** (lịch sử) và các xe **đang hoạt động** (thời gian thực) để phản ánh đúng tình trạng hiện tại.

### Avg Wait (Thời gian chờ trung bình)

- **Cách tính**: `(Tổng thời gian chờ của xe đã đi + Tổng thời gian chờ tích lũy của xe đang đi) / (Số xe đã đi + Số xe đang đi)`.
- **Đơn vị**: Giây (s).
- **Ý nghĩa**: Cho biết trung bình mỗi phương tiện phải đứng yên bao lâu. Nếu tắc đường xảy ra, chỉ số này sẽ tăng ngay lập tức theo thời gian thực.

### Avg Travel (Thời gian di chuyển trung bình)

- **Cách tính**: `Tổng thời gian di chuyển của các xe đã rời đi / Số lượng xe đã rời đi`.
- **Đơn vị**: Giây (s).
- **Lưu ý**: Chỉ tính cho các chuyến đi đã hoàn thành (vì xe đang đi chưa có tổng thời gian cuối cùng).

## 5. Impact (Tác động)

### CO2 (Lượng khí thải)

- **Cách tính**: Tổng tích lũy lượng khí CO2 thải ra từ tất cả các xe (đã đi và đang đi).
  - Xe chạy: Tiêu thụ ít CO2 hơn / frame.
  - Xe dừng (nổ máy): Tiêu thụ CO2 cao hơn / frame.
- **Đơn vị**: kg (đơn vị giả định trong game).

### Frustration (Mức độ ức chế)

- **Định nghĩa**: Tỷ lệ phần trăm tài xế bị "ức chế".
- **Điều kiện ức chế**: Thời gian chờ tích lũy > 60 giây HOẶC số lần dừng xe > 5 lần.
- **Cách tính**: `(Số tài xế ức chế (đã đi + đang đi) / Tổng số tài xế (đã đi + đang đi)) * 100`.
- **Đơn vị**: %.
