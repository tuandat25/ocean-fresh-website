# Hướng dẫn sử dụng thuộc tính đa giá trị trong OceanFresh

## Giới thiệu

Hệ thống Entity-Attribute-Value (EAV) của OceanFresh giờ đây hỗ trợ thuộc tính đa giá trị. Điều này có nghĩa là một sản phẩm có thể có nhiều giá trị cho cùng một thuộc tính. Ví dụ, một sản phẩm "Tôm" có thể có thuộc tính "Khối lượng" với các giá trị như "2kg" và "5kg".

## Cách sử dụng

### 1. Cập nhật sản phẩm với thuộc tính đa giá trị

```
PUT /api/v1/products/{id}
```

**Request Body:**

```json
{
  "name": "Tôm sú tươi",
  "price": 250000,
  "description": "Tôm sú tươi ngon từ Cà Mau",
  "category_id": 2,
  "attributes": {
    "Xuất xứ": "Cà Mau",
    "Đóng gói": "Đông lạnh"
  },
  "multiValueAttributes": {
    "Khối lượng": ["2kg", "5kg"],
    "Màu sắc": ["Đỏ", "Xanh"]
  }
}
```

Trong đó:
- `attributes`: Chứa các thuộc tính đơn giá trị (mỗi thuộc tính chỉ có một giá trị)
- `multiValueAttributes`: Chứa các thuộc tính đa giá trị (mỗi thuộc tính có thể có nhiều giá trị)

### 2. Thêm nhiều giá trị cho một thuộc tính

```
POST /api/v1/product-attributes/product/{productId}/attribute/{attributeId}/multi-values
```

**Request Body:**

```json
{
  "values": ["2kg", "5kg", "10kg"]
}
```

### 3. Cập nhật tất cả giá trị của một thuộc tính

```
PUT /api/v1/product-attributes/product/{productId}/attribute/{attributeId}/multi-values
```

**Request Body:**

```json
{
  "values": ["2kg", "5kg", "10kg"]
}
```

### 4. Lấy tất cả giá trị của một thuộc tính

```
GET /api/v1/product-attributes/product/{productId}/attribute/{attributeId}/values
```

## Cấu trúc Phản hồi

Khi lấy thông tin sản phẩm, hệ thống sẽ trả về thuộc tính đã được nhóm lại trong trường `grouped_attributes`:

```json
{
  "id": 123,
  "name": "Tôm sú tươi",
  "price": 250000,
  "description": "Tôm sú tươi ngon từ Cà Mau",
  "category_id": 2,
  "attribute_values": [
    {
      "id": 1,
      "product_id": 123,
      "attribute_id": 1,
      "attribute_name": "Xuất xứ",
      "value": "Cà Mau"
    },
    {
      "id": 2,
      "product_id": 123,
      "attribute_id": 2,
      "attribute_name": "Khối lượng",
      "value": "2kg"
    },
    {
      "id": 3,
      "product_id": 123,
      "attribute_id": 2,
      "attribute_name": "Khối lượng",
      "value": "5kg"
    }
  ],
  "grouped_attributes": {
    "Xuất xứ": ["Cà Mau"],
    "Khối lượng": ["2kg", "5kg"]
  }
}
```

## Ví dụ sử dụng

### Tạo sản phẩm mới với thuộc tính đa giá trị

```javascript
const productData = {
  name: "Tôm sú tươi",
  price: 250000,
  description: "Tôm sú tươi ngon từ Cà Mau",
  category_id: 2,
  attributes: {
    "Xuất xứ": "Cà Mau",
    "Đóng gói": "Đông lạnh"
  },
  multiValueAttributes: {
    "Khối lượng": ["2kg", "5kg"],
    "Màu sắc": ["Đỏ", "Xanh"]
  }
};

fetch('/api/v1/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(productData)
})
.then(response => response.json())
.then(data => console.log(data));
```

### Truy vấn các giá trị của một thuộc tính cụ thể

```javascript
fetch('/api/v1/product-attributes/product/123/attribute/2/values')
.then(response => response.json())
.then(data => {
  console.log(data.data); // Danh sách các giá trị thuộc tính "Khối lượng" của sản phẩm 123
});
```
