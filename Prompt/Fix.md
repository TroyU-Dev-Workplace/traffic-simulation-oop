Ok 👍 dưới đây là **PROMPT MÔ TẢ YÊU CẦU ĐIỀU CHỈNH DATA OUTPUT & METRICS**, **không gợi ý cách triển khai**, chỉ tập trung vào **logic hiển thị, thời điểm cập nhật và xác minh công thức**.

---

## Prompt: Traffic Metrics Output Validation & Adjustment

> Trong hệ thống mô phỏng giao thông hiện tại, module **Traffic Metrics & Data Collection** đã được tích hợp và hiển thị trên Right Panel Dashboard. Tuy nhiên, cần rà soát và điều chỉnh lại **logic output của các chỉ số**, nhằm đảm bảo tính chính xác học thuật, đúng ngữ nghĩa mô phỏng và dễ kiểm chứng.
>
> ---
>
> ### 1. Thời điểm cập nhật các chỉ số Summary
>
> - Một số chỉ số mang tính **tổng kết (summary metrics)** hiện đang được hiển thị hoặc cập nhật không đúng thời điểm. Ngoài ra hiện tại chỉ số này còn chưa được tính toán, tôi chưa thấy output sau khi simulation kết thúc.
> - Cụ thể:
>
>   - **Average Waiting Time**
>   - **Frustration Index**
>
> - Yêu cầu:
>
>   - Các chỉ số này **chỉ được tính toán và hiển thị sau khi Auto Simulation kết thúc**
>   - Trong thời gian simulation đang chạy:
>     - Hiển thị trạng thái placeholder (ví dụ: `--`)
>
> - Lý do:
>   - Các chỉ số trên phụ thuộc vào **vòng đời hoàn chỉnh của vehicle** (spawn → wait → pass → despawn)
>   - Việc hiển thị realtime sẽ gây sai lệch và không phản ánh đúng thực tế
>
> ---
>
> ### 2. Frustration Index – Chuẩn hoá vai trò
>
> - **Frustration Index** có bản chất tương tự Average Waiting Time:
>
>   - Là chỉ số đánh giá trải nghiệm sau mô phỏng
>
> - Yêu cầu:
>
>   - Không cập nhật realtime
>   - Chỉ xuất hiện khi simulation kết thúc
>   - Dựa trên tập xe đã hoàn tất vòng đời
>   - Hiển thị placeholder (ví dụ: `--`) trong thời gian simulation đang chạy. Ngoài ra tôi chưa thấy output sau khi simulation kết thúc.
>
> ---
>
> ### 3. Bổ sung nhóm chỉ số Deadlock & Logic Failure
>
> - Hiện tại dashboard **chưa hiển thị nhóm chỉ số Deadlock**, trong khi đây là nhóm chỉ số **rất quan trọng để đánh giá độ an toàn và tính đúng đắn của logic điều phối**, đặc biệt trong các kịch bản giao thông đông đúc.
> - Yêu cầu bổ sung các output liên quan đến:
>
>   - **Blockage Count**: Số lần luồng xe bị đứng yên trong khi đèn xanh kéo dài quá ngưỡng cho phép
>   - **Deadlock Rate**: Tỷ lệ thời gian giao lộ rơi vào trạng thái không có bất kỳ chuyển động hợp lệ nào
>
> - Nhóm chỉ số này cần được thể hiện rõ ràng trên dashboard với ý nghĩa đánh giá **Phase Safety & Logic Robustness**
>
> ---
>
> ### 4. Nhóm hoá chỉ số & tiêu đề hiển thị
>
> - Do hệ thống không chỉ hiển thị các chỉ số đơn lẻ mà đánh giá theo **nhóm chỉ số**, dashboard cần:
>
>   - Chia rõ các nhóm metrics
>   - Có **title nhỏ** đứng trước mỗi nhóm
>
> - Các nhóm chỉ số bao gồm (nhưng không giới hạn):
>
>   - Efficiency Metrics
>   - Safety & Congestion Metrics
>   - Deadlock & Logic Failure Metrics
>   - Experience Metrics
>   - Impact Metrics
>
> - Mục tiêu:
>
>   - Giúp người xem hiểu rõ mỗi chỉ số đang đánh giá khía cạnh nào của hệ thống
>   - Phục vụ tốt cho demo, giải thích và chấm điểm học thuật
>
> ---
>
> ### 5. Xác minh lại công thức của từng chỉ số
>
> - Hiện tại chưa có cơ sở khẳng định rằng toàn bộ các chỉ số output đang được tính toán chính xác.
> - Yêu cầu:
>
>   - Rà soát lại **định nghĩa và công thức** của từng metric
>   - Đảm bảo:
>
>     - Phù hợp với ngữ cảnh mô phỏng giao thông
>     - Không bị bias bởi các thực thể chưa hoàn thành vòng đời
>     - Phân biệt rõ giữa:
>
>       - Realtime metrics
>       - Post-simulation (summary) metrics
>
> ---
>
> ### Mục tiêu tổng thể
>
> - Đảm bảo các chỉ số được hiển thị **đúng thời điểm, đúng ý nghĩa và đúng công thức**
> - Tăng tính thuyết phục học thuật của dashboard
> - Giúp người xem (giảng viên / người đánh giá) hiểu rõ:
>
>   - Vì sao một kịch bản giao thông bị tắc
>   - Vì sao hệ thống bị deadlock
>   - Và mức độ hiệu quả – an toàn – trải nghiệm của ngã tư đang được đánh giá
