# Order API Test Cases

## Base URL
```
http://localhost:8088/api/v1/orders
```

---

## 1. CREATE ORDER - Tạo đơn hàng mới

### Endpoint
```
POST /api/v1/orders
```

### Request Body
```json
{
    "user_id": 1,
    "fullname": "Nguyễn Văn An",
    "email": "nguyenvanan@gmail.com",
    "phone_number": "0987654321",
    "shipping_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "note": "Giao hàng trong giờ hành chính",
    "shipping_date_expected": "2025-06-01",
    "payment_method": "COD",
    "shipping_method": "STANDARD",
    "coupon_code": "DISCOUNT10",
    "order_items": [
        {
            "product_variant_id": 1,
            "quantity": 2,
            "unit_price": 150000
        },
        {
            "product_variant_id": 3,
            "quantity": 1,
            "unit_price": 280000
        }
    ]
}
```

### Expected Response (200 OK)
```json
{
    "id": 1,
    "order_code": "OF2025053001",
    "user_id": 1,
    "fullname": "Nguyễn Văn An",
    "email": "nguyenvanan@gmail.com",
    "phone_number": "0987654321",
    "shipping_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "note": "Giao hàng trong giờ hành chính",
    "order_date": "2025-05-30T10:30:00",
    "status": "PENDING",
    "subtotal_amount": 580000,
    "shipping_fee": 30000,
    "discount_amount": 58000,
    "total_amount": 552000,
    "shipping_method": "STANDARD",
    "shipping_date_expected": "2025-06-01",
    "payment_method": "COD",
    "payment_status": "UNPAID",
    "order_details": [
        {
            "id": 1,
            "product_variant_id": 1,
            "quantity": 2,
            "unit_price": 150000,
            "total_price": 300000
        },
        {
            "id": 2,
            "product_variant_id": 3,
            "quantity": 1,
            "unit_price": 280000,
            "total_price": 280000
        }
    ]
}
```

---

## 2. CREATE ORDER (Khách hàng chưa đăng ký) - Tạo đơn hàng guest

### Request Body
```json
{
    "fullname": "Trần Thị Bình",
    "email": "tranthibinh@gmail.com",
    "phone_number": "0912345678",
    "shipping_address": "456 Lê Lợi, Quận 3, TP.HCM",
    "note": "",
    "payment_method": "VNPAY",
    "shipping_method": "EXPRESS",
    "order_items": [
        {
            "product_variant_id": 2,
            "quantity": 3,
            "unit_price": 120000
        }
    ]
}
```

---

## 3. GET ORDERS BY USER ID - Lấy đơn hàng theo user

### Endpoint
```
GET /api/v1/orders/user/{user_id}
```

### Example Request
```
GET /api/v1/orders/user/1
```

### Expected Response (200 OK)
```json
[
    {
        "id": 1,
        "order_code": "OF2025053001",
        "user_id": 1,
        "fullname": "Nguyễn Văn An",
        "email": "nguyenvanan@gmail.com",
        "phone_number": "0987654321",
        "shipping_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
        "order_date": "2025-05-30T10:30:00",
        "status": "PENDING",
        "total_amount": 552000,
        "payment_method": "COD",
        "payment_status": "UNPAID"
    },
    {
        "id": 2,
        "order_code": "OF2025053002",
        "user_id": 1,
        "fullname": "Nguyễn Văn An",
        "email": "nguyenvanan@gmail.com",
        "phone_number": "0987654321",
        "shipping_address": "789 Pasteur, Quận 1, TP.HCM",
        "order_date": "2025-05-29T14:20:00",
        "status": "DELIVERED",
        "total_amount": 750000,
        "payment_method": "VNPAY",
        "payment_status": "PAID"
    }
]
```

---

## 4. GET ORDER BY ID - Lấy chi tiết đơn hàng

### Endpoint
```
GET /api/v1/orders/{id}
```

### Example Request
```
GET /api/v1/orders/1
```

### Expected Response (200 OK)
```json
{
    "id": 1,
    "order_code": "OF2025053001",
    "user_id": 1,
    "fullname": "Nguyễn Văn An",
    "email": "nguyenvanan@gmail.com",
    "phone_number": "0987654321",
    "shipping_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "note": "Giao hàng trong giờ hành chính",
    "order_date": "2025-05-30T10:30:00",
    "status": "PENDING",
    "subtotal_amount": 580000,
    "shipping_fee": 30000,
    "discount_amount": 58000,
    "total_amount": 552000,
    "shipping_method": "STANDARD",
    "shipping_date_expected": "2025-06-01",
    "actual_shipping_date": null,
    "tracking_number": null,
    "payment_method": "COD",
    "payment_status": "UNPAID",
    "vnp_txn_ref": null,
    "created_at": "2025-05-30T10:30:00",
    "updated_at": "2025-05-30T10:30:00",
    "order_details": [
        {
            "id": 1,
            "product_variant_id": 1,
            "product_name": "Cá Hồi Na Uy",
            "product_sku": "SKU001",
            "quantity": 2,
            "unit_price": 150000,
            "total_price": 300000
        },
        {
            "id": 2,
            "product_variant_id": 3,
            "product_name": "Tôm Sú",
            "product_sku": "SKU003",
            "quantity": 1,
            "unit_price": 280000,
            "total_price": 280000
        }
    ]
}
```

---

## 5. GET ALL ORDERS (with pagination) - Lấy tất cả đơn hàng

### Endpoint
```
GET /api/v1/orders?page={page}&size={size}
```

### Example Request
```
GET /api/v1/orders?page=0&size=10
```

### Expected Response (200 OK)
```json
{
    "content": [
        {
            "id": 1,
            "order_code": "OF2025053001",
            "user_id": 1,
            "fullname": "Nguyễn Văn An",
            "order_date": "2025-05-30T10:30:00",
            "status": "PENDING",
            "total_amount": 552000,
            "payment_method": "COD"
        },
        {
            "id": 2,
            "order_code": "OF2025053002",
            "user_id": 2,
            "fullname": "Trần Thị Bình",
            "order_date": "2025-05-29T14:20:00",
            "status": "DELIVERED",
            "total_amount": 360000,
            "payment_method": "VNPAY"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "pageNumber": 0,
        "pageSize": 10,
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "totalElements": 15,
    "totalPages": 2,
    "last": false,
    "first": true,
    "numberOfElements": 10,
    "size": 10,
    "number": 0,
    "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
    },
    "empty": false
}
```

---

## 6. UPDATE ORDER - Cập nhật đơn hàng

### Endpoint
```
PUT /api/v1/orders/{id}
```

### Example Request
```
PUT /api/v1/orders/1
```

### Request Body
```json
{
    "fullname": "Nguyễn Văn An",
    "email": "nguyenvanan@gmail.com",
    "phone_number": "0987654321",
    "shipping_address": "789 Nguyễn Huệ, Quận 1, TP.HCM",
    "note": "Giao hàng buổi sáng",
    "shipping_date_expected": "2025-06-02",
    "payment_method": "COD",
    "shipping_method": "EXPRESS",
    "order_items": [
        {
            "product_variant_id": 1,
            "quantity": 3,
            "unit_price": 150000
        }
    ]
}
```

---

## 7. UPDATE ORDER STATUS - Cập nhật trạng thái đơn hàng

### Endpoint
```
PUT /api/v1/orders/{id}/status?status={status}
```

### Example Requests

#### Xác nhận đơn hàng
```
PUT /api/v1/orders/1/status?status=PROCESSING
```

#### Gửi hàng
```
PUT /api/v1/orders/1/status?status=SHIPPED
```

#### Giao hàng thành công
```
PUT /api/v1/orders/1/status?status=DELIVERED
```

#### Hủy đơn hàng
```
PUT /api/v1/orders/1/status?status=CANCELLED_BY_ADMIN
```

### Expected Response (200 OK)
```json
{
    "id": 1,
    "order_code": "OF2025053001",
    "status": "PROCESSING",
    "updated_at": "2025-05-30T11:00:00",
    "tracking_number": "TN2025053001"
}
```

---

## 8. CANCEL ORDER - Hủy đơn hàng

### Endpoint
```
PUT /api/v1/orders/{id}/cancel
```

### Example Request
```
PUT /api/v1/orders/1/cancel
```

### Expected Response (200 OK)
```json
"Đã hủy đơn hàng thành công"
```

---

## Error Responses

### 400 Bad Request - Validation Error
```json
"Validation errors: [Họ tên không được để trống, Email không được để trống]"
```

### 400 Bad Request - Business Logic Error
```json
"Lỗi tạo đơn hàng: Sản phẩm không đủ số lượng trong kho"
```

### 404 Not Found
```json
"Lỗi lấy chi tiết đơn hàng: Không tìm thấy đơn hàng với ID: 999"
```

### 400 Bad Request - Invalid Status Transition
```json
"Lỗi cập nhật trạng thái đơn hàng: Không thể chuyển từ trạng thái DELIVERED sang PENDING"
```

---

## Order Status Values
- `PENDING` - Chờ xử lý
- `PROCESSING` - Đang xử lý
- `SHIPPED` - Đã gửi hàng
- `DELIVERED` - Đã giao hàng
- `CANCELLED_BY_CUSTOMER` - Khách hàng hủy
- `CANCELLED_BY_ADMIN` - Admin hủy

## Payment Methods
- `COD` - Thanh toán khi nhận hàng
- `VNPAY` - Thanh toán VNPay
- `MOMO` - Thanh toán MoMo
- `BANK_TRANSFER` - Chuyển khoản ngân hàng

## Shipping Methods
- `STANDARD` - Giao hàng tiêu chuẩn (3-5 ngày)
- `EXPRESS` - Giao hàng nhanh (1-2 ngày)
- `SAME_DAY` - Giao hàng trong ngày
