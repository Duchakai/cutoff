# NGUYỄN ĐÌNH ĐỨC - 25021731

# Báo cáo thí nghiệm: chọn giá trị CUTOFF cho Mergesort

## 1. Mục tiêu

Mergesort cải tiến bằng cách chuyển sang Insertion sort khi đoạn con có độ dài ≤ CUTOFF. Giáo trình dùng CUTOFF = 7 (bản cũ) và 10 (bản mới). Bài này đo thực nghiệm để tìm CUTOFF tốt nhất trên máy dùng để chạy thí nghiệm, và kiểm tra 7 và 10 có thật sự tối ưu hay không.

## 2. Cài đặt

- `src/MergeSort.java`: Mergesort top-down dựng theo bài giảng: mảng `aux` cấp phát một lần, `merge()` copy sang `aux` rồi trộn ngược, `insertionSort()` dạng đổi chỗ liền kề. Có `main()` kiểm thử so với `Arrays.sort` (cỡ 0–10000, 7 giá trị cutoff, nhiều khóa trùng) – tất cả đều đúng.
- Quy ước: đoạn `a[lo..hi]` có `hi-lo+1 ≤ cutoff` thì dùng insertion sort. `cutoff = 1` tương ứng mergesort thuần (không cải tiến).
- Cố ý **không** dùng các cải tiến khác (bỏ merge nếu `a[mid] ≤ a[mid+1]`, đổi vai trò a/aux) để chỉ đo ảnh hưởng của CUTOFF.

## 3. Môi trường đo

| Thành phần   | Giá trị                                |
| ------------ | -------------------------------------- |
| CPU          | Intel Xeon @ 2.10 GHz, 1 vCPU (máy ảo) |
| RAM          | 4 GB (JVM `-Xms2g -Xmx2g`)             |
| JVM          | OpenJDK 21.0.12 (HotSpot, 64-bit)      |
| Hệ điều hành | Ubuntu 24.04                           |

Lưu ý: đây là máy ảo dùng chung nên độ nhiễu khá lớn (xem độ lệch chuẩn bên dưới). Kết quả phản ánh cấu hình này; chạy lại `scripts/run_experiment.sh` trên máy khác có thể cho vùng tối ưu hơi lệch.

## 4. Thiết kế thí nghiệm

**Bộ dữ liệu** (`data/`, sinh bởi `DataGenerator.java`, seed cố định 20241113, mỗi dòng một số `int`):

| File                                   | n       | Mô tả                               |
| -------------------------------------- | ------- | ----------------------------------- |
| random_1000 / 10000 / 100000 / 1000000 | 10³…10⁶ | ngẫu nhiên đều trên toàn miền `int` |
| sorted_1000000                         | 10⁶     | đã sắp xếp tăng                     |
| reversed_1000000                       | 10⁶     | sắp xếp giảm                        |
| nearlysorted_1000000                   | 10⁶     | gần có thứ tự (1% cặp bị hoán đổi)  |
| duplicates_1000000                     | 10⁶     | chỉ 100 giá trị khác nhau           |

**Giá trị CUTOFF khảo sát (24 giá trị):** 1–12 (từng giá trị), 14, 16, 18, 20, 24, 28, 32, 40, 48, 64, 96, 128 (có đủ 7 và 10).

**Quy trình đo** (`Experiment.java`):

1. Khởi động JIT: chạy 3 vòng tất cả cutoff trên random 10⁵ và 10⁶ trước khi đo.
2. Mỗi _lượt_ chạy cả 24 cutoff theo **thứ tự ngẫu nhiên** (xáo mới mỗi lượt), để JIT/GC/dao động tải máy không thiên vị riêng cutoff nào.
3. Mỗi lần sort: `clone()` dữ liệu gốc, đo riêng thời gian `MergeSort.sort` bằng `System.nanoTime()`, sau đó kiểm tra kết quả đã sắp xếp (ngoài đoạn tính giờ).
4. Số lượt: 1000 (n=10³), 300 (10⁴), 60 (10⁵), 25 (random 10⁶), 15 (các dạng dữ liệu còn lại). Tổng cộng 34 680 phép đo thô, lưu ở `results/raw_times.csv`.

**Thống kê** (`analyze.py`): trung bình, median, độ lệch chuẩn, khoảng tin cậy 95% (`1.96·s/√m`); chọn cutoff có median nhỏ nhất; "vùng phẳng" là các cutoff có median chậm hơn tốt nhất ≤ 1%; so sánh **cặp** (cùng lượt) với cutoff 10 bằng t-test cặp và Wilcoxon.

## 5. Kết quả

### 5.1 Dữ liệu ngẫu nhiên, n = 10⁶ (bảng chi tiết)

(thời gian tính bằng ms; "so với c=1" là % thay đổi của median)

| cutoff |     median |   mean |   std | KTC95 ± | so với c=1 |
| -----: | ---------: | -----: | ----: | ------: | ---------: |
|      1 |     121.68 | 123.95 |  5.32 |    2.08 |         0% |
|      2 |     119.73 | 122.03 |  6.15 |    2.41 |      −1.6% |
|      3 |     117.37 | 119.45 |  7.24 |    2.84 |      −3.5% |
|      4 |     114.84 | 117.55 |  7.85 |    3.08 |      −5.6% |
|      5 |     114.44 | 116.21 |  5.46 |    2.14 |      −6.0% |
|      6 |     113.24 | 114.33 |  3.40 |    1.33 |      −6.9% |
|  **7** |     112.22 | 114.95 |  7.24 |    2.84 |      −7.8% |
|      8 |     108.86 | 110.74 |  6.53 |    2.56 |     −10.5% |
|      9 |     107.65 | 109.81 |  7.11 |    2.79 |     −11.5% |
| **10** |     108.45 | 111.35 |  6.78 |    2.66 |     −10.9% |
|     11 |     108.31 | 110.77 |  6.62 |    2.59 |     −11.0% |
|     12 |     109.79 | 111.86 |  6.89 |    2.70 |      −9.8% |
|     14 |     107.75 | 110.42 |  7.31 |    2.87 |     −11.5% |
|     16 |     104.72 | 107.39 |  6.82 |    2.67 |     −13.9% |
|     18 |     105.23 | 106.51 |  4.27 |    1.67 |     −13.5% |
|     20 |     106.70 | 106.71 |  3.83 |    1.50 |     −12.3% |
|     24 |     107.01 | 109.36 |  8.41 |    3.30 |     −12.1% |
| **28** | **104.32** | 106.89 |  6.50 |    2.55 |     −14.3% |
|     32 |     107.07 | 111.17 | 10.10 |    3.96 |     −12.0% |
|     40 |     104.95 | 107.50 |  6.48 |    2.54 |     −13.8% |
|     48 |     107.22 | 108.19 |  4.18 |    1.64 |     −11.9% |
|     64 |     109.91 | 112.03 |  6.86 |    2.69 |      −9.7% |
|     96 |     109.71 | 110.50 |  3.67 |    1.44 |      −9.8% |
|    128 |     131.48 | 133.83 |  5.98 |    2.34 |      +8.0% |

Đồ thị: `results/fig2_random_1e6_ci.png` (trung bình ± KTC 95%).

### 5.2 Cutoff tốt nhất theo từng bộ dữ liệu

| Dữ liệu      |   n | Cutoff tốt nhất (median) | Vùng phẳng (≤1%) | Thời gian tốt nhất (ms) | Cutoff 7 chậm hơn | Cutoff 10 chậm hơn | Lợi so với c=1 |
| ------------ | --: | -----------------------: | :--------------: | ----------------------: | ----------------: | -----------------: | -------------: |
| random       | 10³ |                       16 |      16–24       |                   0.028 |             32.8% |               7.4% |          46.5% |
| random       | 10⁴ |                       32 |      20–64       |                   0.677 |             10.1% |               2.6% |          22.4% |
| random       | 10⁵ |                       28 |      28–40       |                    8.37 |              7.1% |               7.6% |          17.9% |
| random       | 10⁶ |                       28 |      16–40       |                   104.3 |              7.6% |               4.0% |          14.3% |
| duplicates   | 10⁶ |                       20 |      16–28       |                    61.4 |             13.3% |               7.1% |          22.8% |
| reversed     | 10⁶ |                        8 |       8–14       |                    25.0 |              4.0% |               1.3% |          26.0% |
| sorted       | 10⁶ |                       96 |      64–96       |                    21.3 |             29.2% |              22.7% |          41.0% |
| nearlysorted | 10⁶ |                      128 |      (biên)      |                    22.5 |             34.0% |              25.1% |          41.0% |

Đồ thị: `results/fig1_random_by_n.png` (random, các n) và `results/fig3_data_types.png` (các dạng dữ liệu).

### 5.3 Kiểm định so sánh cặp với cutoff = 10 (random)

Trích các giá trị chính (`results/paired_vs_cutoff10.csv`; số âm = nhanh hơn cutoff 10):

|   n | cutoff | chênh lệch TB so với c=10 |         p (t-test cặp) |
| --: | -----: | ------------------------: | ---------------------: |
| 10⁵ |      7 |                     −1.1% | 0.30 (không khác biệt) |
| 10⁵ |     16 |                     −4.5% |                 <0.001 |
| 10⁵ |     28 |                     −7.7% |                 <0.001 |
| 10⁵ |     40 |                     −5.6% |                 <0.001 |
| 10⁶ |      7 |                     +3.2% |                  0.052 |
| 10⁶ |     16 |                     −3.6% |                  0.016 |
| 10⁶ |     20 |                     −4.2% |                  0.006 |
| 10⁶ |     28 |                     −4.0% |                  0.025 |
| 10⁶ |     40 |                     −3.5% |                  0.037 |
| 10⁶ |     64 |                     +0.6% |                   0.72 |

## 6. Phân tích

1. **CUTOFF có tác dụng rõ rệt.** Mergesort thuần (c=1) chậm hơn bản tối ưu 14–46% tùy n; đồ thị có dạng chữ U: giảm nhanh khi cutoff tăng từ 1 lên khoảng 8–16, đi ngang (vùng phẳng), rồi tăng lại khi cutoff quá lớn (ở c=128 với dữ liệu ngẫu nhiên n=10⁶ chậm hơn c=1 khoảng 8%).
2. **Vùng tối ưu trên máy này là khoảng 16–40**, ổn định qua các cỡ n (10³ → 16–24; 10⁴ → 20–64; 10⁵ → 28–40; 10⁶ → 16–40). Trong vùng này các giá trị khác nhau không đáng kể so với độ nhiễu (KTC 95% của từng điểm ≈ ±2,5 ms ở n=10⁶, tức ~2,5%), nên không thể khẳng định một giá trị đơn lẻ (ví dụ 28) thật sự hơn 16 hay 20; chỉ có thể kết luận cả vùng là tốt.
3. **7 và 10 không phải tối ưu trên máy này.** So sánh cặp cho thấy cutoff trong 16–40 nhanh hơn cutoff 10 khoảng 3,5–5% (n=10⁶, p<0,05) và 4,5–7,7% (n=10⁵, p<0,001); cutoff 7 chậm hơn bản tốt nhất 7–8% trên dữ liệu ngẫu nhiên lớn. Cutoff 10 vẫn là lựa chọn hợp lý (chỉ chậm hơn ~4%), tốt hơn hẳn 7.
4. **Vì sao tối ưu lớn hơn 7–10?** Insertion sort (bản đổi chỗ) có chi phí ~n²/4 nhưng hằng số rất nhỏ, truy cập bộ nhớ tuần tự trong cache và không cần copy qua `aux`; ở mức đệ quy thấp nó rẻ hơn việc gọi đệ quy + `merge()`. Giá trị tối ưu phụ thuộc phần cứng/JVM nên giáo trình chỉ đưa con số tham khảo.
5. **Ảnh hưởng của dạng dữ liệu.**
   - _Đã sắp xếp / gần sắp xếp:_ insertion sort chạy O(n) trên đoạn nhỏ, còn mergesort vẫn phải merge đầy đủ, nên cutoff lớn càng có lợi (tối ưu ≥ 64–128). Đây là đặc thù khi chưa có cải tiến "bỏ merge nếu đã có thứ tự".
   - _Đảo ngược:_ trường hợp xấu nhất của insertion sort; tối ưu chỉ 8–14 và cutoff lớn rất tệ (c=64 chậm gấp ~2 lần, c=128 gấp ~4 lần so với tốt nhất).
   - _Nhiều khóa trùng:_ tối ưu 16–28, giống dữ liệu ngẫu nhiên.
6. **Với n nhỏ (10³)** thời gian chỉ ~30 µs nên nhiễu tương đối lớn, nhưng xu hướng vẫn giống n lớn.

## 7. Kết luận

- Trên cấu hình đã đo, **CUTOFF nên chọn trong khoảng 16–32; mình chọn CUTOFF = 20–28** (giữa vùng phẳng; vì khác biệt trong vùng nhỏ hơn nhiễu nên không chọn một con số "chính xác").
- So với giáo trình: nhanh hơn cutoff 10 khoảng 4–5% và cutoff 7 khoảng 7–8% trên dữ liệu ngẫu nhiên lớn; so với không cải tiến nhanh hơn ~14–18% (n ≥ 10⁵).
- Cutoff tối ưu phụ thuộc dạng dữ liệu; 16–28 là lựa chọn an toàn cho dữ liệu ngẫu nhiên, trùng lặp và đảo ngược (trên dữ liệu đảo ngược mức 16–28 chỉ chậm hơn tối ưu ~6%).

## 8. Hạn chế

- Đo trên máy ảo 1 vCPU dùng chung, nhiễu lớn; số lượt đo chỉ 15–25 cho n=10⁶ nên khoảng tin cậy còn rộng.
- Chỉ khảo sát kiểu `int`; với đối tượng `Comparable` (so sánh đắt hơn) vùng tối ưu có thể khác.
- Một phần nhỏ dao động có thể đến từ JIT/GC dù đã khởi động và xáo thứ tự.

## 9. Cách tái lập

```
./scripts/run_experiment.sh      # biên dịch, kiểm thử, sinh dữ liệu (nếu chưa có), đo, phân tích
```

Kết quả: `results/raw_times.csv` (dữ liệu thô), `summary.csv`, `best_cutoff.csv`, `paired_vs_cutoff10.csv`, `fig1…fig3`.
