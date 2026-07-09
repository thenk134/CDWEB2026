# 📰 BÁO NGƯỜI LAO ĐỘNG 2026 — Hướng Dẫn Sử Dụng

Hệ thống báo điện tử tích hợp đầy đủ: đọc tin tức, đăng bài thành viên, bình luận, tích lũy điểm nhuận bút, đổi điểm lấy tiền và quản trị hệ thống.

---

## 📋 Mục Lục

1. [Yêu Cầu Hệ Thống](#1-yêu-cầu-hệ-thống)
2. [Cài Đặt & Khởi Chạy](#2-cài-đặt--khởi-chạy)
3. [Tài Khoản & Phân Quyền](#3-tài-khoản--phân-quyền)
4. [Hướng Dẫn Độc Giả (Role: USER)](#4-hướng-dẫn-độc-giả-role-user)
5. [Hướng Dẫn Hội Viên (Role: MEMBER)](#5-hướng-dẫn-hội-viên-role-member)
6. [Hệ Thống Điểm Nhuận Bút](#6-hệ-thống-điểm-nhuận-bút)
7. [Hướng Dẫn Quản Trị Viên (Role: ADMIN)](#7-hướng-dẫn-quản-trị-viên-role-admin)
8. [API Backend Endpoints](#8-api-backend-endpoints)

---

## 1. Yêu Cầu Hệ Thống

| Công cụ | Phiên bản tối thiểu |
|---|---|
| Java JDK | 21+ |
| Node.js | 18+ LTS |
| PostgreSQL | 14+ |
| Maven Wrapper | Đã có sẵn trong repo |

---

## 2. Cài Đặt & Khởi Chạy

### 2.1. Cấu Hình Database

1. Mở PostgreSQL và tạo database:
   ```sql
   CREATE DATABASE newsdb;
   ```
2. Cập nhật thông tin kết nối tại `server/news2026/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/newsdb
   spring.datasource.username=postgres
   spring.datasource.password=<MẬT_KHẨU_CỦA_BẠN>
   ```
   > **Lưu ý:** `ddl-auto=update` sẽ tự động tạo tất cả các bảng khi khởi động lần đầu.

### 2.2. Khởi Chạy Backend (Spring Boot)

```powershell
# Windows
cd server/news2026
.\mvnw spring-boot:run
```

```bash
# macOS / Linux
cd server/news2026
./mvnw spring-boot:run
```

Backend khởi động tại: **http://localhost:5000**

Khi khởi động, hệ thống sẽ tự động:
- Tạo tất cả bảng database cần thiết
- Đồng bộ tin tức từ RSS feed vào PostgreSQL

### 2.3. Khởi Chạy Frontend (Vue 3)

```bash
cd client
npm install
npm run dev
```

Truy cập ứng dụng tại: **http://localhost:5173**

---

## 3. Tài Khoản & Phân Quyền

| Role | Mô tả | Cách lấy |
|---|---|---|
| `USER` | Độc giả thường | Đăng ký tự do trên giao diện |
| `MEMBER` | Hội Viên — có thể đăng bài, tích lũy điểm | Nâng cấp bằng PayPal ($9.99 USD) |
| `ADMIN` | Quản trị viên | Đăng ký với username **`admin`** |

### Tạo Tài Khoản Admin

1. Truy cập `/register`
2. Nhập **username = `admin`** (không phân biệt hoa/thường)
3. Điền email và mật khẩu
4. Hệ thống **tự động** cấp quyền `ADMIN`

---

## 4. Hướng Dẫn Độc Giả (Role: USER)

### 4.1. Đọc Tin Tức

- Trang chủ: Hiển thị tin mới nhất từ tất cả chuyên mục
- Thanh điều hướng: Chọn chuyên mục (**Thời sự**, **Việc làm**, **Pháp luật**, **Bảo hiểm**, **Công đoàn**, **Sức khỏe**)
- Bài Hội Viên (🔒): Cần đăng ký **MEMBER** mới đọc được nội dung đầy đủ

### 4.2. Bình Luận Bài Viết

1. Mở bài viết chi tiết
2. Cuộn xuống phần **Bình luận & Tranh luận**
3. Chọn **Đồng ý** hoặc **Phản đối**
4. Gõ nội dung và nhấn **Gửi bình luận**

> **Lưu ý:** Cần đăng nhập để bình luận.

### 4.3. Ủng Hộ Tòa Soạn

1. Mở bài viết bất kỳ
2. Nhấn nút **❤ Ủng hộ Tòa soạn** ở cuối bài
3. Chọn mức tiền ($1 / $2 / $5 / $10) hoặc nhập tuỳ ý
4. Nhập lời nhắn kèm (tuỳ chọn)
5. Nhấn **Xác nhận ủng hộ** — giao dịch được lưu trực tiếp (giả lập PayPal)

### 4.4. Lưu Bài Yêu Thích (Bookmark)

- Nhấn icon 🔖 trên thẻ tin hoặc đầu bài viết
- Xem danh sách đã lưu tại **Tài khoản → Bài đã lưu**

### 4.5. Nâng Cấp Lên Hội Viên

1. Vào **Tài khoản → Nâng Cấp Hội Viên**
2. Xem quyền lợi
3. Nhấn **Thanh Toán $9.99 qua PayPal**
4. Hoàn thành thanh toán trên cửa sổ PayPal
5. Tài khoản được nâng cấp **ngay lập tức** sau khi PayPal xác nhận

---

## 5. Hướng Dẫn Hội Viên (Role: MEMBER)

Hội Viên có toàn bộ quyền của Độc giả, **cộng thêm**:

### 5.1. Đọc Bài Viết Hội Viên (Exclusive)

- Bài có nhãn 🔒 **"Hội Viên"** hiển thị đầy đủ nội dung
- Bài thường hiển thị như người dùng thông thường

### 5.2. Đăng Bài Viết (Quan điểm - Tranh luận)

1. Vào **Tài khoản → Đăng bài viết** hoặc nhấn **+ Viết bài**
2. Điền **Tiêu đề**, **Mô tả ngắn**, **Nội dung** đầy đủ
3. Thêm **URL hình ảnh** (tuỳ chọn)
4. Nhấn **Gửi bài để duyệt**
5. Bài sẽ được Admin xét duyệt và đăng lên chuyên mục **Quan điểm - Tranh luận**

### 5.3. Ủng Hộ Tác Giả

1. Mở bài viết của Hội Viên khác
2. Nhấn nút **💝 Ủng hộ tác giả @username**
3. Chọn mức tiền và xác nhận
4. Giao dịch được ghi nhận, tác giả nhận điểm thưởng

### 5.4. Yêu Cầu Rút Tiền Nhuận Bút

1. Vào **Tài khoản → Rút tiền nhuận bút**
2. Nhập số điểm muốn rút
3. Chọn phương thức nhận: **Chuyển khoản ngân hàng** hoặc **MoMo**
4. Nhập thông tin tài khoản nhận tiền
5. Nhấn **Gửi yêu cầu rút tiền**
6. Admin sẽ xét duyệt và chuyển tiền thực tế (tỷ lệ: **1 điểm = 1.000đ**)

---

## 6. Hệ Thống Điểm Nhuận Bút

### 6.1. Cách Tích Lũy Điểm

| Hành động | Điểm thưởng |
|---|---|
| Bài viết đạt **10 lượt xem** | +1.0 điểm |
| Nhận **bình luận Đồng ý** từ độc giả | +0.3 điểm |
| Nhận **bình luận Phản đối** từ độc giả | -0.7 điểm |
| Nhận **ủng hộ** từ độc giả | Điểm tính theo số tiền |

### 6.2. Quy Tắc Chống Gian Lận

- **Tác giả tự bình luận bài mình:** 0 điểm (không được tính)
- **Độc giả bình luận trùng lặp:** Chỉ tính điểm cho lần bình luận **đầu tiên** trên mỗi bài

### 6.3. Quy Đổi Điểm Sang Tiền

- **1 điểm = 1.000 VNĐ**
- Yêu cầu tối thiểu: Phải có số điểm dương mới được rút
- Thanh toán qua: Chuyển khoản ngân hàng hoặc MoMo

---

## 7. Hướng Dẫn Quản Trị Viên (Role: ADMIN)

Truy cập trang quản trị tại: **http://localhost:5173/admin**

### 7.1. Bảng Điều Khiển (Dashboard)

- Tổng số tin bài hệ thống
- Số bài viết thành viên **chờ duyệt**
- Số lượng **Hội Viên VIP** hiện tại
- Số yêu cầu rút tiền **chờ xử lý**
- Số dư quỹ vận hành (Thu nhập − Chi nhuận bút)
- Danh sách nhanh các công việc cần xử lý khẩn

### 7.2. Quản Lý Tin Tức Hệ Thống

1. Chọn menu **📁 Quản Lý Tin Tức** ở sidebar
2. Xem danh sách toàn bộ bài viết hệ thống
3. Tìm kiếm theo **từ khóa** hoặc lọc theo **chuyên mục**
4. **Sửa bài:** Nhấn ✏ — Chỉnh sửa tiêu đề, mô tả, nội dung, hình ảnh
5. **Xóa bài:** Nhấn 🗑 — Xác nhận để xóa vĩnh viễn

> Tạo bài mới: Nhấn **+ Đăng bài viết mới** (điều hướng đến trang soạn thảo)

### 7.3. Duyệt Bài Thành Viên

1. Chọn menu **✍ Duyệt Bài Thành Viên** ở sidebar
2. Xem danh sách bài gửi đang **Chờ duyệt** (status = 0)
3. Nhấn **Xem & Duyệt** để đọc nội dung đầy đủ
4. Chọn **✅ Phê duyệt** (đăng bài lên) hoặc **⛔ Đánh dấu vi phạm** (ẩn bài)

### 7.4. Quản Lý Thành Viên

1. Chọn menu **👥 Quản Lý Thành Viên** ở sidebar
2. Xem toàn bộ danh sách người dùng với **username**, **email**, **role**, **điểm**
3. Tìm kiếm theo username hoặc email
4. **Cấp quyền MEMBER:** Nhấn **Cấp VIP** — Nâng người dùng từ `USER` lên `MEMBER`
5. **Thu hồi quyền:** Nhấn **Thu hồi VIP** — Hạ từ `MEMBER` xuống `USER`

### 7.5. Xử Lý Yêu Cầu Rút Tiền

1. Chọn menu **💸 Yêu Cầu Rút Tiền** ở sidebar
2. Xem danh sách đầy đủ: Mã rút, Thành viên, Số điểm, Số tiền quy đổi, Kênh nhận tiền
3. **Phê duyệt (✅ Duyệt):** Xác nhận đã chuyển tiền thực tế
4. **Từ chối (❌ Từ chối):** Hoàn lại điểm vào tài khoản thành viên

### 7.6. Báo Cáo Doanh Thu

1. Chọn menu **📈 Báo Cáo Doanh Thu** ở sidebar
2. Xem **KPI tổng quan:** Tổng thu VIP, Tổng ủng hộ, Tổng đã chi nhuận bút, Số dư quỹ
3. **Biểu đồ tròn:** Cơ cấu nguồn thu (VIP vs Ủng hộ)
4. **Biểu đồ cột:** So sánh Thu − Chi theo từng tháng (6 tháng gần nhất)
5. **Nhật ký giao dịch:** Xem chi tiết từng lượt đăng ký VIP và từng giao dịch ủng hộ
6. Nhấn **📥 Xuất báo cáo** để tải file CSV

---

## 8. API Backend Endpoints

### Auth

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập |
| GET | `/api/auth/profile` | Lấy thông tin tài khoản |

### Tin Tức

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/news` | Danh sách tin tức |
| GET | `/api/news/{id}` | Chi tiết bài viết |
| POST | `/api/news/{id}/comment` | Đăng bình luận |
| POST | `/api/news/{id}/view` | Tăng lượt xem |

### Admin

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/admin/news` | Danh sách tất cả tin |
| POST | `/api/admin/news` | Tạo bài mới |
| PUT | `/api/admin/news/{id}` | Cập nhật bài |
| DELETE | `/api/admin/news/{id}` | Xóa bài |
| GET | `/api/admin/news/pending` | Bài chờ duyệt |
| PUT | `/api/admin/news/{id}/review` | Duyệt/Vi phạm bài |
| GET | `/api/admin/users` | Danh sách người dùng |
| PUT | `/api/admin/users/{id}/role` | Thay đổi quyền |

### Điểm & Tài Chính

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/points/balance` | Số dư điểm |
| POST | `/api/points/withdraw` | Gửi yêu cầu rút tiền |
| GET | `/api/points/withdrawals` | Lịch sử rút tiền |
| GET | `/api/admin/revenue/stats` | Thống kê doanh thu |
| GET | `/api/admin/revenue/details` | Chi tiết giao dịch |
| PUT | `/api/admin/payouts/{id}/approve` | Duyệt rút tiền |
| PUT | `/api/admin/payouts/{id}/reject` | Từ chối rút tiền |

### Thanh Toán & Ủng Hộ

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/payment/create` | Tạo thanh toán PayPal |
| GET | `/api/payment/success` | Xử lý thành công |
| POST | `/api/donations` | Gửi ủng hộ tòa soạn/tác giả |

---

## 📁 Cấu Trúc Dự Án

```
CDWEB2026/
├── client/                          # Vue 3 + Vite Frontend
│   ├── src/
│   │   ├── pages/                   # Các trang chính
│   │   │   ├── Home.vue             # Trang chủ
│   │   │   ├── NewsDetail.vue       # Chi tiết bài viết + Bình luận
│   │   │   ├── AccountManagement.vue# Quản lý tài khoản & Nhuận bút
│   │   │   ├── AdminDashboard.vue   # Trang quản trị tổng hợp
│   │   │   ├── AdminRevenue.vue     # Báo cáo doanh thu
│   │   │   └── ...
│   │   ├── router/index.js          # Định tuyến Vue Router
│   │   └── utils/toast.js           # Thông báo toast
│   └── package.json
│
└── server/news2026/                  # Spring Boot Backend
    └── src/main/java/com/news2026/
        ├── controller/
        │   ├── AuthController.java   # Xác thực người dùng
        │   ├── NewsController.java   # Tin tức & Bình luận
        │   ├── AdminNewsController.java # Quản trị tin tức
        │   ├── AdminUserController.java # Quản trị người dùng
        │   ├── PointsController.java    # Điểm nhuận bút
        │   └── PaymentController.java   # Thanh toán PayPal
        ├── entity/                   # JPA Entities (User, Article, ...)
        ├── repository/               # Spring Data Repositories
        └── service/                  # Business Logic
```

---

*© 2026 Báo Người Lao Động — Hệ thống báo điện tử tích hợp.*
