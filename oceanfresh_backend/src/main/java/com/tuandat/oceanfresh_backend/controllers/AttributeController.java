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

import com.tuandat.oceanfresh_backend.dtos.AttributeDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.responses.AttributeResponse;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.services.attribute.IAttributeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/attributes")
@RequiredArgsConstructor
public class AttributeController {
    
    private final IAttributeService attributeService;
    
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllAttributes() {
        List<AttributeResponse> attributes = attributeService.getAllAttributes();
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Get all attributes successfully")
                .data(attributes)
                .build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getAttributeById(@PathVariable("id") Long id) {
        try {
            Attribute attribute = attributeService.getAttributeById(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Get attribute successfully")
                    .data(AttributeResponse.fromAttribute(attribute))
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
                            .message("Invalid request data")
                            .data(errorMessages)
                            .build());
        }
        
        // Kiểm tra nếu tên thuộc tính đã tồn tại
        if (attributeService.existsByName(attributeDTO.getName())) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Attribute name already exists")
                            .build());
        }
        
        Attribute newAttribute = attributeService.createAttribute(attributeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.builder()
                        .status(HttpStatus.CREATED)
                        .message("Create attribute successfully")
                        .data(AttributeResponse.fromAttribute(newAttribute))
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
                            .message("Invalid request data")
                            .data(errorMessages)
                            .build());
        }
        
        try {
            Attribute updatedAttribute = attributeService.updateAttribute(id, attributeDTO);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Update attribute successfully")
                            .data(AttributeResponse.fromAttribute(updatedAttribute))
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
    public ResponseEntity<ResponseObject> deleteAttribute(@PathVariable("id") Long id) {
        try {
            attributeService.deleteAttribute(id);
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Delete attribute successfully")
                            .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(e.getMessage())
                            .build());
        }
    }
}
