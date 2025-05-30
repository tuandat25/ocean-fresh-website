# Order API Testing Guide

## Tổng quan
Hướng dẫn này cung cấp các test cases chi tiết để kiểm tra API Order Management của hệ thống OceanFresh E-commerce.

## Chuẩn bị

### 1. Environment Setup
- **Base URL**: `http://localhost:8088/api/v1`
- **Database**: Đảm bảo database đã được setup với sample data
- **Authentication**: Cần JWT token cho các endpoint cần authentication

### 2. Sample Data Required
Trước khi test, đảm bảo database có:
- Ít nhất 1 User với ID = 1
- Ít nhất 3 ProductVariant với stock > 0
- Ít nhất 1 Coupon active với code "NEWCUSTOMER10"

### 3. Tools
- **Postman**: Import file `OceanFresh-Order-API.postman_collection.json`
- **cURL**: Sử dụng các command trong phần cURL Examples
- **JSON Test Cases**: File `order-api-test-cases.json` chứa detailed expected responses

## API Endpoints

### 1. POST /api/v1/orders - Tạo đơn hàng

#### Success Cases

**Case 1: Registered User Order**
```json
{
  "userId": 1,
  "fullname": "Nguyễn Văn An",
  "email": "nguyenvanan@gmail.com",
  "phoneNumber": "0987654321",
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "note": "Giao hàng vào buổi sáng",
  "shippingMethod": "STANDARD",
  "shippingDateExpected": "2025-06-02",
  "paymentMethod": "COD",
  "couponCode": "NEWCUSTOMER10",
  "orderItems": [
    {
      "productVariantId": 1,
      "quantity": 2,
      "unitPrice": 25000
    }
  ]
}
```

**Case 2: Guest User Order**
```json
{
  "userId": null,
  "fullname": "Khách Vãng Lai",
  "email": "guest@example.com",
  "phoneNumber": "0912345678",
  "shippingAddress": "456 Đường XYZ, Quận 2, TP.HCM",
  "paymentMethod": "VNPAY",
  "orderItems": [
    {
      "productVariantId": 1,
      "quantity": 1,
      "unitPrice": 25000
    }
  ]
}
```

#### Error Cases

**Insufficient Stock**
```json
{
  "userId": 1,
  "fullname": "Nguyễn Văn An",
  "email": "nguyenvanan@gmail.com",
  "phoneNumber": "0987654321",
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "paymentMethod": "COD",
  "orderItems": [
    {
      "productVariantId": 1,
      "quantity": 1000,
      "unitPrice": 25000
    }
  ]
}
```

**Invalid Coupon**
```json
{
  "userId": 1,
  "fullname": "Nguyễn Văn An",
  "email": "nguyenvanan@gmail.com",
  "phoneNumber": "0987654321", 
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "paymentMethod": "COD",
  "couponCode": "INVALID_COUPON",
  "orderItems": [
    {
      "productVariantId": 1,
      "quantity": 1,
      "unitPrice": 25000
    }
  ]
}
```

### 2. GET /api/v1/orders/{id} - Lấy đơn hàng theo ID

```bash
curl -X GET "http://localhost:8088/api/v1/orders/1" \
  -H "Authorization: Bearer {jwt_token}"
```

### 3. GET /api/v1/orders/code/{orderCode} - Lấy đơn hàng theo mã

```bash
curl -X GET "http://localhost:8088/api/v1/orders/code/OF20250531011730" \
  -H "Authorization: Bearer {jwt_token}"
```

### 4. PUT /api/v1/orders/{id}/status - Cập nhật trạng thái

```json
{
  "status": "PROCESSING"
}
```

Valid transitions:
- PENDING → PROCESSING, CANCELLED_BY_CUSTOMER, CANCELLED_BY_ADMIN
- PROCESSING → SHIPPED, CANCELLED_BY_ADMIN  
- SHIPPED → DELIVERED
- DELIVERED → RETURNED

### 5. GET /api/v1/orders/user/{userId} - Lấy đơn hàng của user

```bash
curl -X GET "http://localhost:8088/api/v1/orders/user/1" \
  -H "Authorization: Bearer {jwt_token}"
```

### 6. GET /api/v1/orders - Lấy tất cả đơn hàng (Admin)

```bash
curl -X GET "http://localhost:8088/api/v1/orders?page=0&size=10&sort=orderDate,desc" \
  -H "Authorization: Bearer {admin_jwt_token}"
```

### 7. PUT /api/v1/orders/{id}/cancel - Hủy đơn hàng

```json
{
  "reason": "Khách hàng đổi ý"
}
```

## Testing Scenarios

### Scenario 1: Complete Order Flow
1. Create order (POST /orders)
2. Get order by ID (GET /orders/{id})
3. Update status to PROCESSING (PUT /orders/{id}/status)
4. Update status to SHIPPED (PUT /orders/{id}/status)
5. Update status to DELIVERED (PUT /orders/{id}/status)

### Scenario 2: Order Cancellation Flow
1. Create order (POST /orders)
2. Cancel order (PUT /orders/{id}/cancel)
3. Verify inventory restored
4. Try to update status (should fail)

### Scenario 3: Inventory Management
1. Check product variant stock
2. Create order with quantity
3. Verify stock decreased
4. Cancel order
5. Verify stock restored

### Scenario 4: Coupon Usage
1. Create order with valid coupon
2. Verify discount applied
3. Check coupon usage count increased
4. Try same coupon if usage limit reached

## Error Handling Test Cases

### 1. Validation Errors (400)
- Empty required fields
- Invalid email format
- Invalid phone number format
- Empty order items array

### 2. Not Found Errors (404)
- Non-existent order ID
- Non-existent order code
- Non-existent user ID
- Non-existent product variant
- Non-existent coupon code

### 3. Business Logic Errors (400)
- Insufficient stock
- Invalid status transition
- Expired coupon
- Inactive coupon
- Order amount below coupon minimum
- Cannot cancel delivered order

### 4. Server Errors (500)
- Database connection issues
- Unexpected exceptions

## Expected Response Format

### Success Response
```json
{
  "status": "SUCCESS",
  "message": "Thành công message",
  "data": {
    // Response data object
  }
}
```

### Error Response
```json
{
  "status": "ERROR", 
  "message": "Error message in Vietnamese",
  "data": null
}
```

### Validation Error Response
```json
{
  "status": "ERROR",
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "field1": "Error message 1",
    "field2": "Error message 2"
  }
}
```

## Performance Testing

### Load Testing
- Create 100 orders simultaneously
- Get order details 1000 times
- Update order status 500 times

### Stress Testing
- Test with large order quantities
- Test with many order items
- Test concurrent order creation

## Security Testing

### Authentication Testing
- Test endpoints without token
- Test with invalid token
- Test with expired token

### Authorization Testing
- User accessing other user's orders
- Non-admin accessing admin endpoints
- Guest user limitations

## Automation Scripts

### cURL Script Example
```bash
#!/bin/bash

BASE_URL="http://localhost:8088/api/v1"
JWT_TOKEN="your_jwt_token_here"

# Test 1: Create Order
echo "Creating order..."
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "userId": 1,
    "fullname": "Test User",
    "email": "test@example.com", 
    "phoneNumber": "0987654321",
    "shippingAddress": "Test Address",
    "paymentMethod": "COD",
    "orderItems": [
      {
        "productVariantId": 1,
        "quantity": 1,
        "unitPrice": 25000
      }
    ]
  }')

echo "Order created: $ORDER_RESPONSE"

# Extract order ID for next tests
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.data.id')

# Test 2: Get Order
echo "Getting order $ORDER_ID..."
curl -s -X GET "$BASE_URL/orders/$ORDER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Test 3: Update Status
echo "Updating order status..."
curl -s -X PUT "$BASE_URL/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"status": "PROCESSING"}'
```

## Monitoring và Logging

### Key Metrics to Monitor
- Order creation success rate
- Average order processing time
- Inventory accuracy
- Coupon usage accuracy
- Error rates by endpoint

### Log Analysis
- Check application logs for errors
- Monitor database query performance
- Track business logic execution

## Troubleshooting

### Common Issues
1. **Database Connection**: Check connection pool settings
2. **Transaction Issues**: Verify @Transactional annotations
3. **Stock Inconsistency**: Check concurrent access handling
4. **Coupon Logic**: Verify business rules implementation

### Debug Steps
1. Check application logs
2. Verify database state
3. Test with minimal data
4. Check network connectivity
5. Validate JWT tokens

## Documentation Updates

Cập nhật tài liệu này khi:
- Thêm endpoint mới
- Thay đổi business logic
- Cập nhật error handling
- Thêm validation rules

---

**Lưu ý**: Tất cả test cases này đã được thiết kế để hoạt động với hệ thống OceanFresh hiện tại. Cập nhật các test cases khi có thay đổi về API hoặc business logic.
