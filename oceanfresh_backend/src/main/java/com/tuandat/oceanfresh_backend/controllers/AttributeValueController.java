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

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeValueDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.responses.AttributeValueResponse;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.services.attribute.IAttributeValueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/attribute-values")
@RequiredArgsConstructor
public class AttributeValueController {
    
    private final IAttributeValueService attributeValueService;
    
    @PostMapping("")
    public ResponseEntity<ResponseObject> createAttributeValue(@Valid @RequestBody AttributeValueDTO attributeValueDTO,
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
            AttributeValueResponse newValue = attributeValueService.createAttributeValue(attributeValueDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseObject.builder()
                            .status(HttpStatus.CREATED)
                            .message("Tạo giá trị thuộc tính thành công")
                            .data(newValue)
                            .build());
        } catch (ResourceNotFoundException | DuplicateResourceException e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getAttributeValueById(@PathVariable("id") Long id) {
        try {
            AttributeValueResponse value = attributeValueService.getAttributeValueById(id);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Lấy thông tin giá trị thuộc tính thành công")
                            .data(value)
                            .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateAttributeValue(@PathVariable("id") Long id,
                                                              @Valid @RequestBody AttributeValueDTO attributeValueDTO,
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
            AttributeValueResponse updatedValue = attributeValueService.updateAttributeValue(id, attributeValueDTO);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Cập nhật giá trị thuộc tính thành công")
                            .data(updatedValue)
                            .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteAttributeValue(@PathVariable("id") Long id) {
        try {
            attributeValueService.deleteAttributeValue(id);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Xóa giá trị thuộc tính thành công")
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
