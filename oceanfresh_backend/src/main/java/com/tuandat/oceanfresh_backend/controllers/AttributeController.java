package com.tuandat.oceanfresh_backend.controllers;

import java.util.List;

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

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.responses.AttributeValueResponse;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.services.attribute.IAttributeService;
import com.tuandat.oceanfresh_backend.services.attribute.IAttributeValueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/attributes")
@RequiredArgsConstructor
public class AttributeController {
    
    private final IAttributeService attributeService;
    private final IAttributeValueService attributeValueService;
    
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllAttributes() {
        List<AttributeDTO> attributes = attributeService.getAllAttributes();
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Lấy danh sách thuộc tính thành công")
                .data(attributes)
                .build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getAttributeById(@PathVariable("id") Long id) {
        try {
            AttributeDTO attribute = attributeService.getAttributeById(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Lấy thông tin thuộc tính thành công")
                    .data(attribute)
                    .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
      @GetMapping("/code/{code}")
    public ResponseEntity<ResponseObject> getAttributeByCode(@PathVariable("code") String code) {
        try {
            AttributeDTO attribute = attributeService.getAttributeByCode(code);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Lấy thông tin thuộc tính thành công")
                    .data(attribute)
                    .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("")
    public ResponseEntity<ResponseObject> createAttribute(@Valid @RequestBody AttributeDTO attributeDTO,
                                                          BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Dữ liệu không hợp lệ")
                            .data(errorMessages)
                            .build());
        }
        
        // Kiểm tra nếu tên hoặc mã thuộc tính đã tồn tại
        if (attributeService.existsByName(attributeDTO.getName())) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Tên thuộc tính đã tồn tại")
                            .build());
        }
        
        AttributeDTO newAttribute = attributeService.createAttribute(attributeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.builder()
                        .status(HttpStatus.CREATED)
                        .message("Tạo thuộc tính thành công")
                        .data(newAttribute)
                        .build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateAttribute(@PathVariable("id") Long id,
                                                          @Valid @RequestBody AttributeDTO attributeDTO,
                                                          BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Dữ liệu không hợp lệ")
                            .data(errorMessages)
                            .build());
        }
        
        try {
            AttributeDTO updatedAttribute = attributeService.updateAttribute(id, attributeDTO);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Cập nhật thuộc tính thành công")
                            .data(updatedAttribute)
                            .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteAttribute(@PathVariable("id") Long id) {
        try {
            attributeService.deleteAttribute(id);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Xóa thuộc tính thành công")
                            .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/{id}/values")
    public ResponseEntity<ResponseObject> getAttributeValues(@PathVariable("id") Long attributeId) {
        try {
            List<AttributeValueResponse> values = attributeValueService.getAttributeValuesByAttributeId(attributeId);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Lấy danh sách giá trị thuộc tính thành công")
                            .data(values)
                            .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
}