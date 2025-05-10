package com.tuandat.oceanfresh_backend.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tuandat.oceanfresh_backend.dtos.ProductAttributeValueDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;
import com.tuandat.oceanfresh_backend.responses.ProductAttributeValueResponse;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.services.product.attribute.IProductAttributeValueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/product-attributes")
@RequiredArgsConstructor
public class ProductAttributeValueController {
    
    private final IProductAttributeValueService productAttributeValueService;
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseObject> getProductAttributes(@PathVariable("productId") Long productId) {
        List<ProductAttributeValueResponse> values = productAttributeValueService.getProductAttributeValues(productId);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Get product attributes successfully")
                .data(values)
                .build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getProductAttributeById(@PathVariable("id") Long id) {
        try {
            ProductAttributeValue value = productAttributeValueService.getProductAttributeValueById(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Get product attribute successfully")
                    .data(ProductAttributeValueResponse.fromProductAttributeValue(value))
                    .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("")
    public ResponseEntity<ResponseObject> createProductAttribute(@Valid @RequestBody ProductAttributeValueDTO dto,
                                                                 BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Invalid request data")
                            .data(errorMessages)
                            .build());
        }
        
        try {
            ProductAttributeValue newValue = productAttributeValueService.createProductAttributeValue(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseObject.builder()
                            .status(HttpStatus.CREATED)
                            .message("Create product attribute successfully")
                            .data(ProductAttributeValueResponse.fromProductAttributeValue(newValue))
                            .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateProductAttribute(@PathVariable("id") Long id,
                                                                @Valid @RequestBody ProductAttributeValueDTO dto,
                                                                BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Invalid request data")
                            .data(errorMessages)
                            .build());
        }
        
        try {
            ProductAttributeValue updatedValue = productAttributeValueService.updateProductAttributeValue(id, dto);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Update product attribute successfully")
                            .data(ProductAttributeValueResponse.fromProductAttributeValue(updatedValue))
                            .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteProductAttribute(@PathVariable("id") Long id) {
        try {
            productAttributeValueService.deleteProductAttributeValue(id);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Delete product attribute successfully")
                            .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ResponseObject> deleteAllProductAttributes(@PathVariable("productId") Long productId) {
        productAttributeValueService.deleteAllProductAttributeValues(productId);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Delete all product attributes successfully")
                        .build());
    }
    
    /**
     * API endpoint để thêm nhiều giá trị cho một thuộc tính của sản phẩm
     * @param productId ID của sản phẩm
     * @param attributeId ID của thuộc tính
     * @param body Map chứa danh sách các giá trị của thuộc tính (key: "values")
     * @return Phản hồi chứa thông tin về các giá trị thuộc tính đã được thêm
     */
    @PostMapping("/product/{productId}/attribute/{attributeId}/multi-values")
    public ResponseEntity<ResponseObject> addMultipleAttributeValues(
            @PathVariable("productId") Long productId,
            @PathVariable("attributeId") Long attributeId,
            @RequestBody Map<String, List<String>> body) {
        try {
            List<String> values = body.get("values");
            if (values == null || values.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .message("Danh sách giá trị thuộc tính không được trống")
                                .build());
            }

            List<ProductAttributeValue> attributeValues = new ArrayList<>();
            for (String value : values) {
                ProductAttributeValueDTO dto = new ProductAttributeValueDTO();
                dto.setProductId(productId);
                dto.setAttributeId(attributeId);
                dto.setValue(value);
                
                ProductAttributeValue attributeValue = productAttributeValueService.createProductAttributeValue(dto);
                attributeValues.add(attributeValue);
            }
            
            List<ProductAttributeValueResponse> responses = attributeValues.stream()
                    .map(ProductAttributeValueResponse::fromProductAttributeValue)
                    .toList();
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Thêm nhiều giá trị thuộc tính sản phẩm thành công")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Lỗi khi thêm nhiều giá trị thuộc tính sản phẩm: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * API endpoint để cập nhật tất cả các giá trị của một thuộc tính sản phẩm
     * @param productId ID của sản phẩm
     * @param attributeId ID của thuộc tính
     * @param body Map chứa danh sách các giá trị mới của thuộc tính (key: "values")
     * @return Phản hồi chứa thông tin về các giá trị thuộc tính đã được cập nhật
     */
    @PutMapping("/product/{productId}/attribute/{attributeId}/multi-values")
    public ResponseEntity<ResponseObject> updateMultipleAttributeValues(
            @PathVariable("productId") Long productId,
            @PathVariable("attributeId") Long attributeId,
            @RequestBody Map<String, List<String>> body) {
        try {
            List<String> values = body.get("values");
            if (values == null) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .message("Danh sách giá trị thuộc tính là bắt buộc")
                                .build());
            }
            
            // Xóa tất cả các giá trị hiện tại của thuộc tính này
            productAttributeValueService.deleteAttributeValuesForProductAndAttribute(productId, attributeId);
            
            // Nếu danh sách giá trị mới là rỗng, trả về thành công (đã xóa tất cả)
            if (values.isEmpty()) {
                return ResponseEntity.ok(ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Đã xóa tất cả giá trị của thuộc tính")
                        .data(new ArrayList<>())
                        .build());
            }
            
            // Thêm các giá trị mới
            List<ProductAttributeValue> attributeValues = new ArrayList<>();
            for (String value : values) {
                ProductAttributeValueDTO dto = new ProductAttributeValueDTO();
                dto.setProductId(productId);
                dto.setAttributeId(attributeId);
                dto.setValue(value);
                
                ProductAttributeValue attributeValue = productAttributeValueService.createProductAttributeValue(dto);
                attributeValues.add(attributeValue);
            }
            
            List<ProductAttributeValueResponse> responses = attributeValues.stream()
                    .map(ProductAttributeValueResponse::fromProductAttributeValue)
                    .toList();
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Cập nhật nhiều giá trị thuộc tính sản phẩm thành công")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Lỗi khi cập nhật nhiều giá trị thuộc tính sản phẩm: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * API endpoint để lấy tất cả các giá trị của một thuộc tính sản phẩm
     * @param productId ID của sản phẩm
     * @param attributeId ID của thuộc tính
     * @return Danh sách các giá trị của thuộc tính
     */
    @GetMapping("/product/{productId}/attribute/{attributeId}/values")
    public ResponseEntity<ResponseObject> getAttributeValues(
            @PathVariable("productId") Long productId,
            @PathVariable("attributeId") Long attributeId) {
        try {
            List<ProductAttributeValueResponse> values = 
                productAttributeValueService.getAttributeValuesForProductAndAttribute(productId, attributeId);
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Lấy giá trị thuộc tính sản phẩm thành công")
                    .data(values)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Lỗi khi lấy giá trị thuộc tính sản phẩm: " + e.getMessage())
                            .build());
        }
    }
}
