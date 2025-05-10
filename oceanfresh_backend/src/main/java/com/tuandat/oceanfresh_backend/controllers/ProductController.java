package com.tuandat.oceanfresh_backend.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tuandat.oceanfresh_backend.dtos.ProductAttributeValueDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductUpdateDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.ProductAttributeValueResponse;
import com.tuandat.oceanfresh_backend.responses.ProductResponse;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.responses.product.ProductListResponse;
import com.tuandat.oceanfresh_backend.services.product.IProductService;
import com.tuandat.oceanfresh_backend.services.product.attribute.IProductAttributeValueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
        private final IProductService productService;
        private final IProductAttributeValueService productAttributeValueService;
        private static final Logger logger = LoggerFactory.getLogger(ProductController.class);        // Create product
        @PostMapping(value = "")
        public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO,
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
                        // Call the service to create the product
                        var newProduct = productService.createProduct(productDTO);
                        // Return the created product as a response
                        return ResponseEntity.ok(ResponseObject.builder()
                                .status(HttpStatus.CREATED)
                                .message("Tạo sản phẩm thành công")
                                .data(newProduct)
                                .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ResponseObject.builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .message("Lỗi khi tạo sản phẩm: " + e.getMessage())
                                        .build());
                }

        }// Upload product images

        @PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadFiles(@PathVariable("id") Long productId,
                        @RequestParam("files") List<MultipartFile> files) throws Exception {

                Product existingProduct = productService.getProductById(productId);
                files = files == null ? new ArrayList<>() : files;

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
                        String filename = productService.storeFile(file); // Thay thế hàm này với code của bạn để lưu
                                                                          // file
                        // lưu vào đối tượng product trong DB
                        ProductImage productImage = productService.createProductImage(
                                        existingProduct.getId(),
                                        ProductImageDTO.builder()
                                                        .imageUrl(filename)
                                                        .build());
                        productImages.add(productImage);
                }
                return ResponseEntity.ok().body(ResponseObject.builder()
                                .message("Tải ảnh thành công")
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
                int totalPages = productPage.getTotalPages();
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
                                .message("Lấy danh sách sản phẩm thành công")
                                .status(HttpStatus.OK)
                                .data(productListResponse)
                                .build());
        }

    /* 
    // Fake data for testing - Commented out as part of code cleanup
    // This endpoint is only used for development/testing and not needed in production
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
                            .price((float) faker.number().numberBetween(10_000, 100_000_000))
                            .description(faker.lorem().paragraph())
                            .quantity(Long.valueOf(faker.number().numberBetween(1, 100)))
                            .soldQuantity(Long.valueOf(faker.number().numberBetween(0, 50)))
                            .categoryId((long) faker.number().numberBetween(10, 25))
                            .build();
            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ResponseObject.builder()
                                                .message("Lỗi khi tạo sản phẩm mẫu: "
                                                                + e.getMessage())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
            }
        }
        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Sản phẩm mẫu được tạo thành công")
                        .status(HttpStatus.OK)
                        .build());
    }
    */

                // Thêm API endpoints làm việc với thuộc tính sản phẩm
        // Lấy tất cả thuộc tính của sản phẩm
        @GetMapping("/{id}/attributes")
        public ResponseEntity<ResponseObject> getProductAttributes(@PathVariable("id") Long productId) {
                try {
                        List<ProductAttributeValueResponse> attributes = productAttributeValueService
                                        .getProductAttributeValues(productId);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .status(HttpStatus.OK)
                                        .message("Lấy thuộc tính sản phẩm thành công")
                                        .data(attributes)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseObject.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .message("Lỗi khi lấy thuộc tính sản phẩm: " + e.getMessage())
                                                        .build());
                }
        }

        // Thêm thuộc tính cho sản phẩm
        @PostMapping("/{id}/attributes")
        public ResponseEntity<ResponseObject> addProductAttribute(
                        @PathVariable("id") Long productId,
                        @Valid @RequestBody ProductAttributeValueDTO attributeValueDTO) {
                try {
                        attributeValueDTO.setProductId(productId); // Đảm bảo ID sản phẩm đúng
                        ProductAttributeValue attributeValue = productAttributeValueService
                                        .createProductAttributeValue(attributeValueDTO);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .status(HttpStatus.OK)
                                        .message("Thêm thuộc tính sản phẩm thành công")
                                        .data(ProductAttributeValueResponse.fromProductAttributeValue(attributeValue))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseObject.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .message("Lỗi khi thêm thuộc tính sản phẩm: " + e.getMessage())
                                                        .build());
                }
        }

        // Cập nhật thuộc tính của sản phẩm
        @PutMapping("/{productId}/attributes/{attributeId}")
        public ResponseEntity<ResponseObject> updateProductAttribute(
                        @PathVariable("productId") Long productId,
                        @PathVariable("attributeId") Long attributeId,
                        @RequestBody Map<String, String> body) {
                try {
                        String value = body.get("value");
                        if (value == null) {
                                return ResponseEntity.badRequest().body(
                                                ResponseObject.builder()
                                                                .status(HttpStatus.BAD_REQUEST)
                                                                .message("Giá trị thuộc tính là bắt buộc")
                                                                .build());
                        }

                        Product product = productService.updateProductAttribute(productId, attributeId, value);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .status(HttpStatus.OK)
                                        .message("Cập nhật thuộc tính sản phẩm thành công")
                                        .data(ProductResponse.fromProduct(product))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseObject.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .message("Lỗi khi cập nhật thuộc tính sản phẩm: "
                                                                        + e.getMessage())
                                                        .build());
                }
        }

        // Xóa thuộc tính của sản phẩm
        @DeleteMapping("/{productId}/attributes/{attributeId}")
        public ResponseEntity<ResponseObject> deleteProductAttribute(
                        @PathVariable("productId") Long productId,
                        @PathVariable("attributeId") Long attributeId) {
                try {
                        Product product = productService.removeProductAttribute(productId, attributeId);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .status(HttpStatus.OK)
                                        .message("Xóa thuộc tính sản phẩm thành công")
                                        .data(ProductResponse.fromProduct(product))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseObject.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .message("Lỗi khi xóa thuộc tính sản phẩm: " + e.getMessage())
                                                        .build());
                }
        }        // Cập nhật sản phẩm với danh sách thuộc tính
        @PutMapping("/{id}/update-with-attributes")
        public ResponseEntity<ResponseObject> updateProductWithAttributes(
                        @PathVariable("id") Long productId,
                        @RequestBody Map<String, Object> requestBody) {
                try {
                        ProductDTO productDTO = new ProductDTO();

                        // Lấy thông tin cơ bản của sản phẩm nếu có
                        if (requestBody.containsKey("name")) {
                                productDTO.setName((String) requestBody.get("name"));
                        }
                        if (requestBody.containsKey("price")) {
                                productDTO.setPrice(Float.valueOf(requestBody.get("price").toString()));
                        }
                        if (requestBody.containsKey("description")) {
                                productDTO.setDescription((String) requestBody.get("description"));
                        }
                        if (requestBody.containsKey("category_id")) {
                                productDTO.setCategoryId(Long.valueOf(requestBody.get("category_id").toString()));
                        }
                        if (requestBody.containsKey("quantity")) {
                                productDTO.setQuantity(Long.valueOf(requestBody.get("quantity").toString()));
                        }

                        // Lấy danh sách thuộc tính
                        List<Long> attributeIds = new ArrayList<>();
                        List<String> attributeValues = new ArrayList<>();

                        if (requestBody.containsKey("attributes") && requestBody.get("attributes") instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> attributes = (List<Map<String, Object>>) requestBody
                                                .get("attributes");

                                for (Map<String, Object> attribute : attributes) {
                                        if (attribute.containsKey("attribute_id") && attribute.containsKey("value")) {
                                                attributeIds.add(
                                                                Long.valueOf(attribute.get("attribute_id").toString()));
                                                attributeValues.add(attribute.get("value").toString());
                                        }
                                }
                        }

                        // Cập nhật sản phẩm với thuộc tính
                        Product updatedProduct = productService.updateProductWithAttributes(
                                        productId, productDTO, attributeIds, attributeValues);
                        return ResponseEntity.ok(ResponseObject.builder()
                                        .status(HttpStatus.OK)
                                        .message("Cập nhật sản phẩm với thuộc tính thành công")
                                        .data(ProductResponse.fromProduct(updatedProduct))
                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseObject.builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .message(e.getMessage())
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseObject.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .message("Lỗi khi cập nhật sản phẩm với thuộc tính: "
                                                                        + e.getMessage())
                                                        .build());
                }
        }        // Cập nhật sản phẩm với thuộc tính động (thực hiện theo EAV model)
        @PutMapping("/{id}")
        public ResponseEntity<ResponseObject> updateProduct(
                        @PathVariable("id") Long productId,
                        @RequestBody ProductUpdateDTO productUpdateDTO) {
                try {
                        // Cập nhật sản phẩm với thuộc tính động theo tên
                        Product updatedProduct = productService.updateProductWithDynamicAttributes(productId,
                                        productUpdateDTO);

                        return ResponseEntity.ok(ResponseObject.builder()
                                        .status(HttpStatus.OK)
                                        .message("Cập nhật sản phẩm thành công")
                                        .data(ProductResponse.fromProduct(updatedProduct))
                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseObject.builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .message(e.getMessage())
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseObject.builder()
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .message("Lỗi khi cập nhật sản phẩm: " + e.getMessage())
                                                        .build());
                }
        }
}
