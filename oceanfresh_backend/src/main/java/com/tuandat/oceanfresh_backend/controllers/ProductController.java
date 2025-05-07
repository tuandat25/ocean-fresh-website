package com.tuandat.oceanfresh_backend.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tuandat.oceanfresh_backend.dtos.ProductDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductImageDTO;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.ProductResponse;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.responses.product.ProductListResponse;
import com.tuandat.oceanfresh_backend.services.product.IProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javafaker.Faker;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    // Create product
    @PostMapping(value = "")
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

    // Upload product images
    @PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles(@PathVariable("id") Long productId,
            @RequestParam("files") List<MultipartFile> files) throws Exception {

        Product existingProduct = productService.getProductById(productId);
        files = files == null ? new ArrayList<MultipartFile>() : files;

        if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Maximum number of images per product is "
                                    + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT)
                            .build());
        }
        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                continue;
            }
            // Kiểm tra kích thước file và định dạng
            if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .build());
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(ResponseObject.builder()
                                .message("Unsupported file type: " + contentType)
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .build());
            }
            // Lưu file và cập nhật thumbnail trong DTO
            String filename = productService.storeFile(file); // Thay thế hàm này với code của bạn để lưu file
            // lưu vào đối tượng product trong DB
            ProductImage productImage = productService.createProductImage(
                    existingProduct.getId(),
                    ProductImageDTO.builder()
                            .imageUrl(filename)
                            .build());
            productImages.add(productImage);
        }

        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Upload image successfully")
                .status(HttpStatus.CREATED)
                .data(productImages)
                .build());
    }

    // Get all products
    @GetMapping("")
    public ResponseEntity<ResponseObject> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) throws JsonProcessingException {
        int totalPages = 0;
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                // Sort.by("createdAt").descending()
                Sort.by("id").ascending());
        logger.info(String.format("keyword = %s, category_id = %d, page = %d, limit = %d",
                keyword, categoryId, page, limit));
        Page<ProductResponse> productPage = productService
                .getAllProducts(keyword, categoryId, pageRequest);
        // Lấy tổng số trang
        totalPages = productPage.getTotalPages();
        List<ProductResponse> productResponses = productPage.getContent();
        // Bổ sung totalPages vào các đối tượng ProductResponse
        for (ProductResponse product : productResponses) {
            product.setTotalPages(totalPages);
        }
        ProductListResponse productListResponse = ProductListResponse
                .builder()
                .products(productResponses)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Get products successfully")
                .status(HttpStatus.OK)
                .data(productListResponse)
                .build());
    }

    //Fake data for testing
    @PostMapping("/generateFakeProducts")
    public ResponseEntity<?> generateFakeProducts(@RequestParam int count) {
        Faker faker = new Faker();
        for (int i = 0; i < count; i++) {
            String name = faker.commerce().productName();
            // Check if the product name already exists
            if (productService.existsByName(name)) {
                continue; // Skip this iteration if the product name already exists
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(name)
                    .price((float)faker.number().numberBetween(10_000, 100_000_000))
                    .description(faker.lorem().paragraph())
                    .quantity(faker.number().numberBetween(1, 100))
                    .soldQuantity(faker.number().numberBetween(0, 50))
                    .categoryId((long)faker.number().numberBetween(10, 25)) // Assuming category ID 1 exists
                    .build();
            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseObject.builder()
                                .message("Error creating fake product: " + e.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
            }
        }
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Fake products generated successfully")
                .status(HttpStatus.OK)
                .build());
    }
}
