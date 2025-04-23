package com.tuandat.oceanfresh_backend.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.dtos.ProductDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductImageDTO;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.services.IProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    
    @PostMapping(value= "")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO,
                                             BindingResult bindingResult) {
        
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errors);
            }
            // Call the service to create the product
            var newProduct = productService.createProduct(productDTO);
            // Return the created product as a response
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating product: " + e.getMessage());
        } 

    }
    
    @PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles( @PathVariable("id") Long productId,
                                            @RequestParam("files") List<MultipartFile> files) throws Exception {


        Product existingProduct = productService.getProductById(productId);
        files = files == null ? new ArrayList<MultipartFile>() : files;

        if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Maximum number of images per product is " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT)
                            .build()
            );
        }
        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if(file.getSize() == 0) {
                continue;
            }
            // Kiểm tra kích thước file và định dạng
            if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .build());
            }
            String contentType = file.getContentType();
            if(contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(ResponseObject.builder()
                                .message("Unsupported file type: " + contentType)
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .build());
            }
            // Lưu file và cập nhật thumbnail trong DTO
            String filename = productService.storeFile(file); // Thay thế hàm này với code của bạn để lưu file
            //lưu vào đối tượng product trong DB
            ProductImage productImage = productService.createProductImage(
                    existingProduct.getId(),
                    ProductImageDTO.builder()
                            .imageUrl(filename)
                            .build()
            );
            productImages.add(productImage);
        }

        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Upload image successfully")
                        .status(HttpStatus.CREATED)
                        .data(productImages)
                .build());
    }
}

