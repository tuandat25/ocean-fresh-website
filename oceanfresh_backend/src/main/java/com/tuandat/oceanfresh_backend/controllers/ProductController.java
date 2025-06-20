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
         * API tạo mới sản phẩm
         * 
         * @param productDetailDTO thông tin sản phẩm cần tạo
         * @param bindingResult    kết quả validation
         * @return ResponseEntity chứa thông tin sản phẩm đã tạo hoặc thông báo lỗi
         */
        @PostMapping(value = "")
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
                        java.nio.file.Path imagePath = Paths.get("uploads/" + imageName);
                        UrlResource resource = new UrlResource(imagePath.toUri());

                        if (resource.exists()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaType.IMAGE_JPEG)
                                                .body(resource);
                        } else {
                                logger.info(imageName + " not found");
                                return ResponseEntity.ok()
                                                .contentType(MediaType.IMAGE_JPEG)
                                                .body(new UrlResource(Paths.get("uploads/notfound.jpeg").toUri()));
                                // return ResponseEntity.notFound().build();
                        }
                } catch (Exception e) {
                        logger.error("Error occurred while retrieving image: " + e.getMessage());
                        return ResponseEntity.notFound().build();
                }
        }

        /**
         * API lấy danh sách sản phẩm theo trang
         * 
         * @param pageable thông tin phân trang
         * @return ResponseEntity chứa danh sách sản phẩm theo trang
         */
        @GetMapping
        public ResponseEntity<ResponseObject> getAllProducts(@PageableDefault(size = 10) Pageable pageable) {
                try {
                        Page<ProductBaseResponse> products = productService.getAllProducts(pageable);
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
         * 
         * @param productId        ID sản phẩm cần cập nhật
         * @param productDetailDTO thông tin sản phẩm mới
         * @param bindingResult    kết quả validation
         * @return ResponseEntity chứa thông tin sản phẩm đã cập nhật hoặc thông báo lỗi
         */
        @PutMapping("/{productId}")
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

        /**
         * API xóa sản phẩm theo ID
         * 
         * @param productId ID sản phẩm cần xóa
         * @return ResponseEntity chứa thông báo xóa thành công hoặc thông báo lỗi
         */
        @DeleteMapping("/{productId}")
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
         * 
         * @param productId         ID sản phẩm cần thêm biến thể
         * @param variantRequestDTO thông tin biến thể cần thêm
         * @param bindingResult     kết quả validation
         * @return ResponseEntity chứa thông tin biến thể đã thêm hoặc thông báo lỗi
         */
        @PostMapping("/{productId}/variants")
        public ResponseEntity<ResponseObject> addVariantToProduct(
                        @PathVariable Long productId,
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
         * 
         * @param variantId         ID biến thể cần cập nhật
         * @param variantRequestDTO thông tin biến thể mới
         * @param bindingResult     kết quả validation
         * @return ResponseEntity chứa thông tin biến thể đã cập nhật hoặc thông báo lỗi
         */
        @PutMapping("/variants/{variantId}")
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
         * 
         * @param variantId ID biến thể cần xóa
         * @return ResponseEntity chứa thông báo xóa thành công hoặc thông báo lỗi
         */
        @DeleteMapping("/variants/{variantId}")
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
         * 
         * @param variantId ID biến thể cần lấy thông tin
         * @return ResponseEntity chứa thông tin chi tiết biến thể hoặc thông báo lỗi
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
         * 
         * @param sku SKU của biến thể cần lấy thông tin
         * @return ResponseEntity chứa thông tin chi tiết biến thể hoặc thông báo lỗi
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
         * 
         * @param productId ID sản phẩm cần lấy danh sách biến thể
         * @return ResponseEntity chứa danh sách biến thể hoặc thông báo lỗi
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

        /**
         * API tìm biến thể sản phẩm theo các thuộc tính
         * 
         * @param productId         ID sản phẩm
         * @param attributeValueIds danh sách ID của các giá trị thuộc tính
         * @return ResponseEntity chứa thông tin biến thể tương ứng hoặc thông báo lỗi
         */
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
}
