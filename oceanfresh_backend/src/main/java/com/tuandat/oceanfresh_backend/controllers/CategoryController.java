package com.tuandat.oceanfresh_backend.controllers;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.components.LocalizationUtils;
import com.tuandat.oceanfresh_backend.dtos.CategoryCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.CategoryDTO;
import com.tuandat.oceanfresh_backend.models.Category;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.services.ICategoryService;
import com.tuandat.oceanfresh_backend.utils.MessageKeys;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/categories")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;
    private final LocalizationUtils localizationUtils;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> createCategory(
            @Valid @ModelAttribute CategoryCreateDTO categoryCreateDTO,
            BindingResult bindingResult) {
        
        try {
            // Kiểm tra lỗi validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                
                return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Validation errors")
                            .status(HttpStatus.BAD_REQUEST)
                            .data(errors)
                            .build()
                );
            }
            
            // Validate file size (max 10MB)
            if (categoryCreateDTO.getFile().getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(ResponseObject.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGE_SIZE_EXCEEDED))
                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .build());
            }
            
            // Validate file content type
            String contentType = categoryCreateDTO.getFile().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(ResponseObject.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGE_TYPE_INVALID))
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .build());
            }
            
            // Check if category name already exists
            Category existingCategory = null;
            try {
                existingCategory = categoryService.getCategoryByName(categoryCreateDTO.getName());
            } catch (RuntimeException ignored) {
                // Category doesn't exist, which is what we want
            }
            
            if (existingCategory != null) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NAME_ALREADY_EXISTS))
                                .status(HttpStatus.BAD_REQUEST)
                                .build()
                );
            }
            
            // Store the image file
            String fileName = categoryService.storeFile(categoryCreateDTO.getFile());
            
            // Create the category DTO
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setName(categoryCreateDTO.getName());
            categoryDTO.setDescription(categoryCreateDTO.getDescription());
            categoryDTO.setImageUrl(fileName);
            
            // Create and save the category
            Category newCategory = categoryService.createCategory(categoryDTO);
            
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY))
                            .status(HttpStatus.CREATED)
                            .data(newCategory)
                            .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGE_FAILED) + ": " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    //Hiện tất cả các categories
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllCategories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get list of categories successfully")
                .status(HttpStatus.OK)
                .data(categories)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getCategoryById(
            @PathVariable("id") Long categoryId
    ) {
        Category existingCategory = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ResponseObject.builder()
                        .data(existingCategory)
                        .message("Get category information successfully")
                        .status(HttpStatus.OK)
                .build());
    }
    
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ResponseObject
                .builder()
                .data(categoryService.getCategoryById(id))
                .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                .build());
    }

    @PutMapping(value = "/{id}/update-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> updateCategoryWithImage(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        try {
            // Lấy category hiện tại
            Category existingCategory = categoryService.getCategoryById(id);
            
            // Tạo DTO từ các tham số nhận được
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setName(name);
            categoryDTO.setDescription(description);
            
            // Nếu có file ảnh mới được upload
            if (file != null && !file.isEmpty()) {
                // Xóa file ảnh cũ nếu có
                if (existingCategory.getImageUrl() != null && !existingCategory.getImageUrl().isEmpty()) {
                    try {
                        categoryService.deleteFile(existingCategory.getImageUrl());
                    } catch (IOException e) {
                        // Ghi log lỗi nhưng vẫn tiếp tục xử lý
                        System.err.println("Could not delete old image: " + e.getMessage());
                    }
                }
                
                // Lưu file ảnh mới
                String fileName = categoryService.storeFile(file);
                categoryDTO.setImageUrl(fileName);
            }
            
            // Cập nhật category
            Category updatedCategory = categoryService.updateCategory(id, categoryDTO);
            
            return ResponseEntity.ok(ResponseObject
                    .builder()
                    .data(updatedCategory)
                    .status(HttpStatus.OK)
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error updating category: " + e.getMessage())
                            .build());
        }
    }
    // @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // public ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id) throws Exception{
    //     categoryService.deleteCategory(id);
    //     return ResponseEntity.ok(
    //             ResponseObject.builder()
    //                     .status(HttpStatus.OK)
    //                     .message("Delete category successfully")
    //                     .build());
    // }
}

