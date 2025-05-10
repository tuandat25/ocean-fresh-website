package com.tuandat.oceanfresh_backend.dtos;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdateDTO {
    @Size(min = 3, max = 200, message = "Tên phải từ 3 đến 200 ký tự")
    private String name;

    @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0")
    @Max(value = 10000000, message = "Giá phải nhỏ hơn hoặc bằng 10,000,000")
    private Float price;

    private String thumbnail;

    private String description;

    @JsonProperty("category_id")
    private Long categoryId;

    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    @Max(value = 10000, message = "Số lượng phải nhỏ hơn hoặc bằng 10,000")
    @JsonProperty("quantity")
    private Long quantity;

    @JsonProperty("sold_quantity")
    @Min(value = 0, message = "Số lượng đã bán phải lớn hơn hoặc bằng 0")
    @Max(value = 10000, message = "Số lượng đã bán phải nhỏ hơn hoặc bằng 10,000")
    private Long soldQuantity;
    
    // Map để lưu trữ thuộc tính động đơn giá trị: tên thuộc tính -> giá trị
    // Ví dụ: {"Xuất xứ": "Cà Mau", "Đóng gói": "Đông lạnh"}
    private Map<String, String> attributes;
    
    // Map để lưu trữ thuộc tính động có nhiều giá trị: tên thuộc tính -> danh sách giá trị 
    // Ví dụ: {"Khối lượng": ["2kg", "5kg"], "Màu sắc": ["Đỏ", "Xanh"]}
    @JsonProperty("multiValueAttributes")
    private Map<String, List<String>> multiValueAttributes;
}
