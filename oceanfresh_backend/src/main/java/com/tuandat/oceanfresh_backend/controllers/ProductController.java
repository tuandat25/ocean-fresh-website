package com.tuandat.oceanfresh_backend.controllers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuandat.oceanfresh_backend.dtos.product.ProductCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductDetailDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantRequestDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.responses.product.ProductBaseResponse;
import com.tuandat.oceanfresh_backend.services.product.ProductService;
import com.tuandat.oceanfresh_backend.utils.FileUtils;
import com.tuandat.oceanfresh_backend.utils.MessageKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý các API liên quan đến sản phẩm
 */
@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
        private final ProductService productService;
        private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

        /**
         * API tạo mới sản phẩm - chỉ admin
         * 
         * @param productDetailDTO thông tin sản phẩm cần tạo
         * @param bindingResult    kết quả validation
         * @return ResponseEntity chứa thông tin sản phẩm đã tạo hoặc thông báo lỗi
         */
        @PostMapping(value = "")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO,
                        BindingResult bindingResult) {

                try {
                        if (bindingResult.hasErrors()) {
                                List<String> errors = bindingResult.getFieldErrors().stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                                .status(HttpStatus.BAD_REQUEST)
                                                .message("Validation errors")
                                                .data(errors)
                                                .build());
                        }
                        // Gọi service để tạo sản phẩm
                        var newProduct = productService.createProduct(productCreateDTO);
                        // Trả về thông tin sản phẩm đã tạo
                        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                                        .status(HttpStatus.CREATED)
                                        .message("Tạo sản phẩm thành công")
                                        .data(newProduct)
                                        .build());
                } catch (DuplicateResourceException e) {
                        logger.warn("Attempted to create a product with a duplicate resource: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                                        .status(HttpStatus.CONFLICT)
                                        .message(e.getMessage())
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi tạo sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseObject.builder()
                                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .message("Lỗi khi tạo sản phẩm: " + e.getMessage())
                                                        .build());
                }
        }

        /**
         * API tạo mới sản phẩm kèm ảnh chính
         * 
         * @param productData thông tin sản phẩm dạng JSON
         * @param mainImage   file ảnh chính (không bắt buộc)
         * @return ResponseEntity chứa thông tin sản phẩm đã tạo hoặc thông báo lỗi
         */
        @PostMapping(value = "/with-main-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> createProductWithMainImage(
                        @RequestPart(value = "productData", required = true) String productDataJson,
                        @RequestPart(name = "mainImage", required = false) MultipartFile mainImage,
                        HttpServletRequest request) {

                logger.info("Request Content-Type: {}", request.getContentType());
                logger.info("Received productDataJson: {}", productDataJson);
                logger.info("Received mainImage: {}", mainImage != null ? mainImage.getOriginalFilename() : "null");

                ProductCreateDTO productData;

                try {
                        // Chuyển đổi JSON string thành đối tượng ProductCreateDTO
                        ObjectMapper objectMapper = new ObjectMapper();
                        productData = objectMapper.readValue(productDataJson, ProductCreateDTO.class);
                } catch (Exception e) {
                        logger.error("Lỗi khi parse productData JSON: {}", e.getMessage());
                        return ResponseEntity.badRequest().body(ResponseObject.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .message("Lỗi khi xử lý dữ liệu sản phẩm: " + e.getMessage())
                                        .build());
                }

                try {
                        // Xử lý file ảnh chính nếu có
                        if (mainImage != null && !mainImage.isEmpty()) {
                                // Kiểm tra kích thước file và định dạng
                                if (mainImage.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                                        .body(ResponseObject.builder()
                                                                        .message("Kích thước ảnh không được vượt quá 10MB")
                                                                        .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                                                        .build());
                                }

                                String contentType = mainImage.getContentType();
                                if (contentType == null || !contentType.startsWith("image/")) {
                                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                                        .body(ResponseObject.builder()
                                                                        .message("File phải là hình ảnh")
                                                                        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                                                        .build());
                                }

                                // Lưu file ảnh và lấy tên file đã lưu
                                String fileName = FileUtils.storeFile(mainImage);

                                // Cập nhật URL ảnh chính trong dữ liệu sản phẩm
                                productData.setMainImageUrl(fileName);
                        }

                        // Gọi service để tạo sản phẩm
                        ProductDetailDTO createdProduct = productService.createProduct(productData);

                        // Trả về thông tin sản phẩm đã tạo
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ResponseObject.builder()
                                                        .status(HttpStatus.CREATED)
                                                        .message("Tạo sản phẩm kèm ảnh chính thành công")
                                                        .data(createdProduct)
                                                        .build());

                } catch (DuplicateResourceException e) {
                        logger.warn("Attempted to create a product with a duplicate resource: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                                        .status(HttpStatus.CONFLICT)
                                        .message(e.getMessage())
                                        .build());
                } catch (IOException e) {
                        logger.error("Lỗi khi xử lý file ảnh: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseObject.builder()
                                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .message("Lỗi khi xử lý file ảnh: " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi tạo sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseObject.builder()
                                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .message("Lỗi khi tạo sản phẩm: " + e.getMessage())
                                                        .build());
                }
        }

        @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> uploadImages(
                        @PathVariable("id") Long productId,
                        @RequestParam("files") List<MultipartFile> files) throws Exception {
                ProductDetailDTO existingProduct = productService.getProductById(productId);
                files = files == null ? new ArrayList<MultipartFile>() : files;
                if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
                        return ResponseEntity.badRequest().body(
                                        ResponseObject.builder()
                                                        .message("Số lượng ảnh tải lên vượt quá giới hạn cho phép")
                                                        .build());
                }
                List<ProductImageDTO> productImageDTOs = new ArrayList<>();
                for (MultipartFile file : files) {
                        if (file.getSize() == 0) {
                                continue;
                        }
                        // Kiểm tra kích thước file và định dạng
                        if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                                .body(ResponseObject.builder()
                                                                .message("Kích thước ảnh không được vượt quá 10MB")
                                                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                                                .build());
                        }
                        String contentType = file.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                                .body(ResponseObject.builder()
                                                                .message("File phải là hình ảnh")
                                                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                                                .build());
                        }
                        // Lưu file và cập nhật thumbnail trong DTO
                        String filename = FileUtils.storeFile(file);
                        // lưu vào đối tượng product trong DB
                        ProductImage productImage = productService.createProductImage(
                                        existingProduct.getId(),
                                        ProductImageDTO.builder()
                                                        .imageUrl(filename)
                                                        .build());
                        // Chuyển sang DTO để trả về
                        ProductImageDTO dto = ProductImageDTO.builder()
                                        .productId(productImage.getProduct().getId())
                                        .imageUrl(productImage.getImageUrl())
                                        .build();
                        productImageDTOs.add(dto);
                }

                return ResponseEntity.ok().body(ResponseObject.builder()
                                .message("Upload image successfully")
                                .status(HttpStatus.CREATED)
                                .data(productImageDTOs)
                                .build());
        }

        @GetMapping("/images/{imageName}")
        public ResponseEntity<?> viewImage(@PathVariable String imageName) {
                try {
                        // Kiểm tra nếu imageName là một URL (http hoặc https)
                        if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
                                // Redirect đến URL gốc
                                return ResponseEntity.status(HttpStatus.FOUND)
                                                .location(java.net.URI.create(imageName))
                                                .build();
                        }

                        // Xử lý file local trong thư mục uploads
                        java.nio.file.Path imagePath = Paths.get("uploads/" + imageName);
                        UrlResource resource = new UrlResource(imagePath.toUri());

                        if (resource.exists()) {
                                // Xác định content type dựa trên extension
                                String contentType = getContentTypeFromFileName(imageName);
                                return ResponseEntity.ok()
                                                .contentType(MediaType.parseMediaType(contentType))
                                                .body(resource);
                        } else {
                                logger.info(imageName + " not found in uploads folder");
                                // Trả về ảnh mặc định
                                return fallbackToDefaultImage();
                        }
                } catch (Exception e) {
                        logger.error("Error occurred while retrieving image: " + e.getMessage());
                        return fallbackToDefaultImage();
                }
        }

        /**
         * Xác định content type dựa trên extension của file
         */
        private String getContentTypeFromFileName(String fileName) {
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                return switch (extension) {
                        case "jpg", "jpeg" -> "image/jpeg";
                        case "png" -> "image/png";
                        case "gif" -> "image/gif";
                        case "webp" -> "image/webp";
                        case "bmp" -> "image/bmp";
                        case "svg" -> "image/svg+xml";
                        default -> "image/jpeg"; // Default fallback
                };
        }

        // API lấy danh sách sản phẩm theo trang

        
        @GetMapping
        public ResponseEntity<ResponseObject> getAllProducts(@PageableDefault(size = 10) Pageable pageable,
                        Authentication authentication) {
                try {
                        Page<ProductBaseResponse> products;
                        
                        // Lấy authentication từ SecurityContext nếu parameter null
                        if (authentication == null) {
                                authentication = SecurityContextHolder.getContext().getAuthentication();
                        }
                        
                        // Kiểm tra xem user có role ADMIN không
                        boolean isAdmin = authentication != null &&
                                        authentication.isAuthenticated() &&
                                        authentication.getAuthorities().stream()
                                                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                        if (isAdmin) {
                                // Admin có thể xem tất cả sản phẩm (kể cả inactive)
                                products = productService.getAllProducts(pageable);
                        } else {
                                // User thường và guest chỉ xem sản phẩm active
                                products = productService.getAllProductsIsActive(pageable);
                        }

                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy danh sách sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(products)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy danh sách sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy danh sách sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        // API lấy danh sách sản phẩm theo trang dành cho khách hàng
        @GetMapping("/get-all-products")
        public ResponseEntity<ResponseObject> getAllProductsIsActiveTrue(
                        @PageableDefault(size = 10) Pageable pageable) {
                try {
                        Page<ProductBaseResponse> products = productService.getAllProductsIsActive(pageable);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy danh sách sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(products)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy danh sách sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy danh sách sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API lấy chi tiết sản phẩm theo ID
         * 
         * @param productId ID sản phẩm cần lấy thông tin
         * @return ResponseEntity chứa thông tin chi tiết sản phẩm hoặc thông báo lỗi
         */
        @GetMapping("/{productId}")
        public ResponseEntity<ResponseObject> getProductById(@PathVariable Long productId) {
                try {
                        ProductDetailDTO product = productService.getProductById(productId);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy chi tiết sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(product)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy chi tiết sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy chi tiết sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API lấy chi tiết sản phẩm theo slug
         * 
         * @param slug slug của sản phẩm cần lấy thông tin
         * @return ResponseEntity chứa thông tin chi tiết sản phẩm hoặc thông báo lỗi
         */
        @GetMapping("/slug/{slug}")
        public ResponseEntity<ResponseObject> getProductBySlug(@PathVariable String slug) {
                try {
                        ProductDetailDTO product = productService.getProductBySlug(slug);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy chi tiết sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(product)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy chi tiết sản phẩm theo slug: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy chi tiết sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API cập nhật sản phẩm
         */
        @PutMapping("/{productId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> updateProduct(
                        @PathVariable Long productId,
                        @Valid @RequestBody ProductDetailDTO productDetailDTO,
                        BindingResult bindingResult) {
                try {
                        if (bindingResult.hasErrors()) {
                                List<String> errors = bindingResult.getFieldErrors().stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                                .status(HttpStatus.BAD_REQUEST)
                                                .message("Validation errors")
                                                .data(errors)
                                                .build());
                        }

                        ProductDetailDTO updatedProduct = productService.updateProduct(productId, productDetailDTO);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Cập nhật sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(updatedProduct)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (DuplicateResourceException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi cập nhật sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi cập nhật sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        // /**
        // * API xóa sản phẩm theo ID
        // */
        @DeleteMapping("/{productId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> deleteProduct(@PathVariable Long productId) {
                try {
                        productService.deleteProduct(productId);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Xóa sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi xóa sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi xóa sản phẩm: " + e.getMessage())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API thêm biến thể cho sản phẩm
         */
        @PostMapping("/{productId}/variants")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> addVariantToProduct(
                        @PathVariable Long productId,
                        @RequestBody @Valid ProductVariantRequestDTO variantRequestDTO,
                        BindingResult bindingResult) {
                try {
                        if (bindingResult.hasErrors()) {
                                List<String> errors = bindingResult.getFieldErrors().stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                                .status(HttpStatus.BAD_REQUEST)
                                                .message("Validation errors")
                                                .data(errors)
                                                .build());
                        }

                        ProductVariantDTO createdVariant = productService.addVariantToProduct(productId,
                                        variantRequestDTO);
                        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseObject.builder()
                                        .message("Thêm biến thể sản phẩm thành công")
                                        .status(HttpStatus.CREATED)
                                        .data(createdVariant)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (DuplicateResourceException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi thêm biến thể sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi thêm biến thể sản phẩm: " + e.getMessage())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API cập nhật biến thể sản phẩm
         */
        @PutMapping("/variants/{variantId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> updateProductVariant(
                        @PathVariable Long variantId,
                        @Valid @RequestBody ProductVariantRequestDTO variantRequestDTO,
                        BindingResult bindingResult) {
                try {
                        if (bindingResult.hasErrors()) {
                                List<String> errors = bindingResult.getFieldErrors().stream()
                                                .map(FieldError::getDefaultMessage)
                                                .toList();
                                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                                .status(HttpStatus.BAD_REQUEST)
                                                .message("Validation errors")
                                                .data(errors)
                                                .build());
                        }

                        ProductVariantDTO updatedVariant = productService.updateProductVariant(variantId,
                                        variantRequestDTO);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Cập nhật biến thể sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(updatedVariant)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (DuplicateResourceException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi cập nhật biến thể sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi cập nhật biến thể sản phẩm: " + e.getMessage())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API xóa biến thể sản phẩm
         */
        @DeleteMapping("/variants/{variantId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject> deleteProductVariant(@PathVariable Long variantId) {
                try {
                        productService.deleteProductVariant(variantId);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Xóa biến thể sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi xóa biến thể sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi xóa biến thể sản phẩm: " + e.getMessage())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API lấy chi tiết biến thể sản phẩm theo ID
         */
        @GetMapping("/variants/{variantId}")
        public ResponseEntity<ResponseObject> getProductVariantById(@PathVariable Long variantId) {
                try {
                        ProductVariantDTO variant = productService.getProductVariantById(variantId);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy chi tiết biến thể sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(variant)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy chi tiết biến thể sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy chi tiết biến thể sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API lấy chi tiết biến thể sản phẩm theo SKU
         */
        @GetMapping("/variants/sku/{sku}")
        public ResponseEntity<ResponseObject> getProductVariantBySku(@PathVariable String sku) {
                try {
                        ProductVariantDTO variant = productService.getProductVariantBySku(sku);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy chi tiết biến thể sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(variant)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy chi tiết biến thể sản phẩm theo SKU: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy chi tiết biến thể sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API lấy danh sách biến thể của sản phẩm
         */
        @GetMapping("/{productId}/variants")
        public ResponseEntity<ResponseObject> getVariantsByProductId(@PathVariable Long productId) {
                try {
                        List<ProductVariantDTO> variants = productService.getVariantsByProductId(productId);
                        // Kiểm tra xem sản phẩm có tồn tại không nếu không có biến thể nào
                        if (variants.isEmpty()) {
                                productService.getProductById(productId); // Sẽ throw ResourceNotFoundException nếu sản
                                                                          // phẩm không tồn tại
                        }
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy danh sách biến thể sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(variants)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi lấy danh sách biến thể sản phẩm: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi lấy danh sách biến thể sản phẩm")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        // API tìm biến thể sản phẩm theo các thuộc tính
        @GetMapping("/{productId}/variants/find")
        public ResponseEntity<ResponseObject> findActiveVariantByAttributes(
                        @PathVariable Long productId,
                        @RequestParam Set<Long> attributeValueIds) {
                try {
                        ProductVariantDTO variant = productService.findActiveVariantByProductAndAttributes(productId,
                                        attributeValueIds);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Tìm biến thể sản phẩm thành công")
                                        .status(HttpStatus.OK)
                                        .data(variant)
                                        .build());
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                } catch (Exception e) {
                        logger.error("Lỗi khi tìm biến thể sản phẩm theo thuộc tính: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseObject.builder()
                                        .message("Lỗi khi tìm biến thể sản phẩm theo thuộc tính")
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build());
                }
        }

        /**
         * API để proxy image từ URL bên ngoài
         * Sử dụng: /api/products/images/proxy?url=https://example.com/image.jpg
         */
        @GetMapping("/images/proxy")
        public ResponseEntity<?> proxyImage(@RequestParam String url) {
                try {
                        // Validate URL
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                return ResponseEntity.badRequest()
                                                .body("Invalid URL. Must start with http:// or https://");
                        }

                        // Tạo connection đến URL
                        java.net.URI imageUri = java.net.URI.create(url);
                        java.net.URL imageUrl = imageUri.toURL();
                        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000); // 5 seconds timeout
                        connection.setReadTimeout(10000); // 10 seconds timeout
                        connection.setRequestProperty("User-Agent", "OceanFresh-Backend/1.0");

                        // Kiểm tra response code
                        int responseCode = connection.getResponseCode();
                        if (responseCode != 200) {
                                logger.warn("Failed to fetch image from URL: {} - Response code: {}", url,
                                                responseCode);
                                return fallbackToDefaultImage();
                        }

                        // Lấy content type từ response header
                        String contentType = connection.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                                logger.warn("URL does not point to an image: {}", url);
                                return fallbackToDefaultImage();
                        }

                        // Đọc dữ liệu ảnh
                        byte[] imageData = connection.getInputStream().readAllBytes();

                        return ResponseEntity.ok()
                                        .contentType(MediaType.parseMediaType(contentType))
                                        .header("Cache-Control", "max-age=3600") // Cache 1 hour
                                        .body(imageData);

                } catch (Exception e) {
                        logger.error("Error proxying image from URL: {} - {}", url, e.getMessage());
                        return fallbackToDefaultImage();
                }
        }

        /**
         * Fallback về ảnh mặc định khi có lỗi
         */
        private ResponseEntity<?> fallbackToDefaultImage() {
                try {
                        UrlResource defaultResource = new UrlResource(Paths.get("uploads/default-product.svg").toUri());
                        return ResponseEntity.ok()
                                        .contentType(MediaType.parseMediaType("image/svg+xml"))
                                        .body(defaultResource);
                } catch (Exception e) {
                        logger.error("Error loading default image: {}", e.getMessage());
                        return ResponseEntity.notFound().build();
                }
        }
}
